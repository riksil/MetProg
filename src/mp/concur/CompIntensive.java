package mp.concur;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import static java.lang.System.out;

/** Classe per testare implementazioni parallele di compiti ad alta intensità di
 * calcolo. */
public class CompIntensive {
    /** Ritorna il massimo numero di passi dell'algorimo della congettura di
     * Collatz, per tutti gli interi nell'intervallo [a, b].
     * @param a  inizio intervallo
     * @param b  fine intervallo
     * @return il massimo numero di passi */
    public static long collatz(long a, long b) {
        long max = 0;
        for (long i = a ; i <= b ; i++) {
            long n = i, t = 0;
            while (n != 1) {
                if (n % 2 == 0) n /= 2;
                else n = 3*n + 1;
                t++;
            }
            if (t > max) max = t;
        }
        return max;
    }

    /** Implementazione parallela di {@link CompIntensive#collatz(long, long)}.
     * @param a  inizio intervallo
     * @param b  fine intervallo
     * @return il massimo numero di passi */
    public static long collatzParallel(long a, long b) {
        int np = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(np);
        List<Future<Long>> tasks = new ArrayList<>();
        long nTasks = Math.min(np, (b - a + 1));
        long size = (b - a + 1)/nTasks;
        long max = 0;
        try {
            for (int i = 0 ; i < nTasks ; i++) {
                long ta = a + i*size;
                long tb = (i == nTasks - 1 ? b : ta + size - 1);
                tasks.add(exec.submit(() -> collatz(ta, tb)));
            }
            for (Future<Long> t : tasks) {
                long m = t.get();
                if (m > max) max = m;
            }
        } catch (InterruptedException | ExecutionException e) {
        } finally { exec.shutdown(); }
        return max;
    }

    /** Ritorna il numero di primi nell'intervallo [a, b].
     * @param a  inizio intervallo
     * @param b  fine intervallo
     * @return il numero di primi nell'intervallo [a, b] */
    public static long numPrimes(long a, long b) {
        long np = 0;
        for (long n = a ; n <= b ; n++) {
            double sqrt = Math.sqrt(n);
            long d = 2;
            while (d <= sqrt && n % d != 0) d++;
            if (d > sqrt) np++;
        }
        return np;
    }

    /** Implementazione parallela di
     * {@link mp.concur.CompIntensive#numPrimes(long, long)}.
     * @param a  inizio intervallo
     * @param b  fine intervallo
     * @return il numero di primi nell'intervallo [a, b] */
    public static long numPrimesParallel(long a, long b) {
        int np = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(np);
        List<Future<Long>> tasks = new ArrayList<>();
        long nTasks = Math.min(10*np, (b - a + 1));
        long size = (b - a + 1)/nTasks;
        long nPrimes = 0;
        try {
            for (int i = 0 ; i < nTasks ; i++) {
                long ta = a + i*size;
                long tb = (i == nTasks - 1 ? b : ta + size - 1);
                tasks.add(exec.submit(() -> numPrimes(ta, tb)));
            }
            for (Future<Long> t : tasks) {
                nPrimes += t.get();
            }
        } catch (InterruptedException | ExecutionException e) {
        } finally { exec.shutdown(); }
        return nPrimes;
    }

    public static void test_comp(long a, long b, BiFunction<Long,Long,Long> cmp) {
        out.println("Intervallo ["+a+", "+b+"]");
        long time = System.currentTimeMillis();
        long r = cmp.apply(a, b);
        out.println(r+"  time: "+(System.currentTimeMillis() - time)+"ms");
    }

    public static void main(String[] args) {
        //test_comp(1, 20_000_000, CompIntensive::collatz);
        //test_comp(1, 20_000_000, CompIntensive::collatzParallel);
        //test_comp(1, 30_000_000, CompIntensive::numPrimes);
        test_comp(1, 30_000_000, CompIntensive::numPrimesParallel);
    }
}
