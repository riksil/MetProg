package mp.web;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Un oggetto {@code TheLatest} rappresenta un servizio web per la ricerca e il
 * recupero di informazioni aggiornate. In altri termini, un oggetto
 * {@code TheLatest} può essere interrogato circa un argomento specificato in
 * una stringa (vedi il metodo {@link mp.web.TheLatest#get(String) get()}). Ad
 * esempio, un oggetto {@code TheLatest} potrebbe rappresentare il servizio di
 * ricerca offerto da un quotidiano. L'attuale implementazione è infatti basata
 * proprio su quest'ulitmo tipo di interrogazioni (si veda il costruttore
 * {@link mp.web.TheLatest#TheLatest(String,java.nio.charset.Charset,String,
        String,String,int,int) TheLatest(...)}). */
public class TheLatest {
    /** Ritorna una mappa che associa ad ogni data interrogazione la lista delle
     * risposte ottenute dai servizi web specificati. L'implementazione è
     * sequenziale nel thread di invocazione.
     * @param lts  i servizi web da interrogare
     * @param qq  le interrogazioni
     * @return  una mappa con le risposte alle interrogazioni */
    public static Map<String,List<String>> get(TheLatest[] lts, String...qq) {
        Map<String,List<String>> res = new HashMap<>();
        for (String q : qq) {
            List<String> r = new ArrayList<>();
            for (TheLatest lt : lts) {
                r.add(lt.get(q));
            }
            res.put(q, r);
        }
        return res;
    }

    /** Ritorna una mappa che associa ad ogni data interrogazione la lista delle
     * risposte ottenute dai servizi web specificati. L'implementazione usa il
     * numero di thread specificato.
     * @param nt  numero thread
     * @param lts  i servizi web da interrogare
     * @param qq  le interrogazioni
     * @return  una mappa con le risposte alle interrogazioni */
    public static Map<String, List<String>> getParallel(int nt, TheLatest[] lts,
                                                        String...qq) {
        List<Callable<String>> tasks = new ArrayList<>();
        for (String q : qq)
            for (TheLatest lt : lts)
                tasks.add(() -> q+":"+lt.get(q));
        ExecutorService exec = Executors.newFixedThreadPool(nt);
        Map<String, List<String>> results = new HashMap<>();
        try {
            List<Future<String>> res = exec.invokeAll(tasks);
            for (Future<String> r : res) {
                String s = r.get();
                int i = s.indexOf(":");
                String q = s.substring(0, i), d = s.substring(i + 1);
                results.merge(q, Arrays.asList(d), (l1, l2) -> {
                    List<String> l = new ArrayList<>(l1);
                    l.addAll(l2);
                    return l;
                });
            }
        } catch (InterruptedException | ExecutionException e) {}
        exec.shutdown();
        return results;
    }

    /** Crea un oggetto per effettuare interrogazioni ad un servizio web tale
     * che la specifica dell'URL della pagina di risposta ad una interrogazione
     * {@code q} si ottiene con la concatenazione {@code uS + q + uE}. Inoltre
     * il titolo e la data della più recente notizia si può estrarre dalla
     * pagina di risposta tramite l'espressione regolare {@code re} e il numero
     * del gruppo che cattura il titolo è {@code gT} mentre quello per la data
     * è {@code gD}.
     * @param n  nome del servizio web
     * @param cs  codifica dei caratteri della pagina di risposta
     * @param uS  parte iniziale dell'URL di interrogazione
     * @param uE  parte finale dell'URL di interrogazione
     * @param re  espressione regolare per estrarre titolo e data
     * @param gT  numero del gruppo, in {@code re}, che cattura il titolo
     * @param gD  numero del gruppo, in {@code re}, che cattura la data */
    public TheLatest(String n, Charset cs, String uS, String uE, String re, int gT, int gD) {
        name = n;
        charset = cs;
        urlStart= uS;
        urlEnd = uE;
        regExp = Pattern.compile(re);
        gTitle = gT;
        gDate = gD;
    }

    /** Ritorna la risposta ad una interrogazione a questo servizio web.
     * @param q  una stringa che contiene l'interrogazione
     * @return  la risposta all'interrogazione o null se accade un errore */
    public String get(String q) {
        q = q.replace(" ", "+");
        try {
            String page = Utils.loadPage(urlStart + q + urlEnd, charset);
            Matcher m = regExp.matcher(page);
            String s = "";
            if (m.find()) {
                s += name+": "+m.group(gDate);
                s += " <<"+Utils.clean(m.group(gTitle))+">>";
            }
            return s;
        } catch (IOException e) { return null; }
    }

    private final String name;
    private final Charset charset;
    private final String urlStart, urlEnd;
    private final Pattern regExp;
    private final int gTitle, gDate;
}
