package mp.app;

/** Un {@code Checker} effettua uno specifico controllo di validità su stringhe.
 * Ad esempio, potrebbe controllare che i caratteri di una stringa siano tutti
 * delle lettere, o che la lunghezza sia compresa in un certo range, o che
 * rispetti la sintassi di un indirizzo email, ecc. */
public interface Checker {
    /** Esegue un controllo di validità della stringa e se è valida ritorna
     * {@code null}, altrimenti ritorna una stringa che contiene una spiegazione
     * del perché non è valida.
     * @param s  la stringa da controllare
     * @return {@code null} oppure una spiegazione della non validità */
    String valid(String s);

    /** Come {@link Checker#valid(String)} ma ritorna un booleano.
     * @param s  la stringa da controllare
     * @return {@code true} se la stringa è valida */
    default boolean isValid(String s) {
        return valid(s) == null;
    }

    /** Ritorna un Checker che controlla che la stringa non sia {@code null} e
     * che i suoi caratteri siano in {@code chars} o, se {@code letters} è
     * {@code true}, che siano lettere.
     * @param chars  caratteri permessi
     * @param letters  se {@code true}, sono permesse anche le lettere
     * @return un Checker che controlla che i caratteri siano quelli dati */
    static Checker getCheckChars(String chars, boolean letters) {
        return new Checker() {
            @Override
            public String valid(String s) {
                for (char c : s.toCharArray()) {
                    if (chars.indexOf(c) < 0 && !(letters && Character.isLetter(c)))
                        return "Il carattere "+c+" non è valido";
                }
                return null;
            }
        };
    }

    /** Ritorna un Checker che controlla che la lunghezza della stringa sia
     * compresa tra i limiti specificati.
     * @param min,max  minima e massima lunghezza
     * @return un Checker che controlla che la lunghezza sia nei limiti dati */
    static Checker getCheckLen(int min, int max) {
        return new Checker() {
            @Override
            public String valid(String s) {
                if (s == null) return "Non può essere null";
                int len = s.length();
                if (len < min || len > max)
                    return "La lunghezza deve essere tra "+min+" e "+max;
                return null;
            }
        };
    }

    /** Ritorna un Checker che controlla che la stringa passi tutti i controlli
     * dei Checker specificati. Il metodo valid del Checker ritornato, se la
     * stringa non passa tutti i controlli, ritorna l'errore del primo Checker
     * insoddisfatto.
     * @param cc  i Checker che eseguono i controlli
     * @return un Checker che controlla che tutti i Checker siano soddisfatti */
    static Checker getCheckAll(Checker...cc) {
        return new Checker() {
            @Override
            public String valid(String s) {
                for (Checker c : cc) {
                    String r = c.valid(s);
                    if (r != null) return r;
                }
                return null;
            }
        };
    }
}
