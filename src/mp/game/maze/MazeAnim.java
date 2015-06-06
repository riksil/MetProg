package mp.game.maze;

import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import mp.game.util.Maze;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static mp.game.util.MazeUtils.Dir;
import static mp.game.util.MazeUtils.Pos;

/** Gestisce un gioco di Find-Treasure. Crea un labirinto random, posiziona il
 * tesoro in una cella random del labirinto e poi anima i giocatori. Il gioco
 * termina quando uno dei giocatori trova il tesoro. I giocatori possono essere
 * aggiunti anche quando il gioco è già iniziato.
 * <br>
 * <br>
 * L'implemntazione ad ogni frame esegue le azioni di tutti i giocatori. Ogni
 * giocatore è eseguito in un suo thread dedicato. Ogni volta che un giocatore
 * invoca un'azione, il metodo dell'azione si blocca fino a che il prossimo
 * frame non viene completato. In questo modo ogni giocatore può eseguire al più
 * una sola azione per frame. Inoltre, grazie la fatto che ogni giocatore è
 * eseguito in un thread distinto, non si corre il rischio che l'esecuzione di
 * un giocatore blocchi il gioco. Se un giocatore impiega troppo tempo per
 * decidere la prossima azione, semplicemente salta il turno. */
public class MazeAnim {
    /** Durata in millisecondi di un frame */
    public static final int FRAME_MS = 10;

    /** Crea un gestore per il gioco Find-Treasure con il labirinto specificato.
     * Non inizia il gioco.
     * @param m  un labirinto */
    public MazeAnim(Maze m) {
        maze = m;
        arena = new Group(maze.draw());
        ImageView treasureImg = getImage(new Image(getClass()
                .getResource("treasure.png").toString()));
        treasure = maze.rndFreeCell(null);
        move(treasureImg, treasure);
        arena.getChildren().add(treasureImg);
        anim = new Timeline(new KeyFrame(new Duration(FRAME_MS), e -> update()));
        anim.setCycleCount(Timeline.INDEFINITE);
        controls = new ArrayList<>();
        frameCounter = 0;
        started = false;
        winner = null;
        gameOverCountdown = -1;
    }

    /** @return il Node che contiene il labirinto del gioco */
    public synchronized Node getNode() { return arena; }

    /** Aggiunge il giocatore dato al gioco. Può essere invocato anche durante
     * l'esecuzione del gioco. La posizione iniziale del nuovo giocatore è scelta
     * in modo random tra quelle libere.
     * @param p  un giocatore */
    public synchronized void add(Player p) {
        Node node = null;
        Runnable anim = null;
        Image im = p.getFace();
        if (im != null) {
            ImageView imV = getImage(im);
            anim = () -> animImage(imV);
            node = imV;
        } else {
            // TODO: Se il giocatore non fornisce un'immagine...
        }
        Pos rp = maze.rndFreeCell(controls.stream().map(AnimControl::getPos)
                .collect(Collectors.toSet()));
        if (rp == null) {
            // TODO: Se non ci sono posizioni libere...
        }
        move(node, rp);
        Dir d = Dir.values()[RND.nextInt(Dir.values().length)];
        AnimControl c = new AnimControl(this, p, node, anim, rp, d);
        controls.add(c);
        arena.getChildren().add(node);
        if (started) c.start();   // Se il gioco è iniziato, inizia il nuovo giocatore
    }

    /** Inizia il gioco. Più precsiamente inizia l'esecuzione dei frame del gioco
     * e i thread dei giocatori. Ad ogni frame lo stato del gioco è aggiornato
     * considerando le azioni dei giocatori. Il gioco termina normalmente quando
     * un giocatore trova il tesoro. */
    public synchronized void start() {
        if (gameOverCountdown < 0 && !started) {
            started = true;
            controls.forEach(AnimControl::start);
            anim.play();
        }
    }

    /** Termina il gioco. L'esecuzione dei frame non termina immediatamente. */
    public synchronized void gameOver() {
        if (gameOverCountdown < 0)
            gameOverCountdown = 10;       // Numero frame per la terminazione
    }


    /** Aggiornamento effettuato ad ogni frame */
    private synchronized void update() {
        if (gameOverCountdown >= 0) {                // Se il gioco sta terminando
            if (winner != null) {
                Runnable r = winner.getAnim();
                if (r != null) r.run();
                winner = null;
            }
            Event gOver = new Event(false, true);
            for (AnimControl c : controls)
                c.setState(gOver, null, null);
            if (gameOverCountdown == 0) anim.stop();
            else gameOverCountdown--;
            notifyAll();
        } else if (frameCounter % SMOOTHNESS == 0) {  // Frame di aggiornamento del gioco
            Set<Pos> currPos = controls.stream().map(AnimControl::getPos)
                    .collect(Collectors.toSet());
            Set<Pos> nextPos = new HashSet<>();
            for (AnimControl c : controls) {
                Move m = c.consumeMove();
                if (m == null) continue;
                switch (m) {
                    case GO: updateGo(c, currPos, nextPos); break;
                    case RIGHT: updateRot(c, m); break;
                    case LEFT: updateRot(c, m); break;
                    default: break;
                }
            }
            // Notifica che il frame è stato eseguito. Tutti i thread dei giocatori
            // che erano in attesa sono risvegliati e riprenderanno l'esecuzione
            // non appena riotterrano il lock su questo oggetto.
            notifyAll();
        } else if (frameCounter % SMOOTHNESS > 0) {   // Frame per la resa dei movimenti
            int i = (int) (frameCounter % SMOOTHNESS) + 1;
            for (AnimControl c : controls) {
                Consumer<Integer> m = c.getMove();
                if (m != null) m.accept(i);
                if (i == SMOOTHNESS) c.setMove(null);
            }
        }
        frameCounter++;
    }


    /** Ritorna un Node per l'immagine di un giocatore.
     * @param im  un'immagine
     * @return un Node per l'immagine di un giocatore */
    private ImageView getImage(Image im) {
        ImageView imV = new ImageView(im);
        int cs = maze.cellSize();
        imV.setPreserveRatio(true);
        imV.setFitWidth(cs);
        imV.setFitHeight(cs);
        return imV;
    }

    /** Sposta un Node nella posizione specificata del labirinto.
     * @param node  un Node
     * @param to  una posizione */
    private void move(Node node, Pos to) {
        Point2D p2D = maze.rectUpperLeft(to.row, to.col);
        node.setTranslateX(p2D.getX());
        node.setTranslateY(p2D.getY());
    }

    /** Muove un Node lungo il tragitto tra due posizioni del labirinto nella
     * posizione intermedia detrminata dall'indice dato.
     * @param node  un Node
     * @param from  posizione di partenza
     * @param to  posizione di arrivo
     * @param i  indice posizione intermedia */
    private void move(Node node, Pos from, Pos to, int i) {
        Point2D from2D = maze.rectUpperLeft(from.row, from.col);
        Point2D to2D = maze.rectUpperLeft(to.row, to.col);
        double x = from2D.getX(), y = from2D.getY();
        double xx = to2D.getX(), yy = to2D.getY();
        double k = ((double)i)/SMOOTHNESS;
        node.setTranslateX(xx*k + x*(1 - k));
        node.setTranslateY(yy*k + y*(1 - k));
    }

    /** Ruota un Node di un angolo intermedio tra un angolo di partenza e uno di
     * arrivo.
     * @param node  un Node
     * @param from  angolo di partenza
     * @param to  angolo di arrivo
     * @param i  indice che determina un angolo intermedio */
    private void rot(Node node, double from, double to, int i) {
        double k = ((double)i)/ SMOOTHNESS;
        node.setRotate(to*k + from*(1 - k));
    }

    /** Aggiorna lo stato di un giocatore a seguito di un'azione di goForth da
     * una posizione currPos alla poszione nextPos.
     * @param c  il controllo di un giocatore
     * @param currPos  posizione attuale
     * @param nextPos  posizione in cui vorrebbe andare il giocatore */
    private void updateGo(AnimControl c, Set<Pos> currPos, Set<Pos> nextPos) {
        Dir d = c.getDir();
        Pos p = c.getPos();
        Pos pp = p.go(d);
        if (!currPos.contains(pp) && !nextPos.contains(pp) &&
                maze.pass(p, d)) {
            Node node = c.getNode();
            move(node, p, pp, 1);
            currPos.remove(p);
            nextPos.add(pp);
            c.setState(new Event(), pp, null);
            c.setMove(i -> move(node, p, pp, i));
            if (pp.equals(treasure)) {
                winner = c;
                gameOver();
            }
        } else
            c.setState(new Event(true), null, null);
    }

    /** Aggiorna lo stato di un giocatore a seguito di una rotazione.
     * @param c  il controllo di un giocatore
     * @param m  la mossa di rotazione */
    private void updateRot(AnimControl c, Move m) {
        Dir d = c.getDir();
        Dir dd = m == Move.LEFT ? d.left() : d.right();
        double from = d.rot(), to = dd.rot();
        Node node = c.getNode();
        rot(node, from, to, 1);
        c.setMove(i -> rot(node, from, to, i));
        c.setState(new Event(), null, dd);
    }

    /** Imposta e inizia l'animazione del Node immagine di un giocatore dopo che
     * ha vinto.
     * @param imV  l'immagine di un giocatore */
    private void animImage(ImageView imV) {
        Bounds b = arena.getBoundsInLocal();
        double aW = b.getWidth(), aH = b.getHeight();
        imV.setFitWidth(aW/2);
        imV.setFitHeight(aH/2);
        b = imV.getBoundsInLocal();
        double w = b.getWidth(), h = b.getHeight();
        imV.setTranslateX((aW - w)/2);
        imV.setTranslateY((aH - h) / 2);
        imV.toFront();
        RotateTransition rt = new RotateTransition(Duration.millis(300), imV);
        rt.setFromAngle(-45);
        rt.setToAngle(45);
        rt.setCycleCount(8);
        rt.setAutoReverse(true);
        rt.play();
    }

    /** le possibili mosse di un giocatore */
    private enum Move { GO, RIGHT, LEFT }

    /** Implementazione dell'interfaccia Event per il controllo di un giocatore */
    private static class Event implements Player.Event {
        Event(boolean c, boolean o) {
            coll = c;
            gOver = o;
        }
        Event(boolean c) { this(c, false); }
        Event() { this(false); }

        @Override
        public boolean collision() { return coll; }
        @Override
        public boolean gameOver() { return gOver; }

        private final boolean coll, gOver;
    }

    /** Implementazione del controllo di un giocatore */
    private static class AnimControl implements Player.Control {
        AnimControl(MazeAnim boss, Player player, Node n, Runnable a,
                    Pos p, Dir d) {
            this.boss = boss;
            node = n;
            anim = a;
            pos = p;
            dir = d;
            thread = new Thread(() -> player.play(this));
            thread.setDaemon(true);
        }

        @Override
        public Player.Event goForth() { return action(Move.GO); }
        @Override
        public Player.Event rotRight() { return action(Move.RIGHT); }
        @Override
        public Player.Event rotLeft() { return action(Move.LEFT); }


        void start() { thread.start(); }

        Pos getPos() { synchronized (boss) { return pos; } }

        Dir getDir() { synchronized (boss) { return dir; } }

        Move consumeMove() {
            synchronized (boss) {
                Move m = currMove;
                currMove = null;
                return m;
            }
        }

        Node getNode() { return node; }

        Consumer<Integer> getMove() { return move; }

        void setMove(Consumer<Integer> m) { move = m; }

        void setState(Player.Event evt, Pos p, Dir d) {
            synchronized (boss) {
                event = evt;
                if (p != null) pos = p;
                if (d != null) dir = d;
            }
        }

        Runnable getAnim() { return anim; }

        /** Invocato quando il giocatore fa una mossa.
         * @param m  la mossa del giocatore
         * @return  l'evento dopo l'esecuzione della mossa */
        private Player.Event action(Move m) {
            synchronized (boss) { // Sincronizza sull'oggetto che gestisce il gioco
                long frame = boss.frameCounter;
                currMove = m;
                while (frame == boss.frameCounter) {
                    try {
                        // Aspetta fino a che il prossimo frame di gioco non è stato
                        // eseguito. Il thread rilascia il lock su boss e rimane
                        boss.wait();  // dormiente fino a che non viene eseguito un
                                      // notifyAll su boss e il thread riprende il
                                      // lock su boss.
                    } catch (InterruptedException e) { }
                }
                return event;
            }
        }


        // Il gestore del gioco. Usato anche per sincronizzare l'esecuzione delle
        private final MazeAnim boss;                      // mosse dei giocatori.
        private final Node node;
        private final Thread thread;
        private final Runnable anim;
        private volatile Move currMove;
        private volatile Player.Event event;
        private volatile Pos pos;
        private volatile Dir dir;
        private volatile Consumer<Integer> move;
    }


    private final Maze maze;
    private final Group arena;
    private final Timeline anim;
    private final List<AnimControl> controls;
    private final Pos treasure;
    private volatile long frameCounter;
    private volatile boolean started;
    private volatile AnimControl winner;
    private volatile int gameOverCountdown;

    private static final Random RND = new Random();
    private static final int SMOOTHNESS = 10;
}
