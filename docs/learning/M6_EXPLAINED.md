# Module 6: Multithreading & Concurrency Architecture (L9–L16)

> **Module 6**: Multithreading, Thread Lifecycle, Monitor Synchronization & Producer-Consumer  
> **Target Milestone**: Phase 6 (`v0.6-concurrency`)  
> **Target Syllabus**: 25CSE0202 Programming in Java (Lectures 9, 10, 11, 12, 13, 14, 15, 16)

---

## 1. Architectural Overview

RevLine incorporates multithreading at both the background system layer and the interactive application layer:
1. **Background System Daemons**:
   - **`AutoSaveThread` (`extends Thread`)**: Low-priority (`MIN_PRIORITY`) daemon thread executing periodic snapshot flushes of the showroom fleet via `inventoryService.saveSnapshot()`. Controlled cleanly via `interrupt()` and `join()`.
   - **`ReservationExpiryThread` (`implements Runnable`)**: Daemon thread continuously sweeping the `ReservationManager` min-heap every 5 seconds to restore expired reservation holds back to `AVAILABLE`.
2. **Producer-Consumer Sales Pipeline (L15, L16)**:
   - **`PurchaseRequestQueue`**: Bounded buffer (capacity 5) utilizing intrinsic monitor locks with **guarded blocks** (`wait()` and `notifyAll()`).
   - **`SalesProcessor` (`implements Runnable`)**: Consumer worker thread processing incoming purchase requests asynchronously.
3. **Interactive Concurrency Lab (`ConcurrencyLab`)**:
   - 5 deterministic demonstrations accessible via Seller Menu `[8]` illustrating race conditions, atomicity, thread states, producer-consumer pipelines, and memory visibility.

---

## 2. Multiprogramming vs. Multithreading (L9, L10)

| Concept | Multiprogramming | Multithreading |
| :--- | :--- | :--- |
| **Execution Entity** | Multiple independent processes | Multiple execution paths within a single process |
| **Memory Space** | Isolated address spaces per process (IPC required) | Shared heap memory and method area; separate call stacks |
| **Context Switching** | High overhead (swapping page tables & cache) | Low overhead (switching CPU register state & stack pointer) |
| **RevLine Implementation** | Running the RevLine JVM process alongside SQLite | `AutoSaveThread`, `SalesProcessor`, and UI thread sharing `InventoryService` |

### Creating Threads: `extends Thread` vs. `implements Runnable`
- **`AutoSaveThread extends Thread`**: Chosen when the class represents a specialized thread entity with lifecycle hooks (`shutdown()`).
- **`ReservationExpiryThread implements Runnable`**: Chosen to decouple task logic from the threading execution mechanism, allowing composition.

---

## 3. Thread Lifecycle & States (L11, L12)

Java threads transition through 6 distinct states defined in `java.lang.Thread.State`:

```
                       start()
     [ NEW ] ──────────────────────────► [ RUNNABLE ] (Ready or Running)
                                            │      ▲
                        sleep() / join()    │      │  sleep expired / join finished
                                            ▼      │
                                   [ TIMED_WAITING ]
                                            │      ▲
                        wait() on monitor   │      │  notify() / notifyAll()
                                            ▼      │
                                      [ WAITING ]
                                            │      ▲
                        waiting for monitor │      │  monitor lock acquired
                                            ▼      │
                                      [ BLOCKED ]
                                            │
                                            ▼ run() completes / uncaught exception
                                      [ TERMINATED ]
```

### Demonstration via `ThreadStateTracer`:
RevLine traces each state transition deterministically in `ConcurrencyLab.runDemo3ThreadLifecycleTracer()`, proving transitions from `NEW` to `RUNNABLE`, `TIMED_WAITING`, `BLOCKED`, `WAITING`, and `TERMINATED`.

### Daemon Threads & Priority (L12):
- **Daemon Threads (`setDaemon(true)`)**: Service threads running in the background. The JVM automatically terminates when only daemon threads remain.
- **Thread Priorities (`1` to `10`)**: An operating system scheduling hint; not a strict execution guarantee. RevLine uses `Thread.MIN_PRIORITY` for background autosaving.

---

## 4. Shared Data & Synchronization (L13, L14)

When multiple threads concurrently execute read-modify-write operations on shared state, race conditions occur.

### Unsafe vs. Safe Counter:
```java
// Unsafe: count++ expands to:
// 1. READ count from memory into register
// 2. MODIFY register by adding 1
// 3. WRITE register back to memory
// Interleaved execution loses updates!
public void increment() { count++; }

// Safe: synchronized method acquires the intrinsic lock of 'this':
public synchronized void increment() { count++; }
```

### The Double-Sell Race Condition:
In showroom sales, selling a car requires a check-then-act sequence:
```
1. Check if car.getStatus() == AVAILABLE
2. If available, update status to SOLD and persist transaction
```
Without synchronization, two concurrent buyers can both observe `AVAILABLE` at step 1 before either reaches step 2, causing the same supercar to be sold twice.

**Solution in `SalesService`**:
```java
synchronized (lock) {
    Car car = inventoryService.findById(carId);
    if (car.getStatus() == Status.SOLD) {
        throw new CarNotAvailableException(carId, "Already sold");
    }
    inventoryService.updateCarStatus(carId, Status.SOLD);
    saleRepository.save(sale);
}
```
All mutations across `SalesService` and `InventoryService` synchronize on the **same intrinsic monitor lock** (`inventoryService.getLock()`).

---

## 5. Guarded Blocks & Producer-Consumer (L15, L16)

The Producer-Consumer pattern decouples tasks that produce purchase requests from workers that process them.

### Why `while` Instead of `if` for `wait()`?
Always enclose `wait()` inside a `while` loop:
```java
while (queue.size() >= capacity && active) {
    wait();
}
```
**Two Critical Reasons**:
1. **Spurious Wakeups**: The operating system or JVM may wake a thread from `wait()` without an explicit `notify()` or `notifyAll()`.
2. **State Invalidation**: Another thread may have acquired the lock first and consumed the slot before this thread resumed execution.

### EX-8 Guarded Dequeue:
`PurchaseRequestQueue.take()` waits while the queue is empty and active, returns `null` after shutdown when no requests remain, and notifies blocked producers after dequeuing a request. Its implementation is covered by the EX-8 check in `ConcurrencyStressTest`.

---

## 6. Thread Interruption & Memory Visibility (L10, L14)

### Graceful Interruption:
Instead of deprecated and unsafe methods like `Thread.stop()`, Java uses cooperative cancellation via `interrupt()`:
```java
public void shutdown() {
    this.running = false;
    interrupt(); // Wakes thread if blocked in sleep() or wait()
}
```
When `Thread.sleep()` receives an interrupt, it throws `InterruptedException` and clears the interrupt status. The thread restores the flag via `Thread.currentThread().interrupt()` and terminates its loop cleanly.

### Memory Visibility & `volatile`:
Without `volatile`, one thread modifying a boolean variable may store the update in its local CPU register or cache, invisible to another thread reading from main memory. Declaring `volatile boolean running` establishes a **happens-before** memory barrier ensuring immediate cross-thread visibility.

---

## 7. Verification & Stress Testing (GATE 6)

All concurrency invariants are tested in `ConcurrencyStressTest`:
1. **SafeCounter Integrity**: 4 threads x 5,000 increments = 20,000 exact total.
2. **AutoSave Interruption**: Daemon thread starts, interrupts, and joins within 1.5 seconds.
3. **Reservation Expiry Sweep**: Periodic background sweep cleans active reservations.
4. **20-Round Stress Showdown**: 20 consecutive rounds with 15 concurrent buyer threads. In every round:
   - Exactly **1 buyer succeeds**.
   - Exactly **14 buyers fail** with `CarNotAvailableException`.
   - Vehicle status moves to `SOLD`.
   - **Zero deadlocks** detected across all rounds.
