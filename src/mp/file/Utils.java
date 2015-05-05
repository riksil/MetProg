package mp.file;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.System.out;

/** Una classe di metodi di utilità per file */
public class Utils {
    /** Ritorna una mappa che ad ogni parola del file specificato associa il
     * numero di occorrenze. Per parola si intende una sequenza di lettere
     * (riconosciute dal metodo {@link Character#isLetter(char)}) di
     * lunghezza massimale. Le parole sono sensibili alle maiuscole/minuscole.
     * @param path  il percorso del file
     * @param charset  il charset per decodificare i caratteri
     * @return  una mappa che conta le occorenze delle parole
     * @throws java.io.IOException se si verifica un errore accedendo al file */
    public static Map<String,Integer> wordMap(Path path, String charset)
            throws IOException {
        Map<String,Integer> map = new HashMap<>();
        try (Scanner scan = new Scanner(path, charset)){
            scan.useDelimiter("[^\\p{IsLetter}]+");    // Caratteri != lettere
            while (scan.hasNext()) {
                String w = scan.next();
                Integer n = map.get(w);
                map.put(w, (n != null ? n+1 : 1));
            }
        }
        return map;
    }

    /** Ritorna una stringa che rappresenta l'albero di directory e file a partire
     * dal percorso specificato.
     * @param root  percorso della directory radice
     * @return  una stringa che rappresenta l'albero di directory e file
     * @throws java.io.IOException se si verifi qualche errore nell'accesso ai file/dir */
    public static String fileTreeToString(Path root) throws IOException {
        // Classe locale che realizza un visitatore di un albero di dir e file
        class Visitor extends SimpleFileVisitor<Path> {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                if (Files.isHidden(dir)) return FileVisitResult.SKIP_SUBTREE;
                s += pre+(pre.isEmpty() ? "" : "---")+dir.getFileName()+"\n";
                pre += "    |";
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                s += pre+"---"+file.getFileName()+" " + attrs.size()+"\n";
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                pre = pre.substring(0, pre.length() - 4);
                s += pre + "\n";
                return FileVisitResult.CONTINUE;
            }

            String s = "", pre = "";
        }
        Visitor vis = new Visitor();    // Crea il visitatore e lo passa
        Files.walkFileTree(root, vis);  // al metodo che effettua la visita
        return vis.s;                   // Al termine della visita, in s c'è
    }                                   // la rappresentazione dell'albero

    private static void test_fileTreeToString() {
        Scanner input = new Scanner(System.in);
        out.println("Test fileTreeToString: digita un percorso: ");
        String pathname = input.nextLine();
        Path root = Paths.get(pathname).toAbsolutePath();
        try {
            out.println(fileTreeToString(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final LinkOption NOL = LinkOption.NOFOLLOW_LINKS;

    /** Ritorna il numero totale di byte contenuti nella directory specificata. Se
     * non è una directory, ritorna 0. I link simbolici non sono seguiti, solamente
     * i file regolari sono conteggiati e tutti gli errori di I/O sono ignorati.
     * @param d  percorso di una directory
     * @return il numero totale di byte contenuti nella directory */
    public static long totalSize(Path d) {
        long size = 0;
        try (Stream<Path> list = Files.list(d)) {
            for (Path p : list.toArray(Path[]::new)) {
                if (Files.isDirectory(p, NOL)) {
                    size += totalSize(p);
                } else if (Files.isRegularFile(p, NOL))
                    size += Files.size(p);       // Files.size è garantito
            }                                    // solo per file regolari
        } catch (IOException ex) { }  // Ignora errori di I/O
        return size;
    }

    /** Implementazione non ricorsiva di {@link mp.file.Utils#totalSize(Path)}.
     * @param d  percorso di una directory
     * @return il numero totale di byte contenuti nella directory */
    public static long totalSizeNR(Path d) {
        class Tot {
            long total(Path d) {  // Ritorna numero totale di byte della dir d
                long size = 0;
                try (Stream<Path> list = Files.list(d)) {
                    for (Path e : list.toArray(Path[]::new)) {
                        if (Files.isDirectory(e, NOL)) {
                            size += new Tot().total(e);  // No chiamata ricorsiva
                        } else if (Files.isRegularFile(e, NOL))
                            size += Files.size(e);
                    }
                } catch (IOException ex) { }
                return size;
            }
        }
        return new Tot().total(d);
    }

    /** Implementazione concorrente di {@link mp.file.Utils#totalSize(Path)}. Esegue
     * un task per ogni directory. Il task per una directory sottomette i task per
     * le sub-directory, aspetta che completano e ritorna la somma dei byte di tutti
     * i file regolari contenuti nella directory.
     * @param d  percorso di una directory
     * @return il numero totale di byte contenuti nella directory */
    public static long totalSizeNaiveConcur(Path d) {
        //ExecutorService exec = Executors.newFixedThreadPool(500);
        //ExecutorService exec = Executors.newCachedThreadPool();
        ExecutorService exec = Executors.newWorkStealingPool();
        class Tot {
            long total(Path d) {
                long size = 0;
                List<Future<Long>> tasks = new ArrayList<>();
                try (Stream<Path> list = Files.list(d)) {
                    for (Path e : list.toArray(Path[]::new)) {
                        if (Files.isDirectory(e, NOL)) {
                            tasks.add(exec.submit(() -> new Tot().total(e)));
                        } else if (Files.isRegularFile(e, NOL))
                            size += Files.size(e);
                    }
                } catch (IOException e) { }
                for (Future<Long> t : tasks)
                    try {
                        size += t.get(30, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException |
                            TimeoutException ex) {
                        throw new RuntimeException(ex);
                    }
                return size;
            }
        }
        try {
            return new Tot().total(d);
        } finally { exec.shutdown(); }
    }

    /** Implementazione concorrente di {@link mp.file.Utils#totalSize(Path)}. Esegue
     * un task per ogni directory. Il task per una directory sottomette i task per
     * le sub-directory e ritorna la somma dei byte dei file regolari direttamente
     * contenuti nella directory. Per eseguire i task usa un
     * {@link java.util.concurrent.CompletionService}. Per mantenere il conto dei
     * task ancora pendenti (cioè sottomessi ma non ancora completati) usa un
     * {@link java.util.concurrent.atomic.LongAdder}.
     * @param d  percorso di una directory
     * @return il numero totale di byte contenuti nella directory */
    public static long totalSizeConcur(Path d)  {
        int np = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(np);
        CompletionService<Long> exec = new ExecutorCompletionService<>(pool);
        LongAdder pending = new LongAdder();  // Tiene il conto dei task pendenti
        class Tot {
            long total(Path d) {  // Sottomette i task delle sub-directory e
                long size = 0;    // ritorna la somma dei byte dei file regolari
                try (Stream<Path> list = Files.list(d)) {
                    for (Path e : list.toArray(Path[]::new)) {
                        if (Files.isDirectory(e, NOL)) {
                            pending.increment();   // Sottomette il sub-task per la
                            exec.submit(() -> new Tot().total(e)); // sub-directory
                        } else if (Files.isRegularFile(e, NOL))
                            size += Files.size(e);
                    }
                } catch (IOException ex) { }
                return size;
            }
        }
        long total = 0;
        try {
            pending.increment();
            exec.submit(() -> new Tot().total(d));
            while (pending.sum() > 0) {       // Finché ci sono task pendenti,
                total += exec.take().get();   // chiedi il risultato di un task
                pending.decrement();          // e decrementa i task pendenti
            }
        } catch (InterruptedException | ExecutionException exc) {
        } finally { pool.shutdown(); }
        return total;
    }

    /** Implementazione alternativa di {@link mp.file.Utils#totalSizeConcur(Path)}
     * che non usa variabili condivise mutabili. Un task per ogni directory che
     * ritorna la lista delle sub-directory e la somma dei bytes dei file regolari
     * direttamente contenuti nella directory. Tutti i task sono sottomessi nel
     * thread d'invocazione del metodo.
     * @param p  percorso di una directory
     * @return il numero totale di bytes contenuti nella directory */
    public static long totalSizeConcur2(Path p)  {
        class Content {               // Il contenuto di una directory
            Content(long s, List<Path> list) {
                size = s;
                dirs = list;
            }
            final long size;          // Somma dei bytes dei file
            final List<Path> dirs;    // Lista delle sub-directory
        }
        Function<Path,Content> getCont = d -> { // Ritorna il contenuto della dir d
            long size = 0;                      // e non sottomette sub-task
            List<Path> dirs = new ArrayList<>();
            try (Stream<Path> list = Files.list(d)) {
                for (Path e : list.toArray(Path[]::new)) {
                    if (Files.isDirectory(e, NOL)) {
                        dirs.add(e);
                    } else if (Files.isRegularFile(e, NOL))
                        size += Files.size(e);
                }
            } catch (IOException ex) { }
            return new Content(size, dirs);
        };
        int np = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(np);
        CompletionService<Content> exec = new ExecutorCompletionService<>(pool);
        long total = 0, pending = 0;
        try {
            pending++;
            exec.submit(() -> getCont.apply(p));
            while (pending > 0) {
                Content c = exec.take().get();  // Il prossimo task completato
                total += c.size;
                pending += c.dirs.size() - 1;
                for (Path d : c.dirs)           // Sottomette i sub-task del task
                    exec.submit(() -> getCont.apply(d));
            }
        } catch (InterruptedException | ExecutionException e) {
        } finally { pool.shutdown(); }
        return total;
    }

    /** Implementazione concorrente di {@link mp.file.Utils#totalSize(Path)}. Usa
     * una coda sincronizzata {@link java.util.concurrent.ConcurrentLinkedQueue} e
     * un contatore atomico {@link java.util.concurrent.atomic.LongAdder} per la
     * somma dei byte dei file. Il task per una directory aggiunge alla coda le
     * sub-directory e aggiorna il contatore atomico con i byte dei file regolari
     * direttamente contenuti nella directory. Per eseguire i task usa un
     * {@link java.util.concurrent.Executors#newFixedThreadPool(int)}.
     * @param p  percorso di una directory
     * @return il numero totale di bytes contenuti nella directory */
    public static long totalSizeQueue(Path p) {
        ConcurrentLinkedQueue<Optional<Path>> queue = new ConcurrentLinkedQueue<>();
        LongAdder size = new LongAdder();  // Per il conteggio dei byte
        Consumer<Path> task = d -> {       // Somma i byte dei file a size e accoda
            try (Stream<Path> list = Files.list(d)) {  // le sub-directory
                for (Path e : list.toArray(Path[]::new)) {
                    if (Files.isDirectory(e, NOL)) {
                        queue.add(Optional.of(e));   // Accoda la sub-directory
                    } else if (Files.isRegularFile(e, NOL))
                        size.add(Files.size(e));     // Aggiorna size
                }
            } catch (IOException ex) { }
            queue.add(Optional.empty());     // Marca la fine del task
        };
        int np = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(np);
        long pending = 0;
        try {
            pending++;
            exec.submit(() -> task.accept(p));
            while (pending > 0) {              // Finché ci sono task pendenti
                Optional<Path> op = queue.poll();  // Il primo elemento in coda
                if (op != null) {            // Se c'è,
                    if (op.isPresent()) {    // ed è relativo a una sub-directory
                        pending++;           // sottomette il relativo task
                        exec.submit(() -> task.accept(op.get()));
                    } else                   // se invece marca la fine di un
                        pending--;           // task, decrementa il contatore
                }
            }
        } finally { exec.shutdown(); }
        return size.sum();
    }

    /** Implementazione tramite Fork-Join di {@link mp.file.Utils#totalSize(Path)}.
     * @param d  percorso di una directory
     * @return il numero totale di bytes contenuti nella directory */
    public static long totalSizeForkJoin(Path d)  {
        long size = 0;
        List<ForkJoinTask<Long>> tasks = new ArrayList<>();
        try (Stream<Path> list = Files.list(d)) {
            for (Path e : list.toArray(Path[]::new)) {
                if (Files.isDirectory(e, NOL)) {
                    tasks.add(ForkJoinTask.adapt(() -> totalSizeForkJoin(e)));
                } else if (Files.isRegularFile(e, NOL))
                    size += Files.size(e);
            }
        } catch (IOException ex) { }
        for (ForkJoinTask<Long> t : ForkJoinTask.invokeAll(tasks))
            size += t.join();
        return size;
    }
}
