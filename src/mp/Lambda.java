package mp;

import mp.util.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import static java.lang.System.out;

/** Una classe per fare esempi sulle espressioni lambda */
public class Lambda {
    /** Ritorna una mappa che ha come chiavi le stringhe della mappa wm ridotte
     * in minuscolo e i conteggi sono aggiornati sommando quelli che sono
     * relativi a chiavi di wm che sono uguali se ridotte in minuscole.
     * @param wm  una mappa da stringhe a interi
     * @return una mappa che con solo chiavi minuscole ma conteggi preservati */
    public static Map<String,Integer> wordMapLowerCase(Map<String,Integer> wm) {
        Map<String,Integer> wmap = new HashMap<>();
        wm.forEach((s,i) -> wmap.merge(s.toLowerCase(), i, Integer::sum));
        return wmap;
    }

    /** Ritorna la somma degli interi della collezione.
     * @param coll  una colezione di interi
     * @return la somma degli interi della collezione */
    public static int sum(Collection<Integer> coll) {
        int[] counter = {0};
        coll.forEach(i -> counter[0] += i);
        return counter[0];
    }

    /** Ritorna una lista con ogni elemento creato tramite gen in corrispondenza
     * ad ognuno dei valori dell'array specificato.
     * @param gen  per creare un elemento
     * @param array  i valori usati per creare gli elementi
     * @param <E>  tipo degli elementi
     * @param <T>  tipo dei valori
     * @return una lista di elementi creati dai valori dell'array */
    public static <E, T> List<E> createList(Function<T,E> gen, T...array) {
        List<E> list = new ArrayList<>();
        for (T v : array)
            list.add(gen.apply(v));
        return list;
    }

    /** Ritorna un array che contiene tutti gli elementi della collezione.
     * @param coll  una collezione
     * @param gen  per creare un array del tipo giusto
     * @param <T>  il tipo degli elementi dell'array
     * @return un array che contiene tutti gli elementi della collezione */
    public static <T> T[] toArray(Collection<T> coll, IntFunction<T[]> gen) {
        T[] array = gen.apply(coll.size());
        int i = 0;
        for (T x : coll)
            array[i++] = x;
        return array;
    }

    public static void main(String[] args) {
        Predicate<String> pred = (s) -> true;
        pred = s -> true;
        pred = new Predicate() {
            @Override
            public boolean test(Object o) {
                return true;
            }
        };
        BiPredicate<String,Integer> pred2 = (s, i) -> s.length() == i;
        //pred2 = (Integer i, s) -> s.length() == i;  // ERRORE: o tutti o nessuno
        BiPredicate<String,String> eqstr = (s1,s2) -> s1.equals(s2);
        eqstr = String::equals;

        List<Dipendente> dlist = new ArrayList<>();
        dlist.forEach(System.out::println);
        List<String> slist = new ArrayList<>();
        slist.removeIf(Objects::isNull);

        dlist = createList(Dipendente::new, "Mario Rossi","Ugo Gialli",
                "Carla Bo","Lia Dodi","Ciro Espo");
        //dlist.forEach(System.out::println);
        Dipendente[] darr = toArray(dlist, Dipendente[]::new);
        String[] sarr = {"Mela","fragola","arancia","Uva","Pera","pesca"};
        //Arrays.sort(sarr);
        //out.println(Arrays.toString(sarr));
        //Arrays.sort(sarr, String::compareToIgnoreCase);
        out.println(Arrays.toString(sarr));
        Arrays.sort(sarr, Comparator.comparingInt(String::length));
        out.println(Arrays.toString(sarr));
        Arrays.sort(sarr, Comparator.comparingInt(String::length).
                thenComparing(String::compareToIgnoreCase));
        out.println(Arrays.toString(sarr));

        List<String> fruits = Arrays.asList(sarr);
        fruits.replaceAll(String::toUpperCase);
        out.println(fruits);

        try {
            Map<String,Integer> wmap = Utils.wordMap(Paths.get("files",
                    "alice_it_utf8.txt"), "utf8");
            out.println("Numero chiavi: "+wmap.size());
            out.println("Numero occorrenze: "+ sum(wmap.values()));
            Map<String,Integer> wmaplc = wordMapLowerCase(wmap);
            out.println("Numero chiavi: "+wmaplc.size());
            out.println("Numero occorrenze: "+ sum(wmaplc.values()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
