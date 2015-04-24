package mp.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

/** Metodi di utilità per il Web */
public class Utils {
    /** Ritorna la stringa ottenuta dalla sequenza dei bytes del flusso dato
     * decodificando i caratteri tramite la codifica specificata. I fine linee
     * sono sostituiti con '\n'.
     * @param in  un flusso di bytes
     * @param cs  la codifica per i caratteri
     * @return una stringa che contiene i caratteri decodificati dal flusso */
    public static String read(InputStream in, Charset cs) {
        BufferedReader r = new BufferedReader(new InputStreamReader(in, cs));
        return r.lines().collect(Collectors.joining("\n"));
    }

    /** Ritorna una stringa con il contenuto della pagina localizzata dall'URL
     * dato usando la codifica per i caratteri specificata.
     * @param url  una stringa contenente un URL
     * @param cs  la codifica per i caratteri della pagina
     * @return  il contenuto della pagina come stringa
     * @throws IOException se accade un errore durante la connessione remota */
    public static String loadPage(String url, Charset cs) throws IOException {
        URL urlO = new URL(url);
        URLConnection urlC = urlO.openConnection();
        urlC.setRequestProperty("User-Agent", "Mozilla/5.0");
        urlC.setRequestProperty("Accept", "text/html;q=1.0,*;q=0");
        urlC.setRequestProperty("Accept-Encoding", "identity;q=1.0,*;q=0");
        urlC.setReadTimeout(10000);
        urlC.setConnectTimeout(10000);
        urlC.connect();
        return read(urlC.getInputStream(), cs);
    }

    /** Ritorna la stringa normalizzata (sostituzione di tutti i whitespaces con
     * lo spazio e riduzione di due o più spazi consecutivi ad uno solo) e con
     * sostituzione delle più comuni HTML character references.
     * @param s  una stringa
     * @return la stringa normalizzata e con sostituzione delle più comuni
     * HTML character references */
    public static String clean(String s) {
        s = s.replaceAll("\\s+", " ");
        for (String[] cr : CHAR_REFS)
            s = s.replace("&"+cr[0]+";", cr[1]);
        return s;
    }

    /** Sostituzioni per le più comuni HTML character references */
    private static final String[][] CHAR_REFS = {{"amp","&"},{"laquo","\""},
            {"raquo","\""},{"quot","\""},{"egrave","è"},{"Egrave","È"},
            {"eacute","é"},{"agrave","à"},{"ograve","ò"},{"igrave","ì"},
            {"ugrave","ù"},{"deg","°"}};
}
