package mp;

import java.math.BigInteger;
import java.util.Scanner;
import static java.lang.System.out;

/** Una classe per fare esempi sulla classi locali */
public class LocalClasses {
    /** Ritorna il coefficiente binomiale n su k. Ovvero, il numero di
     * k-sottoinsiemi di un insieme di n elementi. Calcolo basato sulla formula
     * ricorsiva: C(n, k) = C(n-1, k) + C(n-1, k-1). Già per n=30, k=15, arranca.
     * @param n,k  parametri del coefficiente binomiale
     * @return il coefficiente binomiale n su k */
    public static BigInteger binom(int n, int k) {
        recCount++;
        if (k == 0 || k == n) return BigInteger.valueOf(1);
        else return binom(n - 1, k).add(binom(n- 1, k - 1));
    }

    /** Ritorna il coefficiente binomiale n su k. Ovvero, il numero di
     * k-sottoinsiemi di un insieme di n elementi. Calcolo basato sulla formula
     * ricorsiva: (n, k) = (n-1, k) + (n-1, k-1). La ricorsione è resa efficiente
     * dalla memoizzazione.
     * @param n,k  parametri del coefficiente binomiale
     * @return il coefficiente binomiale n su k */
    public static BigInteger binomM(int n, int k) {
        class Binom {
            Binom(int n, int k) { c = new BigInteger[n+1][k+1]; }

            BigInteger compute(int n, int k) {
                if (c[n][k] == null) {
                    if (k == 0 || k == n) c[n][k] = BigInteger.valueOf(1);
                    else c[n][k] = compute(n - 1, k).add(compute(n - 1, k - 1));
                }
                return c[n][k];
            }

            BigInteger[][] c;
        }
        Binom b = new Binom(n, k);
        return b.compute(n, k);
    }

    /** Implementazione alternativa di {@link mp.LocalClasses#binomM(int,int)} */
    public static BigInteger binomM2(int n, int k) {
        class Binom {
            BigInteger compute(int n, int k) {
                if (c[n][k] == null) {
                    if (k == 0 || k == n) c[n][k] = BigInteger.valueOf(1);
                    else c[n][k] = compute(n - 1, k).add(compute(n - 1, k - 1));
                }
                return c[n][k];
            }

            BigInteger[][] c = new BigInteger[n+1][k+1];
        }
        return new Binom().compute(n, k);
    }

    /** Implementazione alternativa di {@link mp.LocalClasses#binomM2(int,int)} */
    public static BigInteger binomM3(int n, int k) {
        return new Object() {
            BigInteger compute(int n, int k) {
                if (c[n][k] == null) {
                    if (k == 0 || k == n) c[n][k] = BigInteger.valueOf(1);
                    else c[n][k] = compute(n - 1, k).add(compute(n - 1, k - 1));
                }
                return c[n][k];
            }

            BigInteger[][] c = new BigInteger[n+1][k+1];
        }.compute(n, k);
    }

    public static void main(String[] args) {
        //test_binom();
        test_binomM();
    }

    private static void test_binom() {
        Scanner input = new Scanner(System.in);
        while (true) {
            out.print("Test binom: digita n e k: ");
            int n = input.nextInt(), k = input.nextInt();
            if (n < 0 || k < 0) return;
            recCount = 0;
            out.println(binom(n, k));
            out.println("Chiamate ricorsive: "+recCount);
        }
    }

    private static void test_binomM() {
        Scanner input = new Scanner(System.in);
        while (true) {
            out.print("Test binomM: digita n e k: ");
            int n = input.nextInt(), k = input.nextInt();
            if (n < 0 || k < 0) return;
            out.println(binomM(n, k));
        }
    }

    private static long recCount = 0;   // Per contare le chiamate ricorsive
}
