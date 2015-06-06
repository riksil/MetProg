package mp.game.maze;

import javafx.scene.image.Image;

/** Interfaccia che deve essere implementata da un giocatore per il gioco
 * Find-Treasure */
public abstract class Player {
    /** L'evento causato da un'azione del giocatore o avvenuto nel momento
     * dell'esecuzione dell'azione. */
    public interface Event {
        /** Ritorna true se l'azione del giocatore ha prodotto una collisione con
         * un altro giocatore o con un ostacolo. Se l'azione produce una
         * collisione, non viene eseguita.
         * @return true se l'azione ha prodotto una collisione */
        boolean collision();

        /** Ritorna true se il gioco è terminato. In risposta a questo evento il
         * giocatore deve terminare la propria esecuzione.
         * @return true se il gioco è terminato */
        boolean gameOver();
    }

    /** L'oggetto che è passato al giocatore quando inizia il gioco che gli
     * permette di comandare le proprie azioni. I metodi sono bloccanti, nel
     * senso che sono eseguiti solamente in corrispondenza ad ogni frame del
     * gioco */
    public interface Control {
        /** Va avanti di un passo, se possibile.
         * @return l'evento relativo all'azione */
        Event goForth();

        /** Ruota di 90 gradi a destra. Questa azione è sempre eseguita.
         * @return l'evento relativo all'azione */
        Event rotRight();

        /** Ruota di 90 gradi a sinistra. Questa azione è sempre eseguita.
         * @return l'evento relativo all'azione */
        Event rotLeft();
    }

    /** Invocato solamente quando inizia il gioco. Il giocatore deve implementare
     * le sue azioni in questo metodo tramite un ciclo che deve però interrompersi
     * non appena è ritornato un evento che segnala la fine del gioco.
     * @param c l'oggetto che permette il comando delle azioni di gioco */
    public abstract void play(Control c);

    /** Ritorna eventualmente un'immagine che rappresenta il giocatore.
     * @return l'immagine del giocatore o null */
    public Image getFace() { return null; }
}
