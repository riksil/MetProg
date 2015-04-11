package mp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.System.out;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

/** Una classe per fare test su Stream */
public class TestStream {
    /** Ritorna una lista di dipendenti creati con i dati specificati nelle stringhe
     * nomCogStip ognuna delle quali contiene il nomeCognome e lo stipendio di un
     * dipendente. Esempio,
     * <pre>
     *     create("Mario Rossi 1000", "Ugo Gio 1200", "Lia Dodi 1100");
     * </pre>
     * @param nomCogStip  array coi nomeCognome e stipendio dei dipendenti
     * @return lista di dipendenti creati con i dati specificati */
    public static List<Dipendente> create(String...nomCogStip) {
        List<Dipendente> dd = new ArrayList<>();
        for (String d : nomCogStip) {
            String[] tt = d.split(" ");
            dd.add(new Dipendente(String.join(" ", Arrays.copyOf(tt, tt.length-1)),
                    Double.parseDouble(tt[tt.length-1])));
        }
        return dd;
    }

    public static void main(String[] args) throws IOException {
        List<Dipendente> dips = create("Ugo Bo 1000", "Lia La 1200",
                "Ciro Espo 1100", "Lea Gio 1350", "Dea Mia 1400",
                "Ugo Bea 1200");

        // Stampa il numero di dipendenti con uno stipendio di almeno 1200
        long c1200 = 0;
        for (Dipendente d : dips)
            if (d.getStipendio() >= 1200)
                c1200++;
        out.println("# dipendenti con stipendio >= 1200: "+c1200);

        // Stampa il numero di dipendenti con uno stipendio di almeno 1200
        c1200 = dips.stream().filter(d -> d.getStipendio() >= 1200).count();
        out.println("# dipendenti con stipendio >= 1200: "+c1200);

        // Stampa i dipendenti in ordine di stipendio crescente
        Stream<Dipendente> dstream = dips.stream();
        dstream.sorted(comparingDouble(Dipendente::getStipendio))
                .forEachOrdered(out::println);

        //c1200 = dstream.count();           // ERRORE IllegalStateException

        // Stampa gli stipendi
        dips.stream().forEach(d -> out.println(d.getStipendio()));
        out.println();

        // Stampa i valori degli stipendi senza ripetizioni
        dips.stream().map(Dipendente::getStipendio).distinct().forEach(out::println);
        out.println();

        // Stampa gli stipendi oprdinati e senza ripetizioni
        dips.stream().map(Dipendente::getStipendio).distinct()
                .sorted().forEachOrdered(out::println);

        // Stampa il dipendente con il massimo stipendio
        Optional<Dipendente> maxStip = dips.stream()
                .max(comparingDouble(Dipendente::getStipendio));
        out.println("Dipendente con il max stipendio: "+
                (maxStip.isPresent() ? maxStip.get() : "non ci sono dipendenti"));


        Path p = Paths.get("files", "alice_it_utf8.txt");
        List<String> lines = Files.readAllLines(p);

        // Stampa il numero di lnee che contengono la prola "Alice"
        String w = "Alice";
        out.println("Numero linee che contengono "+w+": "+
                lines.stream().filter(l -> l.contains(w)).count());

        // Stampa le prime 10 linee che contengno la parola "Alice"
        try (Stream<String> ll = Files.lines(p)) {
            ll.filter(l -> l.contains(w)).limit(10)
                    .forEach(out::println);
        }

        String txt = new String(Files.readAllBytes(p));
        String[] cc = txt.split("");  // Array di tutti i caratteri

        // Stampa il numero di caratteri distinti
        out.println("Numero caratteri distinti: "+
                Arrays.stream(cc).distinct().count());

        String[] ww = txt.split("[^\\p{IsLetter}]+");  // Array di tutte le parole
        out.println("Numero occorrenze parole: "+ww.length);

        // Stampa il numero di parole distinte
        out.println("Numero parole distinte: "+
                Stream.of(ww).distinct().count());

        // Stampa il numero di parole distinte ignorando maiuscole/minuscole
        out.println("# parole distinte ignorando M/m: "+
                Stream.of(ww).map(String::toLowerCase).distinct().count());

        // Stampa la prola più lunga
        Optional<String> maxParola = Stream.of(ww).max(comparingInt(String::length));
        out.println("Parola più lunga: "+maxParola.get());

        // Raccogli in una lista le 10 parole più lunghe
        List<String> longest = Stream.of(ww).sorted(comparingInt(String::length).reversed())
                .limit(10).collect(toList());
        out.println(longest);

        // Stringa con tutti i caratteri senza ripetizioni
        String chars = Stream.of(txt.split("")).distinct().collect(joining());
        out.println(chars);

        // Statistiche sulla lunghezza delle parole
        Stream<String> ws = Stream.of(ww);                 // Lo Stream delle parole
        DoubleSummaryStatistics stats = ws.collect(summarizingDouble(String::length));
        out.println("Numero parole: "+stats.getCount());
        out.println("Lunghezza:  media = "+stats.getAverage()+
                "  min = "+stats.getMin()+"  max = "+stats.getMax());

        // Mappa delle occorrenze delle parole
        ws = Stream.of(ww);                 // Di nuovo lo Stream delle parole
        Map<String,Integer> counts = ws.collect(toMap(Function.identity(), s -> 1, Integer::sum));
        out.println("Alice: "+counts.get("Alice")+"  Regina: "+counts.get("Regina")+" Re: "+counts.get("Re"));
    }
}
