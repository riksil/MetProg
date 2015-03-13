package mp;

import java.util.Objects;

/*
class Pair<T> {
    public Pair(T f, T s) {
        first = f;
        second = s;
    }

    public T getFirst() { return first; }
    public void setFirst(T first) { this.first = first; }

    public T getSecond() { return second; }

    public void setSecond(T second) { this.second = second; }

    private T first, second;
}
*/

/** Un oggetto {@code DPair} rappresenta una coppia di valori di tipi anche
 * diversi.
 * @param <F>  tipo del primo valore della coppia
 * @param <S>  tipo del secondo valore della coppia */
class DPair<F,S> {
    public DPair(F f, S s) {
        first = f;
        second = s;
    }

    public F getFirst() { return first; }
    public void setFirst(F first) { this.first = first; }

    public S getSecond() { return second; }
    public void setSecond(S second) { this.second = second; }

    private F first;
    private S second;
}

class Pair<T> extends DPair<T,T> {
    public Pair(T f, T s) {
        super(f, s);
    }
}

/** Una classe per fare test sulla genericità */
public class Generic {
    public static <T extends Comparable<T>> T min(T[] a) {
        T min = a[0];
        for (T v : a)
            if (v.compareTo(min) < 0)
                min = v;
        return min;
    }

    /** Ritorna il primo indice di un elemento dell'array che è uguale a v
     * o -1 se non c'è.
     * @param a  un array
     * @param v  il valore da cercare
     * @param <T>  variabile di tipo
     * @return il primo indice di un elemento uguale a v o -1 */
    public static <T> int find(T[] a, T v) {
        for (int i = 0 ; i < a.length ; i++)
            if (Objects.equals(a[i], v))
                return i;
        return -1;
    }

    /** Metodo generico che riempe l'array con il valore dato.
     * @param a  un array
     * @param v  valoredi riempimento
     * @param <T>  variabile di tipo */
    public static <T> void fill(T[] a, T v) {
        for (int i = 0 ; i < a.length ; i++)
            a[i] = v;
    }

    /** Metodo generico che ritorna l'elemento dell'array con la più lunga
     * stringa ritornata da {@code toString}.
     * @param a  un array
     * @param <T>  variabile di tipo
     * @return l'elemento dell'array con la più lunga {@code toString} */
    public static <T> T longestStr(T[] a) {
        T val = null;
        int max = 0;
        for (T v : a)
            if (v != null && v.toString().length() > max) {
                val = v;
                max = v.toString().length();
            }
        return val;
    }

    public static void main(String[] args) {
        Pair<String> pS = new Pair<>("A", "B");
        pS.setFirst("BB");
        //pS.setFirst(2);     // ERRORE in compilazione
        Pair<Integer> pI = new Pair<>(1,2);
        //pI.setSecond("A");  // ERRORE in compilazione
        Pair<Pair<String>> ppS = new Pair<>(new Pair<>("a","b"),
                new Pair<>("c","d"));

        Pair<Dipendente> dP = new Pair<>(new Dipendente("M. Rossi"), new Dipendente("Ugo Gialli"));
        Pair<Dirigente> dirP = new Pair<>(new Dirigente("Carla Bo", 100), new Dirigente("Lia la", 200));
        //dP = dirP;                 // ERRORE in compilazione
        dP.setFirst(new Dipendente("Giogio Verdi"));
        dirP.getFirst().getBonus();

        DPair<String,String> pDS = pS;
        String[] strA = new String[] {"A", "0", ""};
        String s = min(strA);
        Dipendente[] dA = new Dipendente[] {new Dipendente("Mario Rossi")};
        Dipendente d = min(dA);
        Dirigente[] dirA = new Dirigente[2];
        //Dirigente dir = min(dirA);      // ERRORE in compilazione
        Pair<String>[] psA; // = new Pair<String>[1];


        /*
        String[] strA = new String[] {"A","B","C"};
        Integer[] intA = new Integer[] {1,2,3,4};
        int[] iA = new int[] {1,2,3};
        int k = find(strA, "D");  //tipo inferito: String
        k = find(intA, 4);        //tipo inferito: Integer
        //k = find(iA, 3);
        k = find(strA, 3);
        //k = Generic.<String>find(strA, 3);

        fill(strA, "X");
        //Generic.<String>fill(intA, "A");

        String s = longestStr(strA);
        //k = longestStr(strA);
        */
    }
}
