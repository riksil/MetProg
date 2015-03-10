package mp;

import mp.app.Checker;
import mp.tapp.MenuApp;

import java.util.Arrays;
import java.util.Scanner;
import static java.lang.System.out;

/** Una semplice applicazione con menu testuale per gestire un archivio
 * dipendenti. */
public class AziendaApp extends MenuApp {
    public static void main(String[] args) {
        AziendaApp app = new AziendaApp();
        app.run();
    }

    public AziendaApp() {
        super("Nuovo...", "Rimuovi...", "Cerca...", "Tutti");
        dipendenti = new Dipendente[0];
    }

    @Override
    protected void doMenu(int choice) {
        switch (choice) {
            case 1: nuovo(); break;
            case 2: rimuovi(); break;
            case 3: cerca(); break;
            case 4: tutti(); break;
        }
    }

    /** Legge dalla console i dati di un nuovo dipendente e lo aggiunge
     * all'archivio. */
    private void nuovo() {
        Scanner input = new Scanner(System.in);
        String nc = inputString(input, "Nome e cognome: ", CHECK_NOMECOGNOME);
        String ind = inputString(input, "Indirizzo: ", CHECK_INDIRIZZO);
        String tel = inputString(input, "Telefono: ", CHECK_TELEFONO);
        Dipendente d = new Dipendente(nc);
        d.setIndirizzo(ind);
        d.setTelefono(tel);
        dipendenti = Arrays.copyOf(dipendenti, dipendenti.length + 1);
        dipendenti[dipendenti.length-1] = d;
        out.println("Il dipendente "+nc+" è stato inserito");
    }

    /** Legge dalla console un codice e rimuove dall'archivio il dipendente con
     * quel codice, se esiste. */
    private void rimuovi() {
        out.println("Non ancora implementato");    // Lasciato come esercizio
    }

    /** Legge dalla console una stringa e stampa i dependenti che contengono nel
     * loro nome e cognome la stringa letta. */
    private void cerca() {
        out.println("Non ancora implementato");    // Lasciato come esercizio
    }

    /** Stampa sulla console i dati di tutti i dipendenti nell'archivio */
    private void tutti() {
        for (Dipendente d : dipendenti) {
            out.println(d.getCodice()+"  "+d.getNomeCognome());
            Dipendente.Contatti c = d.getContatti();
            out.println("  Indirizzo: "+c.getIndirizzo()+" Tel: "+c.getTelefono());
        }
    }

    private Dipendente[] dipendenti;    // Mantiene l'archivo dei dipendenti


    private static class CheckNomeCognome implements Checker {
        @Override
        public String valid(String s) {
            String r = checkString(s, " '", true);
            return (r != null ? r : (s.isEmpty() ? "Non può essere vuoto" : null));
        }
    }

    private static class CheckIndirizzo implements Checker {
        @Override
        public String valid(String s) {
            return checkString(s, " ,.'0123456789", true);
        }
    }

    private static class CheckTelefono implements Checker {
        @Override
        public String valid(String s) {
            return checkString(s, " 0123456789", false);
        }
    }

    /** Controlla che la stringa data non sia {@code null} e che i suoi caratteri
     * siano in {@code chars} o, se {@code letters} è {@code true}, che siano
     * lettere.
     * @param s  la stringa da controllare
     * @param chars  caratteri permessi
     * @param letters  se {@code true}, sono permesse anche le lettere
     * @return  null se la stringa è valida, altrimenti una stringa con la
     * spiegazione dell'errore */
    private static String checkString(String s, String chars, boolean letters) {
        for (char c : s.toCharArray()) {
            if (chars.indexOf(c) < 0 && (!letters || !Character.isLetter(c)))
                return "Il carattere "+c+" non è valido";
        }
        return null;
    }

    /** Legge dallo {@link java.util.Scanner} {@code input} una stringa finché non
     * passa il controllo del {@link mp.app.Checker} {@code check}.
     * @param input  scanner da cui leggere
     * @param prompt  la descrizione della stringa da leggere
     * @param check  il controllo
     * @return la prima stringa letta che passa il controllo */
    private static String inputString(Scanner input, String prompt, Checker check) {
        while (true) {
            out.print(prompt);
            String s = input.nextLine();
            String r = check.valid(s);
            if (r == null)
                return s;
            else
                out.println(r);
        }
    }

    private static final Checker CHECK_NOMECOGNOME = new CheckNomeCognome();
    private static final Checker CHECK_INDIRIZZO = new CheckIndirizzo();
    private static final Checker CHECK_TELEFONO = new CheckTelefono();
}
