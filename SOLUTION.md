# Solution

The challenge specification defines 3 endpoints -- one for registering
transactions, one for getting summary statistics from transactions that happened
in the last 60 seconds and one deleting all registered transactions. Both are
supposed to run in constant time and space which makes this a non-trivial task.

Lets start with naive, not optimal solution first and see how we can improve it
from there.

## Sliding window statistics

Maintaining statistics over a sliding time window requires keeping values of
transactions not older than Δt in some kind of queue. If we were receiving them
in an increasing timestamp order we could use a simple FIFO queue, but because
we might get them out of order we should probably keep them in a more organized
way. A way that would allow us cheaply identify (and remove) transactions that
happened over Δt ago so that we could update aggregates (easy for `sum`,
`count` and `avg`, but more challenging for `max` and `min` which might require
finding a new maximum/minimum value in the sliding window). One such data
structure is a [priority queue](https://en.wikipedia.org/wiki/Priority_queue),
built on top of [binary heap](https://en.wikipedia.org/wiki/Binary_heap). It
gives access to the *least* element (head) in a constant time, but adding (and
removing) elements from it take O(log(n)) time.

(There's even a thread safe version of such queue in Java standard library that
allows easy retrieval/removal of expired elements --
[`java.util.concurrent.DelayQueue`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/DelayQueue.html)).

Unfortunately, this makes transaction recording endpoint O(log(n)) in time and
O(n) in space (where n is a number of *non-expired* transactions).

Can we do better than that?

## Reducing complexities with timestamp quantization

Unfortunately in order to calculate correct statistics we need all the
transactions from a given time window. That means that maintaining correct
statistics requires at least O(n) of space.

We can decrease that complexity if we allow for some degree of imprecision. For
example, we could split time window into smaller "buckets", each of width Δt/C,
holding aggregates of transactions that happened in this small time frame. One
important detail is that these "buckets" remain fixed in time, they don't
"slide". We can simulate effect of "sliding" by periodically appending new
buckets and removing *expired* ones. In fact, we don't even need to mutate our
collection of buckets as we can reuse expired buckets (we just need to clear
its aggregates and move the "read pointer"). This concept is very similar to
how [circular buffers](https://en.wikipedia.org/wiki/Circular_buffer) work, but
unlike typical circular buffers it also provides constant time random access.

Whenever a new transaction is being registered we update aggregates of specific
bucket, that fulfills the following criteria:

    index * Δt/C <= T - t' < (index + 1) * Δt/C

Where:

- index - bucket index (starting from 0)
- C - number of buckets ("resolution")
- T - current timestamp *quantized*
- t' - transaction timestamp
- Δt - time window width

This little trick reduces space and time complexity to `O(C)`, where `C` is the
number of buckets -- a constant that does not depend on the number of
registered transactions.

## Statistics summary errors

This solution can potentially return incorrect result, which comes from the
fact that that we store transaction values aggregated by time-frame in which
they happened. Each Δt/C we "slide" by dropping "oldest" bucket. This means
that we either expire transactions that should not yet expire or we keep
transactions that already expired for a little longer (it's possible to choose
which one we prefer, in my implementation I chose the former).

It's hard to precisely say what errors this introduce, but in uniform
distribution o transactions in time (with similar values) this error is ~1/C
(which means ~1% for 100 buckets).

## Thread safety

Thread safety can be achieved in many ways and Java provides many helpers and
utilities that can help with that.

One important topic to consider is synchronization granularity. Full
synchronization of bucket list with a single mutex solves the problem of thread
safety but might result in high lock contention. Since transactions can come
with a different timestamps they might as well target different
buckets. Therefore it might make more sense to have a mutex per bucket.

It's important to notice that too fine-grained solution, like using atomic
variables for aggregate values, might not be sufficient. While atomic
variables protect from data races they don't protect from data
inconsistencies.
(We need synchronized updates to `sum` and `count` in order to have consistent
`avg` value.)

I chose to base my implementation on AtomicReferenceArray where changes to each
bucket are made in "optimistic" update loops (that use
[CAS](https://en.wikipedia.org/wiki/Compare-and-swap) mechanism).
