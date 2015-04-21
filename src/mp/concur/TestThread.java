package mp.concur;

import java.math.BigInteger;
import java.util.Scanner;

import static java.lang.System.out;

/** Classe per semplici esempi d'uso di threads */
public class TestThread {
    /** Aspetta che siano passati il numero di secondi specificati
     * @param seconds  numero di secondi di attesa */
    public static void waitFor(int seconds) {
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time < 1000*seconds) ;
    }

    public static void fibonacci() {
        Scanner input = new Scanner(System.in);
        Thread comp = null;
        out.println("Digita n per calcolare F_n o 0 per terminare");
        while (true) {
            try {
                long n = input.nextLong();
                if (comp != null) comp.interrupt();
                if (n == 0) break;
                comp = new Thread(() -> {
                    BigInteger a = BigInteger.valueOf(0);
                    BigInteger b = BigInteger.valueOf(1);
                    for (long i = 1 ; i < n ; i++) {
                        BigInteger c = b.add(a);
                        a = b;
                        b = c;
                        if (Thread.currentThread().isInterrupted()) {
                            out.println("Calcolo interrotto di F_" + n);
                            return;
                        }
                    }
                    out.println("F_"+n+" = "+b);
                });
                comp.start();
            } catch (Exception e) {}
        }
    }

    public static void main(String[] args) throws InterruptedException {
        /*
        Thread t = new Thread(() -> {
            for (int i = 0 ; i < 50 ; i++)
                out.println("Nuovo "+i);
        });
        t.start();
        for (int i = 0 ; i < 50 ; i++)
            out.println("MAIN "+i);
        */

        /*
        Thread counter = new Thread(() -> {
            int count = 0;
            while (true) {         // Ogni secondo stampa il numero di secondi
                waitFor(1);        // passati dalla partenza del thread
                count++;
                out.println(count);
            }
        });
        counter.start();
        waitFor(4);
        counter.interrupt();
        */

        /*
        Thread counter = new Thread(()  -> {
            int count = 0;
            while (true) {        // Ogni secondo stampa il numero di secondi
                try {             // passati dalla partenza del thread
                    Thread.sleep(1000);
                } catch (InterruptedException e) { break; }
                count++;
                out.println(count);
            }
            out.println("Counter interrotto");
        });
        counter.start();
        Thread.sleep(4000);
        counter.interrupt();
        */

        fibonacci();
    }
}
