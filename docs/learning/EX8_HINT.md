# Student Exercise Hint: EX-8 (Guarded Wait & Dequeue in Producer-Consumer)

## Method to Implement
Location: `src/main/java/com/revline/concurrency/PurchaseRequestQueue.java`

```java
public synchronized PurchaseRequest take() throws InterruptedException
```

---

## Goal
Implement the **Consumer Dequeue** operation for RevLine's bounded buffer purchase request queue using **guarded blocks** (`wait()` and `notifyAll()`).

### Syllabus Topics
- **Lectures 15–16**: Inter-thread communication, `wait()`, `notify()`, `notifyAll()`, guarded blocks, and the Producer-Consumer pattern.
- **Lecture 13–14**: Shared data, synchronized methods, intrinsic monitor locks, memory visibility.

---

## Why Guarded Blocks?
In a bounded buffer:
- If the queue is empty, a consumer cannot take anything yet. It must release the monitor lock and **wait** until a producer puts an item into the queue.
- When a producer puts an item, it calls `notifyAll()` to wake up waiting consumers.
- Always use a `while` loop, **never an `if` statement**, to check the condition around `wait()`:
  ```java
  while (queue.isEmpty() && active) {
      wait();
  }
  ```
  Why? Because of **spurious wakeups** (the thread may wake up without notification) and because another consumer thread might have dequeued the item first before this thread regained the monitor lock.

---

## Step-by-Step Implementation Guide

Open `src/main/java/com/revline/concurrency/PurchaseRequestQueue.java` and look at `take()`:

```java
public synchronized PurchaseRequest take() throws InterruptedException {
    // 1. Guarded wait loop: while queue is empty AND queue is still active, wait()
    while (queue.isEmpty() && active) {
        wait();
    }

    // 2. Check if stopped while empty: if deactivated and nothing left, return null
    if (!active && queue.isEmpty()) {
        return null;
    }

    // 3. Dequeue the head element from the LinkedList
    PurchaseRequest request = queue.poll();

    // 4. Notify waiting producers: because we removed an item, there is now room
    //    for any producer thread blocked in put()!
    notifyAll();

    // 5. Return the retrieved purchase request
    return request;
}
```

---

## How to Verify
Run the automated test script:
```bat
.\scripts\test.bat
```
Watch the exercise report flip from:
```
  [PENDING]  EX-8: PurchaseRequestQueue.take()
```
to:
```
  [COMPLETE] EX-8: PurchaseRequestQueue.take()
```
And the final summary will show:
```
=================================================
 Exercises  : 9/9 Completed (ALL EXERCISES DONE!)
=================================================
```
