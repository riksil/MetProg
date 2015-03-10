package mp.tapp;

import java.util.Arrays;
import java.util.Scanner;

import static java.lang.System.out;

/** {@code MenuApp} fornisce le funzionalità di base per un'applicazione basata
 * su un menu testuale. La sotto-classe deve fornire i contenuti. */
public abstract class MenuApp {
    /** Esegue l'applicazione. L'utente sceglie una voce del menu digitando il
     * numero mostrato a sinistra della voce. */
    public void run() {
        Scanner input = new Scanner(System.in);
        boolean quit = false;
        while (!quit) {
            for (int i = 0 ; i < menu.length ; i++)
                out.println((i+1)+". "+menu[i]);
            while (!input.hasNextInt())
                input.next();     // Scarta qualsiasi input che non è un intero
            int choice = input.nextInt();
            if (choice >= 1 && choice < menu.length)
                doMenu(choice);
            else if (choice == menu.length)
                quit = true;
        }
        out.println("Applicazione terminata");
    }

    /** Usato dalla sotto-classe per inizializzare le voci del menu, esclusa la
     * voce per terminare "Quit", che è gestita direttamente da questa classe.
     * @param items  le voci del menu */
    protected MenuApp(String...items) {
        menu = Arrays.copyOf(items, items.length+1);
        menu[menu.length-1] = "Quit";
    }

    /** Implementato dalla sotto-classe per eseguire la voce del menu scelta.
     * @param choice  il numero della voce di menu scelta */
    protected abstract void doMenu(int choice);

    private String[] menu;
}
