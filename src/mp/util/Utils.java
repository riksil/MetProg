package mp.util;

import mp.util.HTree;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.lang.System.out;   // Importa il campo statico out di System

/** Alcuni metodi e costanti di utilità */
public class Utils {
    /** Ritorna la stringa s tra doppi apici.
     * @param s  una stringa
     * @return la stringa s tra doppi apici */
    public static String q(String s) { return "\"" + s + "\""; }

    /** Ritorna true se il primo orario è minore o uguale al secondo. Ogni orario è
     * specificato da un numero di ore, minuti e secondi.
     * @param h1,m1,s1   primo orario
     * @param h2,m2,s2   secondo orario
     * @return true se il primo orario è minore o uguale al secondo */
    public static boolean timeLEQ(int h1, int m1, int s1, int h2, int m2, int s2) {
        if (h1 < h2)
            return true;
        else if (h1 == h2 && m1 < m2)
            return true;
        else if (h1 == h2 && m1 == m2 && s1 <= s2)
            return true;
        else
            return false;
    }

    public static final long KILOBYTE = 1024;
    public static final long MEGABYTE = 1024*KILOBYTE;
    public static final long GIGABYTE = 1024*MEGABYTE;

    /** Ritorna una stringa che descrive il numero di bytes nb in termini di
     * GigaByte, MegaByte, KiloByte e Byte. Ad esempio se nb = 2147535048, ritorna
     * "2GB 50KB 200B".
     * @param nb  numero di bytes
     * @return stringa che descrive nb in GB, MB, KB e B */
    public static String toGMKB(long nb) {
        String s = "";
        if (nb >= GIGABYTE)
            s += nb/GIGABYTE+"GB";
        nb %= GIGABYTE;
        if (nb >= MEGABYTE)
            s += (s.isEmpty() ? "" : " ") + nb/MEGABYTE+"MB";
        nb %= MEGABYTE;
        if (nb >= KILOBYTE)
            s += (s.isEmpty() ? "" : " ") + nb/KILOBYTE+"KB";
        nb %= KILOBYTE;
        if (nb > 0)
            s += (s.isEmpty() ? "" : " ") + nb+"B";
        return s;
    }

    /** Ritorna una stringa ottenuta ripetendo n volte la stringa s. Ad esempio, se
     * s = "Tre" e n = 3, ritorna "TreTreTre".
     * @param s  stringa da ripetere
     * @param n  numero di ripetizioni
     * @return una stringa uguale alla ripetizione n volte di s */
    public static String rep(String s, int n) {
        String r = "";
        for (int i = 0 ; i < n ; i++)
            r += s;
        return r;
    }

    /** Ritorna una stringa con la sequenza dei caratteri inversa rispetto a quella
     * di s. Ad esempio, se s = "rovescio", ritorna "oicsevor".
     * @param s  una stringa
     * @return la stringa rovesciata */
    public static String reverse(String s) {
        String r = "";
        for (int i = s.length()-1 ; i >= 0 ; i--)
            r += s.charAt(i);
        return r;
    }

    /** Ritorna true se n è un numero primo.
     * @param n  un intero
     * @return true se n è primo */
    public static boolean prime(long n) {
        if (n <= 1) return false;
        long d = 2;
        while (d*d <= n && n%d != 0) d++;
        return d >= n || n%d != 0;
    }

    /** Costanti che specificano i diversi tipi di allineamento. */
    public static enum Align { LEFT, RIGHT, CENTER, CENTRE }

    /** Ritorna la stringa s allineata secondo {@code a} in un campo di lunghezza
     * len riempendo la lunghezza mancante con spazi. Ad esempio, se s = "pippo",
     * len = 10 e a = Align.CENTER, ritorna "  pippo   ".
     * @param s  una stringa
     * @param len  lunghezza campo
     * @param a  allineamento
     * @return la stringa s allineata */
    public static String align(String s, int len, Align a) {
        int ns = len - s.length();     // Numero spazi da inserire
        switch (a) {
            case LEFT:
                return s + rep(" ", ns);
            case RIGHT:
                return rep(" ", ns) + s;
            case CENTER:case CENTRE:
                return rep(" ", ns/2) + s + rep(" ", ns - ns/2);
        }
        return s;    // Solamente per evitare warning del compilatore
    }


    /** Ritorna un array che contiene gli interi distinti e ordinati letti da uno
     * Scanner.
     * @param scan  lo Scanner da cui leggere gli interi
     * @return un array che contiene gli interi distinti e ordinati letti dallo
     * Scanner */
    public static int[] readDistinct(Scanner scan) {
        int[] ints = new int[0];
        while (scan.hasNextInt()) {      // Finchè c'è un prossimo intero da leggere
            int n = scan.nextInt();      // Leggi il prossimo intero
            if (Arrays.binarySearch(ints, n) < 0) {   // Se non è già presente
                ints = Arrays.copyOf(ints, ints.length + 1);  // Estendi l'array,
                ints[ints.length - 1] = n;                    // aggiungi il nuovo
                Arrays.sort(ints);                            // e ordina l'array
            }
        }
        return ints;
    }

    /** Ritorna {@code true} se la stringa contiene solamente lettere secondo il
     * metodo {@link java.lang.Character#isLetter}.
     * @param s  una stringa
     * @return {@code true} se la stringa contiene solamente lettere */
    public static boolean isWord(String s) {
        for (int i = 0 ; i < s.length() ; i++)
            if (!Character.isLetter(s.charAt(i)))
                return false;
        return true;
    }

    /** Ritorna l'insieme delle sottostringhe di una data stringa che hanno una
     * data lunghezza e sono composte solamente da lettere.
     * @param s  una stringa
     * @param len  lunghezza delle sottostringhe
     * @return l'insieme delle sottostringhe */
    public static Set<String> subwords(String s, int len) {
        Set<String> subSet = new HashSet<>();
        for (int i = 0 ; i <= s.length() - len ; i++) {
            String sub = s.substring(i, i + len);
            if (isWord(sub)) subSet.add(sub);
        }
        return subSet;
    }

    /** Ritorna una mappa che ad ogni sotto-stringa della stringa data della
     * lunghezza specificata associa il numero di occorrenze.
     * @param s  una stringa
     * @param len  lunghezza delle sottostringhe
     * @return  mappa che conta le occorrenze delle sotto-stringhe */
    public static Map<String,Integer> subwordsCount(String s, int len) {
        Map<String, Integer> count = new HashMap<>();
        for (int i = 0 ; i <= s.length() - len ; i++) {
            String sub = s.substring(i, i + len);
            if (isWord(sub)) {
                if (count.containsKey(sub))
                    count.put(sub, count.get(sub)+1);
                else
                    count.put(sub, 1);
            }
        }
        return count;
    }

    /** Ritorna una mappa che ad ogni parola del file specificato associa il
     * numero di occorrenze. Per parola si intende una sequenza di lettere
     * (riconosciute dal metodo {@link java.lang.Character#isLetter(char)}) di
     * lunghezza massimale. Le parole sono sensibili alle maiuscole/minuscole.
     * @param path  il percorso del file
     * @param charset  il charset per decodificare i caratteri
     * @return  una mappa che conta le occorenze delle parole
     * @throws IOException se si verifica un errore accedendo al file */
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



    public static void main(String[] args) {
        /*
        test_toGMKB();
        test_timeLEQ();
        test_reverse_rep();
        test_prime();
        test_align();
        test_readDistinct();
        */
        test_subwords();
        test_subwordsCount();
        test_HTree();
    }



    private static boolean timeLEQ2(int h1, int m1, int s1, int h2, int m2, int s2) {
        return h1 < h2 || h1 == h2 && m1 < m2 || h1 == h2 && m1 == m2 && s1 <= s2;
    }

    private static void test_toGMKB() {
        Scanner input = new Scanner(System.in);
        out.print("Test metodo toGMKB(), digita un numero di bytes: ");
        long nb = input.nextLong();
        out.println("toGMKB(" + nb + ") --> " + q(toGMKB(nb)));
    }

    private static void test_timeLEQ() {
        Scanner input = new Scanner(System.in);
        out.print("Test metodo timeLEQ(), digita due orari (h m s): ");
        int h1 = input.nextInt(), m1 = input.nextInt(), s1 = input.nextInt();
        int h2 = input.nextInt(), m2 = input.nextInt(), s2 = input.nextInt();
        out.println("Il primo orario " + (timeLEQ(h1, m1, s1, h2, m2, s2) ? "" :
                "non ") + "è minore o uguale al secondo");
    }

    private static void test_reverse_rep() {
        Scanner input = new Scanner(System.in);
        out.print("Test metodi rep() e reverse(), digita una stringa: ");
        String s = input.nextLine();
        out.println("reverse(" + q(s) + ") --> " + q(reverse(s)));
        out.print("Digita un intero: ");
        int n = input.nextInt();
        out.println("rep(" + q(s) + ", "+n+") --> " + q(rep(s, n)));
    }

    private static void test_prime() {
        Scanner input = new Scanner(System.in);
        out.print("Test metodo prime(), digita un intero: ");
        long n = input.nextLong();
        out.println("prime(" + n + ") --> " + prime(n));
    }

    private static void test_align() {
        Scanner input = new Scanner(System.in);
        out.print("Test metodo align(), digita una stringa e un intero: ");
        String s = input.nextLine();
        int len = input.nextInt();
        for (Align a : Align.values())
            out.println("align("+ q(s) + ", " + len + ", " + a + ") --> " +
                    q(align(s, len, a)));
    }

    private static void test_readDistinct() {
        Scanner input = new Scanner(System.in);
        out.print("Test metodo readDistinc(), digita una serie di interi,\n"+
                "per terminare digita un qualsiasi carattere non numerico: ");
        int [] ints = readDistinct(input);
        out.println(Arrays.toString(ints));
    }

    private static void test_subwords() {
        Scanner input = new Scanner(System.in);
        out.print("Test metodo subwords(), digita una linea di testo: ");
        String line = input.nextLine();
        out.print("Digita la lunghezza delle sotto-stringhe: ");
        int len = input.nextInt();
        out.println(subwords(line, len));
    }

    private static void test_subwordsCount() {
        Scanner input = new Scanner(System.in);
        out.print("Test metodo subwordsCount(), digita una linea di testo: ");
        String line = input.nextLine();
        out.print("Digita la lunghezza delle sotto-stringhe: ");
        int len = input.nextInt();
        out.println(subwordsCount(line, len));
    }

    private static void test_HTree() {
        HTree<String> tree = new HTree<>("Computer Science");
        tree.add("Computer Science", "Software", "Hardware");
        tree.add("Hardware", "Memory", "Processor", "Architecture");
        tree.add("Software", "Operating System", "Data Base", "Word Processing",
                "Image Processing", "Algorithms", "Languages");
        tree.add("Languages", "Procedural", "Functional", "Object Oriented");
        tree.add("Procedural", "C", "Pascal");
        tree.add("Object Oriented", "C++", "Java", "Smalltalk");
        tree.add("Data Base", "SQL", "Data Mining");
        tree.add("Operating System", "Unix", "Linux", "MacOS X");
        out.println(tree.toFullString());
    }
}
