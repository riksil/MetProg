package mp;

/** Un oggetto {@code Dirigente} rappresenta un dirigente dell'azienda */
public class Dirigente extends Dipendente {
    /** Crea un dirigente con il dato nome e cognome e bonus.
     * @param nomeCognome  nome e cognome del dirigente
     * @param bonus  bonus del dirigente */
    public Dirigente(String nomeCognome, double bonus) {
        super(nomeCognome);
        this.bonus = bonus;
    }

    /** @return lo stipendio di questo dirigente */
    @Override
    public double getStipendio() {
        return super.getStipendio() + bonus;
    }

    /** Imposta il supervisore di questo dirigente.
     * @param supervisore  il supervisore
     * @throws IllegalArgumentException se il supervisore non è un dirigente */
    @Override
    public void setSupervisore(Dipendente supervisore) {
        if (!(supervisore instanceof Dirigente))
            throw new IllegalArgumentException("Il supervisore di un dirigente deve essere un dirigente");
        super.setSupervisore(supervisore);
    }

    /** @return il bonus di questo dirigente */
    public double getBonus() { return bonus; }

    /** Imposta il bonus di questo dirigente.
     * @param bonus  il nuovo bouns */
    public void setBonus(double bonus) {
        this.bonus = bonus;
    }



    private double bonus;
}
