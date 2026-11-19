# Student Exercise Hint: EX-2 (Value-for-Money Comparator)

## Method to Implement
Location: `src/main/java/com/revline/service/CarComparators.java`

```java
public static Comparator<Car> byValueForMoney()
```

---

## Goal
Create and return a `Comparator<Car>` that ranks vehicles according to their **horsepower-per-dollar ratio**. Buyers looking for the biggest performance bargain should see high-horsepower, reasonably-priced supercars at the top.

### Syllabus Topics
- **Lecture 8**: Custom sorting using `Comparator`, multi-criteria tie-breaking (`thenComparing` or manual conditional comparison).

---

## Metric Formula
For any car `c`:
$$\text{score} = \left(\frac{\text{c.getHorsepower()}}{\text{c.getPrice()}}\right) \times 100{,}000.0$$

*Higher score means better value-for-money, so higher score must come first (descending order).*

### Tie-Breaking Rule
If two cars have essentially the same score (e.g. `Math.abs(score1 - score2) < 0.001`), tie-break by **0-100 acceleration time ascending** (the quicker car with smaller acceleration seconds wins).

---

## Hint / Algorithm

You can write this cleanly using a lambda or an anonymous class:

```java
return (c1, c2) -> {
    double score1 = ((double) c1.getHorsepower() / c1.getPrice()) * 100000.0;
    double score2 = ((double) c2.getHorsepower() / c2.getPrice()) * 100000.0;

    // Compare scores in descending order (score2 vs score1)
    if (Math.abs(score1 - score2) > 0.001) {
        return Double.compare(score2, score1);
    }

    // Tie-break: quicker 0-100 acceleration ascending (accel1 vs accel2)
    return Double.compare(c1.getAcceleration(), c2.getAcceleration());
};
```

---

## Verification
Run `scripts/test.bat` (or `scripts/test.sh`) to see `CarComparatorsTest` verify your comparator against seeded cars.
