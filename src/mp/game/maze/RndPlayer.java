package mp.game.maze;

import javafx.scene.image.Image;

import java.util.Random;

/** Un giocatore per il gioco Find-Treasure. Non usa alcuna strategia, semplicemente
 * ogni volta fa una mossa random */
public class RndPlayer extends Player {
    @Override
    public void play(Control c) {
        Random rnd = new Random();
        Event e = null;
        while (true) {
            switch(rnd.nextInt(3)) {
                case 0: e = c.goForth(); break;
                case 1: e = c.rotRight(); break;
                case 2: e = c.rotLeft(); break;
            }
            if (e.gameOver()) break;
        }
    }

    @Override
    public Image getFace() {
        return new Image(getClass().getResource("ghostRed.png").toString());
    }
}
