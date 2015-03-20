package mp;

import mp.util.Utils;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import static java.lang.System.out;

/** Una classe per fare test sui file */
public class TestFile {
    public static void main(String[] args) {
        //info();
        test_wordMap();
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
                Map<String,Integer> map = Utils.wordMap(path, charset);
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
