import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    private static final int[] EXPERIMENT_SIZES = {100, 1_000, 10_000, 100_000};

    public static void main(String[] args) {
        System.out.println("=============================================================");
        System.out.println("   Monte Carlo Estimation of Pi — Three Methods Compared");
        System.out.println("=============================================================\n");

        for (int n : EXPERIMENT_SIZES) {
            double piCircle  = estimatePiCircle(n);
            double piCoprime = estimatePiCoprime(n);
            double piBuffon  = estimatePiBuffon(n);

            printResultBlock(n, piCircle, piCoprime, piBuffon);
        }

        printSummaryTable();
    }

    // ---------------------------------------------------------------
    //  Method 1 — Quarter-circle inside a unit square
    //
    //  A quarter-circle of radius 1 sits inside the unit square
    //  [0,1] x [0,1].  Area of quarter-circle = pi/4, area of
    //  square = 1.  So  pi ≈ 4 * (points inside) / (total points).
    //
    //  To reduce floating-point accumulation we generate integer
    //  coordinates in [0, MAX] and compare  x² + y² <= MAX²
    //  using long arithmetic (no floating-point until the final
    //  division).
    // ---------------------------------------------------------------
    static double estimatePiCircle(int n) {
        final long MAX = 1_000_000L;
        final long R_SQUARED = MAX * MAX;
        Random rng = ThreadLocalRandom.current();

        long inside = 0;
        for (int i = 0; i < n; i++) {
            long x = nextLong(rng, MAX + 1);
            long y = nextLong(rng, MAX + 1);
            if (x * x + y * y <= R_SQUARED) {
                inside++;
            }
        }
        return 4.0 * inside / n;
    }

    // ---------------------------------------------------------------
    //  Method 2 — Co-prime probability
    //
    //  The probability that two random positive integers are coprime
    //  is  6 / pi².  So  pi ≈ sqrt(6 / (coprimeCount / n)).
    //
    //  GCD is computed on integers — no floating-point at all until
    //  the final formula.
    // ---------------------------------------------------------------
    static double estimatePiCoprime(int n) {
        final int RANGE = 1_000_000;
        Random rng = ThreadLocalRandom.current();

        long coprimeCount = 0;
        for (int i = 0; i < n; i++) {
            int a = rng.nextInt(RANGE) + 1;
            int b = rng.nextInt(RANGE) + 1;
            if (gcd(a, b) == 1) {
                coprimeCount++;
            }
        }

        if (coprimeCount == 0) return Double.NaN;
        double probability = (double) coprimeCount / n;
        return Math.sqrt(6.0 / probability);
    }

    // ---------------------------------------------------------------
    //  Method 3 — Buffon's Needle
    //
    //  A needle of length L is dropped on a floor with parallel
    //  lines spaced D apart (L <= D).  The probability of crossing
    //  a line is  P = 2L / (pi * D).  So  pi ≈ 2L / (P * D).
    //
    //  We choose L = D = 1 for simplicity, giving  pi ≈ 2 / P.
    //
    //  For each drop we need:
    //    • the distance from the needle's center to the nearest
    //      line — uniform in [0, D/2] = [0, 0.5]
    //    • the acute angle θ the needle makes with the lines
    //      — uniform in [0, pi/2]
    //  The needle crosses a line when  centerDist <= (L/2) * sin(θ).
    //
    //  The sin() call is unavoidable here, but we keep the
    //  crossing test in integer-friendly form where we can.
    // ---------------------------------------------------------------
    static double estimatePiBuffon(int n) {
        Random rng = ThreadLocalRandom.current();

        long crossings = 0;
        for (int i = 0; i < n; i++) {
            double centerDist = rng.nextDouble() * 0.5;
            double theta = rng.nextDouble() * Math.PI / 2.0;
            if (centerDist <= 0.5 * Math.sin(theta)) {
                crossings++;
            }
        }

        if (crossings == 0) return Double.NaN;
        return 2.0 * n / crossings;
    }

    // ---------------------------------------------------------------
    //  Utility: fast integer GCD (binary / Stein's algorithm)
    // ---------------------------------------------------------------
    static int gcd(int a, int b) {
        if (a == 0) return b;
        if (b == 0) return a;

        int shift = Integer.numberOfTrailingZeros(a | b);
        a >>= Integer.numberOfTrailingZeros(a);

        do {
            b >>= Integer.numberOfTrailingZeros(b);
            if (a > b) { int t = a; a = b; b = t; }
            b -= a;
        } while (b != 0);

        return a << shift;
    }

    /**
     * Uniform random long in [0, bound) — avoids modulo bias.
     */
    static long nextLong(Random rng, long bound) {
        long bits, val;
        do {
            bits = (rng.nextLong() << 1) >>> 1;
            val = bits % bound;
        } while (bits - val + (bound - 1) < 0L);
        return val;
    }

    // ---------------------------------------------------------------
    //  Pretty-printing
    // ---------------------------------------------------------------
    static void printResultBlock(int n, double piCircle, double piCoprime, double piBuffon) {
        String sep = "-------------------------------------------------------------";
        System.out.println(sep);
        System.out.printf("  N = %,d experiments%n", n);
        System.out.println(sep);
        System.out.printf("  %-28s  %-12s  %-12s%n", "Method", "Estimate", "Error");
        System.out.printf("  %-28s  %-12.8f  %-+12.8f%n",
                "1. Circle-in-Square", piCircle, piCircle - Math.PI);
        System.out.printf("  %-28s  %-12.8f  %-+12.8f%n",
                "2. Co-prime Probability", piCoprime, piCoprime - Math.PI);
        System.out.printf("  %-28s  %-12.8f  %-+12.8f%n",
                "3. Buffon's Needle", piBuffon, piBuffon - Math.PI);
        System.out.printf("  %-28s  %-12.10f%n", "(reference) Math.PI", Math.PI);
        System.out.println();
    }

    static void printSummaryTable() {
        System.out.println("=============================================================");
        System.out.println("   Summary — Absolute errors across all experiment sizes");
        System.out.println("=============================================================");
        System.out.printf("  %10s | %14s | %14s | %14s%n",
                "N", "Circle", "Co-prime", "Buffon");
        System.out.println("  " + "-".repeat(10) + "-+-"
                + "-".repeat(14) + "-+-"
                + "-".repeat(14) + "-+-"
                + "-".repeat(14));

        for (int n : EXPERIMENT_SIZES) {
            double errCircle  = Math.abs(estimatePiCircle(n)  - Math.PI);
            double errCoprime = Math.abs(estimatePiCoprime(n) - Math.PI);
            double errBuffon  = Math.abs(estimatePiBuffon(n)  - Math.PI);
            System.out.printf("  %,10d | %14.10f | %14.10f | %14.10f%n",
                    n, errCircle, errCoprime, errBuffon);
        }

        System.out.println("\n  (Errors are from a fresh run — results vary with each execution.)");
        System.out.println();
    }
}
