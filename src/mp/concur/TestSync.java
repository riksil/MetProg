package mp.concur;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;

/** Classe per testare la sincronizzazione di threads */
public class TestSync {
    /** Un generatore di interi (tutti diversi) */
    public interface Gen {
        int getNext();
    }

    /** Implementazione semplice di {@link mp.concur.TestSync.Gen} */
    public static class SimpleGen implements Gen {
        @Override
        public int getNext() { return counter++; }

        private int counter = 0;
    }

    /** Implementazione thread safe di {@link mp.concur.TestSync.Gen} che usa la
     * sincronizzazione */
    public static class SyncGen implements Gen {
        @Override
        public synchronized int getNext() { return counter++; }

        private int counter = 0;
    }

    /** Implementazione thread safe di {@link mp.concur.TestSync.Gen} che usa
     * una variabie atomica. */
    public static class AtomicGen implements Gen {
        @Override
        public int getNext() { return counter.getAndIncrement(); }

        private final AtomicInteger counter = new AtomicInteger(0);
    }

    /** Mette alla prova un generatore con un dato numero di threads e tasks.
     * @param g  un generatore
     * @param nThreads  numero threads
     * @param nTasks  numero tasks */
    public static void test_Gen(Gen g, int nThreads, int nTasks) {
        out.println("Threads: "+nThreads+"  Tasks: "+nTasks);
        ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        List<Future<Integer>> tasks = new ArrayList<>();
        Set<Integer> vals = new HashSet<>();
        try {
            for (int i = 0; i < nTasks; i++)
                tasks.add(exec.submit(g::getNext));
            for (Future<Integer> t : tasks)
                vals.add(t.get());
        } catch (InterruptedException | ExecutionException e) {
        } finally { exec.shutdown(); }
        out.println("Valori ripetuti: "+(nTasks - vals.size()));
    }

    public static void main(String[] args) {
        //test_Gen(new SimpleGen(), 1, 1_000_000);
        //test_Gen(new SimpleGen(), 2, 10_000);
        //test_Gen(new SyncGen(), 1000, 1_000_000);
        test_Gen(new AtomicGen(), 1000, 1_000_000);
    }
}
