package mp.game.util;

import mp.util.Utils;

import java.util.Objects;
import java.util.Random;

/** Classe di utilità per la costruzione di labirinti e il loro uso durante un
 * gioco. */
public class MazeUtils {
    /** Le quattro direzioni in cui un giocatore può essere diretto */
    public enum Dir { NORTH, EAST, SOUTH, WEST;

        /** @return la direzione dopo una rotazione a destra di 90 gradi */
        public Dir right() {
            return Dir.values()[(index() + 1) % Dir.values().length];
        }

        /** @return la direzione dopo una rotazione a sinistra di 90 gradi */
        public Dir left() {
            int n = Dir.values().length;
            return Dir.values()[(index() + n - 1) % n];
        }

        /** @return rotazione in gradi rispetto alla direzione NORTH */
        public double rot() {
            switch (this) {
                case NORTH: return 0;
                case EAST: return 90;
                case SOUTH: return 180;
                case WEST: return 270;
                default:
                    return 0;
            }
        }

        private int index() {  // Ritorna l'indice di questa direzione
            Dir[] vals = Dir.values();
            for (int i = 0 ; i < vals.length ; i++)
                if (vals[i] == this) return i;
            return 0;
        }
    }

    /** Una posizione all'interno di un labirinto */
    public static class Pos {
        public final int row, col;  // Riga e colonna della posizione

        /** Crea una posizione con la riga e la colonna specificate.
         * @param r  numero di riga
         * @param c  numero di colonna */
        public Pos(int r, int c) {
            row = r;
            col = c;
        }

        /** La posizione della cella in cui si arriva se da questa posizione ci si
         * muove di un passo nella direzione specificata. Si assume che questa
         * posizione sia quella di una cella.
         * @param d  una direzione
         * @return la posizione della cella di arrivo */
        public Pos go(Dir d) {
            switch (d) {
                case NORTH: return go(0);
                case EAST: return go(1);
                case SOUTH: return go(2);
                case WEST: return go(3);
                default:
                    return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pos pos = (Pos)o;
            return row == pos.row && col == pos.col;
        }

        @Override
        public int hashCode() { return Objects.hash(row, col); }

        /** Ritorna la posizione d'arrivo muovendosi nella direzione di indice dato.
         * @param i  indice della direzione
         * @return la posizione d'arrivo muovendosi nella direzione data */
        private Pos go(int i) {
            return new Pos(movR(row, i), movC(col, i));
        }
    }

    /** Ritorna un labirinto con le specificate dimensioni. Il labirinto è
     * rappresentato con una matrice di boolean. Ogni elemento della matrice
     * corrisponde a un rettangolo identificato dalle coordinate di riga e colonna
     * e che può essere di uno dei seguenti tre tipi: bordo, muro e cella. La
     * matrice ritornata m è tale che m[r][c] è true se e solo se il rettangolo
     * di coordinate (r, c) è percorribile (o libero). Generalmente tutte le
     * celle sono percorribili. La disposizione dei vari tipi di rettangoli ha il
     * seguente schema:
     * <pre>
     *     B B B B B . . . B B B B
     *     B C W C W . . . C W C B
     *     B W W W W . . . W W W B
     *     B C W C W . . . C W C B
     *     B W W W W . . . W W W B
     *     B C W C W . . . C W C B
     *     . . . . . . . . . . . .
     *     B W W W W . . . W W W B
     *     B C W C W . . . C W C B
     *     B B B B B . . . B B B B
     * </pre>
     * Dove B è un bordo, W un muro e C una cella. Per rispettare questo schema
     * sia il numero di righe che quello delle colonne deve essere un intero
     * dispari. Il labirinto è costruito tramite una visita in profondità (DFS)
     * random. Più precisamnete, la visita inizia in una cella random e ad ogni
     * passo sceglie in modo random una delle celle vicine e se non è stata ancora
     * visitata apre il muro tra le due celle e continua la visita da quella
     * cella. I labirinti costruiti in questo modo non hanno cicli.
     * @param nr  numero righe (intero dispari)
     * @param nc  numero colonne (intero dispari)
     * @return  un labirinto con le specificate dimensioni */
    public static boolean[][] genMaze(int nr, int nc) {
        boolean[][] maze = new boolean[nr][nc];
        class DFS {
            void visit(int r, int c) {
                maze[r][c] = true;
                int[] ind = Utils.rndIndices(4);
                for (int h = 0 ; h < 4 ; h++) {
                    int rr = movR(r, ind[h]), cc = movC(c, ind[h]);
                    if (inside(rr, cc, nr, nc) && !maze[rr][cc]) {
                        maze[passI(r, rr)][passI(c, cc)] = true;
                        new DFS().visit(rr, cc);
                    }
                }
            }
        }
        Pos start = rndCell(nr, nc);
        new DFS().visit(start.row, start.col);
        return maze;
    }

    /** Aggiunge cicli al labirinto dato aprendo muri in modo tale da eliminare
     * tutti i vicoli ciechi (cioè, celle che hanno una sola cella libera nel
     * loro vicinato).
     * @param maze  un labirinto */
    public static void noDeadEnd(boolean[][] maze) {
        int nr = maze.length, nc = maze[0].length;
        for (int r = 1 ; r < nr-1 ; r+=2)
            for (int c = 1 ; c < nc-1 ; c+=2) {
                int cnt = 0;
                for (int k = 0 ; k < 4 ; k++) {
                    int rr = movR(r, k), cc = movC(c, k);
                    if (inside(rr, cc, nr, nc) && maze[passI(r, rr)][passI(c, cc)])
                        cnt++;
                }
                if (cnt > 1) continue;
                for (int k = 0 ; k < 4 ; k++) {
                    int rr = movR(r, k), cc = movC(c, k);
                    if (inside(rr, cc, nr, nc) && !maze[passI(r, rr)][passI(c, cc)])
                        maze[passI(r, rr)][passI(c, cc)] = true;
                }
            }
    }


    /** Aggiunge il numero specificato di uscite (random) al labirinto dato.
     * @param maze  un labirinto
     * @param nExits  numero uscite */
    public static void exits(boolean[][] maze, int nExits) {
        int nr = maze.length, nc = maze[0].length;
        int[][] border = {{-1, 0}, {-1, nc-1}, {0, -1}, {nr-1, -1}};
        while (nExits > 0) {
            int[] b = border[RND.nextInt(4)];
            int r, c;
            if (b[0] == -1) {
                r = 2*RND.nextInt(nr/2)+1;
                c = b[1];
            } else {
                r = b[0];
                c = 2*RND.nextInt(nc/2)+1;
            }
            if (!maze[r][c]) {
                maze[r][c] = true;
                nExits--;
            }
        }
    }


    static int passI(int k1, int k2) { return k1 + (k2 - k1)/2; }

    static int movR(int r, int i) { return r + moves[i][0]; }

    static int movC(int c, int i) { return c + moves[i][1]; }

    static boolean inside(int r, int c, int nr, int nc) {
        return r > 0 && r < nr && c > 0 && c < nc;
    }

    static Pos rndCell(int nr, int nc) {
        return new Pos(2*RND.nextInt(nr/2)+1, 2*RND.nextInt(nc/2)+1);
    }


    private static final int[][] moves = {{-2, 0}, {0, 2}, {2, 0}, {0, -2}};
    private static final Random RND = new Random();
}
