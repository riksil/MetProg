package mp.file;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import mp.util.Utils;

import static java.lang.System.out;

/** Una classe per fare test sui file */
public class TestFile {
    /** Mette alla prova un metodo che preso in input il percorso di una directory
     * ritorna la somma di tutti i byte dei file regolari contenuti nella
     * directory. Stampa il valore ritornato, il minimo, il massimo e la media dei
     * tempi di esecuzione relativamente alla n invocazioni. Inoltre, stampa i
     * picchi, registrati durante il test, del numero di thread addizionali usati e
     * della memoria addizionale usata.
     * @param name  nome del metodo
     * @param ts  permette di invocare il metodo
     * @param p  il percorso della directory
     * @param n  numero di volte che il metodo è invocato */
    public static void test_ts(String name, Function<Path,Long> ts, Path p, int n) {
        out.println(name+"  Directory: "+p);
        long max = 0, min = -1, size = 0;
        double average = 0;
        ThreadMXBean tm = ManagementFactory.getThreadMXBean();
        int nt = tm.getThreadCount();   // Numero attuale di thread
        tm.resetPeakThreadCount();
        long mem = Utils.getUsedMem();  // Memoria attualmente usata
        Utils.resetPeakMem();
        try {
            for (int i = 0; i < n; i++) {
                long time = System.currentTimeMillis();
                size = ts.apply(p);
                time = System.currentTimeMillis() - time;
                if (time > max) max = time;
                if (min == -1 || time < min) min = time;
                average += time;
            }
        } catch (Exception ex) { out.println(ex); }
        nt = tm.getPeakThreadCount() - nt;  // Picco numero thread addizionali
        mem = Utils.getPeakMem() - mem;     // Picco memoria addizionale
        average /= n;
        out.println(String.format("Size: %s  Time (seconds): min = %.2f "+
                        " max = %.2f ave = %.2f",
                Utils.toGMKB(size), min/1000.0, max/1000.0, average/1000.0));
        out.println("Picco numero thread addizionali: "+nt);
        out.println("Picco memoria addizionale: " + Utils.toGMKB(mem));
    }


    public static void main(String[] args) {
        Path dir = Paths.get("/usr");
        //test_ts("totalSize", mp.file.Utils::totalSize, dir, 10);
        //test_ts("totalSizeNaiveConcur FixedThreadPool 500", mp.file.Utils::totalSizeNaiveConcur, dir, 10);
        //test_ts("totalSizeNaiveConcur CachedThreadPool", mp.file.Utils::totalSizeNaiveConcur, dir, 10);
        //test_ts("totalSizeNaiveConcur WorkStealingPool", mp.file.Utils::totalSizeNaiveConcur, dir, 10);
        //test_ts("totalSizeConcur", mp.file.Utils::totalSizeConcur, dir, 10);
        //test_ts("totalSizeConcur2", mp.file.Utils::totalSizeConcur2, dir, 10);
        //test_ts("totalSizeQueue", mp.file.Utils::totalSizeQueue, dir, 10);
        test_ts("totalSizeForkJoin", mp.file.Utils::totalSizeForkJoin, dir, 10);
    }


    /** Prova alcuni metodi di {@link java.nio.file.Files} chiedendo un percorso
     * da tastiera e controllando se esiste, se è una directory, ecc. */
    private static void info() {
        Scanner input = new Scanner(System.in);
        out.println("Digita un percorso: ");
        String pathname = input.nextLine();
        Path path = Paths.get(pathname);
        path = path.toAbsolutePath();
        out.println("Percorso assoluto: "+path);
        boolean exists = Files.exists(path);
        out.println("Esiste? "+exists);
        if (exists) {
            out.println("Directory? "+Files.isDirectory(path));
            out.println("File regolare? "+Files.isRegularFile(path));
            out.println("Link simbolico? "+Files.isSymbolicLink(path));
            try {
                info_content(path);
            } catch (IOException e) { out.println(e); }
        }
    }

    /** Se il percorso dato porta a un file regolare con estensione ".txt",
     * stampa il numero di linee del file e anche al più 5 linee random.
     * @param p  il percorso del file
     * @throws IOException se la lettura del file va in errore */
    private static void info_content(Path p) throws IOException {
        if (!Files.isRegularFile(p) || !p.toString().endsWith(".txt"))
            return;
        List<String> lines = Files.readAllLines(p);
        int n = lines.size();
        out.println("Numero linee: "+n);
        for (int i = 0 ; i < Math.min(n, 5) ; i++) {
            int r = (int)Math.floor(Math.random()*n);
            out.println(lines.get(r));
        }
    }

    /** Chiede all'utente di digitare il percorso di un file di testo e il nome
     * di un charset per decodificarne i caratteri e stampa il numero di parole
     * distinte nel file e un campione di al più 100 parole con i relativi
     * conteggi. Continua a chiedere in input un file finché non viene immessa
     * una linea vuota. */
    private static void test_wordMap() {
        Scanner input = new Scanner(System.in);
        while (true) {
            out.println("Digita un percorso: ");
            String pathname = input.nextLine();
            if (pathname.isEmpty()) break;
            Path path = Paths.get(pathname).toAbsolutePath();
            out.println("Digita un charset: ");
            String charset = input.nextLine();
            try {
                Map<String,Integer> map = mp.file.Utils.wordMap(path, charset);
                out.println("Numero parole: "+map.size());
                out.println(randSample(map, 100));
            } catch (IOException e) { out.println(e); }
        }
    }

    /** Ritorna una mappa che contiene un campione random della mappa data.
     * @param map  la mappa da campionare
     * @param expectedSize  numero atteso di chiavi nella mappa campione
     * @return la mappa ottenuta campionando la mappa data */
    private static Map<String,Integer> randSample(Map<String,Integer> map,
                                                  int expectedSize) {
        Map<String,Integer> sample = new HashMap<>();
        if (map.size() == 0) return sample;
        double p = ((double)expectedSize)/map.size(); // Probabilità di selezionare
                                                      // una chiave
        for (String k : map.keySet())
            if (Math.random() <= p)
                sample.put(k, map.get(k));
        return sample;
    }
}
