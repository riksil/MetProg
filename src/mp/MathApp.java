package mp;

import mp.tapp.MenuApp;
import java.util.Scanner;

import static java.lang.System.out;

/** Una semplice applicazione con menu testuale per il calcolo di funzioni
 * matematiche. */
public class MathApp extends MenuApp {
    public MathApp() {
        super("Logaritmo", "Radice quadrata");
    }

    /** Esegue il calcolo relativo alla voce di menu scelta.
     * @param choice  il numero della voce di menu scelta */
    @Override
    protected void doMenu(int choice) {
        out.print("Digita un numero: ");
        Scanner input = new Scanner(System.in);
        while (!input.hasNextDouble())
            input.next();     // Scarta qualsiasi input che non è un numero
        double x = input.nextDouble();
        String result = "";
        switch (choice) {
            case 1: result = "Log("+x+") = "+Math.log(x); break;
            case 2: result = "Sqrt("+x+") = "+Math.sqrt(x); break;
        }
        out.println(result);
    }

    public static void main(String[] args) {
        MathApp app = new MathApp();
        app.run();
    }
}
