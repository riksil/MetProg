package mp;

import java.util.Objects;

/** Un oggetto {@code Dipendente} rappresenta un dipendente dell'azienda */
public class Dipendente implements Comparable<Dipendente> {
    /** Mantiene i contatti di un dipendente come indirizzo, telefono, ecc. */
    public static class Contatti {
        /** @return  l'indirizzo del dipendente */
        public String getIndirizzo() { return indirizzo; }

        /** @return  il recapito telefonico del dipendente */
        public String getTelefono() { return telefono; }


        private Contatti() {
            indirizzo = "";
            telefono = "";
        }

        private String indirizzo;
        private String telefono;
    }

    /** Crea un dipendente con i dati specificati. Da usarsi solamente se al
     * dipendente è già stato assegnato un codice.
     * @param nomeCognome  nome e cognome del dipendente
     * @param stipendio  stipendio del dipendente
     * @param codice  codice del dipendente */
    public Dipendente(String nomeCognome, double stipendio, long codice) {
        this.nomeCognome = nomeCognome;
        this.stipendio = stipendio;
        this.codice = codice;
        codiceUsato(codice);          // Comunica che il codice è usato
        contatti = new Contatti();
    }

    /** Crea un dipendente con i dati specificati.
     * @param nomeCognome  nome e cognome del dipendente
     * @param stipendio  stipendio del dipendente */
    public Dipendente(String nomeCognome, double stipendio) {
        this(nomeCognome, stipendio, nuovoCodice());
    }

    /** Crea un dipendente con il dato nome e cognome e lo stipendio a zero.
     * @param nomeCognome  nome e cognome del dipendente */
    public Dipendente(String nomeCognome) {
        this(nomeCognome, 0);
    }

    /** @return i contatti di questo dipendente */
    public Contatti getContatti() { return contatti; }

    /** Imposta l'indirizzo di questo dipendente.
     * @param indirizzo  il nuovo indirizzo
     * @throws NullPointerException  se indirizzo è null */
    public void setIndirizzo(String indirizzo) {
        Objects.requireNonNull(indirizzo, "Indirizzo non può essere null");
        contatti.indirizzo = indirizzo;
    }

    /** Imposta il recapito telefonico di questo dipendente.
     * @param telefono  il nuovo numero di telefono */
    public void setTelefono(String telefono) { contatti.telefono = telefono; }

    /** @return il codice di questo dipendente */
    public final long getCodice() { return codice; }

    /** @return il nome e cognome di questo dipendente */
    public String getNomeCognome() { return nomeCognome; }

    /** @return lo stipendio di questo dipendente */
    public double getStipendio() { return stipendio; }

    /** Imposta un nuovo stipendio per questo dipendente.
     * @param nuovoStipendio  l'importo del nuovo stipendio
     * @throws IllegalArgumentException  se lo stipendio è negativo */
    public void setStipendio(double nuovoStipendio) {
        if (nuovoStipendio < 0)
            throw new IllegalArgumentException("Stipendio non può essere negativo");
        stipendio = nuovoStipendio;
    }

    /** @return il supervisore di questo dipendente */
    public Dipendente getSupevisore() { return supervisore; }

    /** Imposta il supervisore di questo dipendente.
     * @param supervisore  il nuovo supervisore */
    public void setSupervisore(Dipendente supervisore) {
        this.supervisore = supervisore;
    }

    @Override
    public String toString() {
        return getClass().getName()+"[codice="+codice+",nomeCognome="+nomeCognome+"]";
    }

    public int compareTo(Dipendente d) {
        return (codice < d.getCodice() ? -1 :
                (codice > d.getCodice() ? 1 : 0));
    }



    private static long ultimoCodice;     // Ultimo codice usato

    private static long nuovoCodice() {   // Ritorna un nuovo codice
        ultimoCodice++;
        return ultimoCodice;
    }

    // Aggiorna la generazione dei codici tenendo conto che il dato codice è in uso
    private static void codiceUsato(long codice) {
        ultimoCodice = Math.max(ultimoCodice, codice);
    }

    private final long codice;                 // Il codice del dipendente
    private String nomeCognome;
    private double stipendio;
    private Contatti contatti;
    private Dipendente supervisore;   // Inizialmente è null
}
