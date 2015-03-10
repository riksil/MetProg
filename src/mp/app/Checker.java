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
}
