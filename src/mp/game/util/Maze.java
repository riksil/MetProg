package mp.game.util;


import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.Collection;

import static mp.game.util.MazeUtils.*;

/** Un labirinto */
public class Maze {
    /** Crea un labirinto con le date dimensioni (vedi
     * {@link mp.game.util.MazeUtils#genMaze(int, int)}).
     * @param nr  numero righe (intero dispari)
     * @param nc  numero colonne (intero dispari) */
    public Maze(int nr, int nc) {
        maze = genMaze(nr, nc);
        //noDeadEnd(maze);  // Per eliminare tutti i vicoli ciechi
        this.nr = nr;
        this.nc = nc;
        wallW = 16;
        cellW = 16;
        bordW = 16;
        wallP = Color.SADDLEBROWN;
        cellP = Color.FLORALWHITE;
        bordP = Color.BLACK;
    }

    /** Imposta la larghezza (in pixels) dei muri, delle celle e del bordo. Per
     * ognuna di queste, se null rimane l'impostazione precedente.
     * @param w  larghezza muri o null
     * @param c  larghezza celle o null
     * @param b  larghezza bordo o null */
    public void setWidths(int w, int c, int b) {
        if (w > 0) wallW = w;
        if (c > 0) cellW = c;
        if (b > 0) bordW = b;
    }

    /** Imposta i colori per i muri, le celle e i passaggi e il bordo. Per ognuo
     * di questi, se null rimane l'impostazione precedente.
     * @param w  colore per i muri o null
     * @param c  colore per le celle e i passaggi o null
     * @param b  colore per il bordo o null */
    public void setPaints(Paint w, Paint c, Paint b) {
        if (w != null) wallP = w;
        if (c != null) cellP = c;
        if (b != null) bordP = b;
    }

    /** @return il Node che contiene l'immagine del labirinto */
    public Node draw() {
        int width = 2*bordW + ((nc - 2)/2)*(wallW + cellW)+cellW;
        int height = 2*bordW + ((nr - 2)/2)*(wallW + cellW)+cellW;
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(wallP);
        gc.fillRect(0, 0, width, height);
        gc.setFill(bordP);
        gc.fillRect(0, 0, width, bordW);
        gc.fillRect(0, height - bordW, width, bordW);
        gc.fillRect(0, 0, bordW, height);
        gc.fillRect(width - bordW, 0, bordW, height);
        gc.setFill(cellP);
        for (int r = 0 ; r < nr ; r++)
            for (int c = 0 ; c < nc ; c++)
                if (maze[r][c]) {
                    Point2D p = rectUpperLeft(r, c);
                    Dimension2D d = rectSize(r, c);
                    gc.fillRect(p.getX(), p.getY(), d.getWidth(), d.getHeight());
                }
        return canvas;
    }

    /** @return la larghezza delle celle */
    public int cellSize() { return cellW; }

    /** Ritorna le coordinate rispetto all'immagine del labirinto dell'angolo
     * superiore sinistro del rettangolo con la riga e la colonna specificate.
     * @param r  numero di riga
     * @param c  numero di colonna
     * @return le coordinate dell'angolo superiore sinistro del rettangolo */
    public Point2D rectUpperLeft(int r, int c) {
        int x = (c > 0 ? bordW + (c/2)*(cellW+wallW) + (c % 2 == 0 ? -wallW : 0) : 0);
        int y = (r > 0 ? bordW + (r/2)*(cellW+wallW) + (r % 2 == 0 ? -wallW : 0) : 0);
        return new Point2D(x, y);
    }

    /** Ritorna true se la cella a cui si arriva dalla cella p con un passo nella
     * direzione d è libera e il passaggio tra le due celle è aperto.
     * @param p  la posizione di una cella
     * @param d  una direzione
     * @return true se si può fare un passo dalla cella p nella direzione d */
    public boolean pass(Pos p, Dir d) {
        Pos pp = p.go(d);
        if (!inside(pp.row, pp.col, nr, nc) || !isFree(pp)) return false;
        int r = passI(p.row, pp.row), c = passI(p.col, pp.col);
        return maze[r][c];
    }

    /** Ritorna la posizione di una cella scelta in modo random tra quelle libere
     * e la cui posizione non è tra quelle date.
     * @param taken  una collezione di posizioni di celle o null
     * @return la poszione di una cella libera random */
    public Pos rndFreeCell(Collection<Pos> taken) {
        for (int t = 0 ; t < 100 ; t++) {
            Pos p = rndCell(nr, nc);
            if (maze[p.row][p.col] && (taken == null || !taken.contains(p)))
                return p;
        }
        return null;
    }

    /** Ritorna le dimensioni del rettangolo del labirinto con le specificate
     * riga e colonna.
     * @param r  numero di riga
     * @param c  numero di colonna
     * @return le dimensioni del rettangolo con le date riga e colonna */
    private Dimension2D rectSize(int r, int c) {
        int w, h;
        if (r == 0 || r == nr - 1) h = bordW;
        else if (r % 2 == 1) h = cellW;
        else h = wallW;
        if (c == 0 || c == nc - 1) w = bordW;
        else if (c % 2 == 1) w = cellW;
        else w = wallW;
        return new Dimension2D(w, h);
    }

    /** Ritorna true se il rettangolo con la posizione data è libero, cioè
     * percorribile.
     * @param p  una poszione qualsiasi del labirinto
     * @return true se il rettangolo con la posizione data è libero */
    private boolean isFree(Pos p) {
        return inside(p.row, p.col, nr, nc) && maze[p.row][p.col];
    }

    private final boolean[][] maze;
    private final int nr, nc;
    private int wallW, cellW, bordW;
    private Paint wallP, cellP, bordP;
}
