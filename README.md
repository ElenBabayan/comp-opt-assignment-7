# Monte Carlo Pi Estimation

Estimates pi using three different Monte Carlo approaches, then compares their accuracy
across 100, 1 000, 10 000, and 100 000 random experiments.

## Methods

1. **Circle-in-square** — drop random points into the unit square and check whether they
   land inside a quarter-circle of radius 1. Ratio of hits gives pi/4.

2. **Co-prime probability** — pick two random integers and check if their GCD is 1.
   The probability of that happening is 6/pi², so we can solve for pi.

3. **Buffon's needle** — simulate dropping a unit-length needle onto parallel lines
   spaced 1 unit apart. The crossing probability is 2/pi.

## Reducing floating-point error

Methods 1 and 2 keep all per-sample work in pure integer arithmetic — no doubles
are used inside the simulation loops.

- In the circle method, coordinates are random longs in [0, 1 000 000] and the
  distance check (`x*x + y*y <= R*R`) is a long comparison. A double only appears
  in the final `4.0 * inside / n`.
- In the co-prime method, GCD is computed with Stein's binary algorithm (shifts and
  subtracts, no division). Again, floating-point is only used at the very end
  (`sqrt(6.0 / probability)`).
- Buffon's needle inherently needs `sin(θ)`, so there's no way to fully avoid
  floating-point there.

## How to run

```
javac src/Main.java -d out/
java -cp out Main
```

Output is a per-N breakdown of each method's estimate and error, plus a summary table.
Results change on every run because, well, it's Monte Carlo.

## Results summary

All three methods converge toward the true value of pi as N grows, but the rate and
consistency differ quite a bit.

At **N = 100** the estimates are all over the place — errors of 0.05 to 0.4 are common
across all methods. That's expected; 100 samples just isn't enough for any of them.

By **N = 1 000** things start settling down. Errors typically drop to the 0.01–0.08
range, though individual runs still vary a lot.

At **N = 10 000** the estimates are consistently within about 0.01–0.03 of pi. The
circle method and co-prime method tend to be a bit tighter here, while Buffon's needle
is a bit noisier.

At **N = 100 000** all three methods get within roughly 0.001–0.007 of pi. The circle
method is the most reliable — it converges smoothly and doesn't have much variance
between runs. The co-prime method is also solid, sometimes even slightly more accurate,
but it's a bit less predictable run-to-run. Buffon's needle works but tends to have the
largest errors of the three at the same sample size, probably because the sin() in its
inner loop adds geometric noise on top of the statistical noise.

Overall takeaway: the circle-in-square method is the simplest and most stable. The
co-prime method is a fun number-theory trick that works surprisingly well. Buffon's
needle is the most "physical" simulation but converges the slowest of the three.
