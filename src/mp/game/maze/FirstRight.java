package mp.game.maze;

import javafx.scene.image.Image;

/** Un giocatore per il gioco Find-Treasure. Per cercare di visitare l'intero
 * labirinto usa la strategia della "mano destra". Se però il labirinto contiene
 * cicli, non è garantito che riesca a visitare l'intero labirinto prima di finire
 * catturato da un ciclo. Questo giocatore può essere aggiunto al gioco, scegliendo
 * il class file di questa classe. */
public class FirstRight extends Player {
    @Override
    public void play(Control c) {
        this.c = c;
        while (true) {
            Event e = tryRight();
            if (e.gameOver()) break;
            while (e.collision()) {
                e = tryLeft();
                if (e.gameOver()) break;
            }
        }
    }

    @Override
    public Image getFace() {
        return new Image(getClass().getResource("ghostBlue.png").toString());
    }


    private Event tryRight() {
        Event e = c.rotRight();
        if (e.gameOver()) return e;
        e = c.goForth();
        return e;
    }

    private Event tryLeft() {
        Event e = c.rotLeft();
        if (e.gameOver()) return e;
        e = c.goForth();
        return e;
    }

    private Control c;
}
