package mp.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static java.lang.System.out;

/** Classe per testare operazioni relative al web */
public class TestWeb {
    public static void test_loadPage() {
        try {
            String page = Utils.loadPage("https://www.python.org",
                    StandardCharsets.UTF_8);
            out.println("Page length: "+page.length());
        } catch (IOException e) { e.printStackTrace();}
    }

    public static void test_TheLatest(TheLatest[] lts, String[] qq,
                                      BiFunction<TheLatest[],String[],Map<String,List<String>>> get) {
        out.println("servizi: "+lts.length+" interrogazioni: "+qq.length);
        long time = System.currentTimeMillis();
        Map<String, List<String>> results = get.apply(lts, qq);
        out.println(String.format("Tempo: %.2f secondi",
                (System.currentTimeMillis() - time)/1000.0));
        results.forEach((k, l) -> {
            out.println(k);
            l.forEach(out::println);
        });
    }

    public static void main(String[] args) {
        //test_loadPage();
        // I servizi web da interrogare
        TheLatest[] lts = {new TheLatest("Repubblica", StandardCharsets.UTF_8,
                "http://ricerca.repubblica.it/ricerca/repubblica?query=",
                "&sortby=ddate&mode=phrase",
                "<h1>[^<]*<[^>]*>([^<]*)<[^<]*</h1>([^<]*<[^t][^<]*)*<time[^>]*>([^<]*)</time>", 1, 3),
                new TheLatest("Corriere", StandardCharsets.ISO_8859_1,
                        "http://sitesearch.corriere.it/forward.jsp?q=", "#",
                        "<span class=\"hour\">([^<]*)</span>([^<]*<[^h][^<]*)*<h1>[^<]*<[^>]*>([^<]*)<[^<]*</h1>", 3, 1),
                new TheLatest("Il Sole 24ore", StandardCharsets.ISO_8859_1,
                        "http://www.ricerca24.ilsole24ore.com/fc?cmd=static&chId=30&path=%2Fsearch%2Fsearch_engine.jsp&keyWords=%22",
                        "%22&orderByString=Data+desc",
                        "<a[^>]*>([^<]*)</a></div></div><div class=\"box_autore\"><div class=\"autore_text\">[^<0-9]*([^<]*)</div>", 1, 2)};
        // Le interrogazioni
        String[] qq = {"Carlo Padoan","Matteo Renzi","Sofia Loren","Totti",
                "Belen","Barack Obama","Informatica","Nanni Moretti",
                "Federica Pellegrini","Beppe Grillo","spread",
                "debito pubblico","Università"};
        //test_TheLatest(lts, qq, TheLatest::get);
        test_TheLatest(lts, qq, (l, q) -> TheLatest.getParallel(40, l, q));
    }
}
