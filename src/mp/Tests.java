package mp;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

import static java.lang.System.out;


public class Tests {
    public static void main(String[] args) {
        Dipendente rossi = new Dipendente("Mario Rossi", 2000.0);
        rossi.setIndirizzo("Roma, via Rossini, 15");
        rossi.setTelefono("06 8989898");
        Dipendente verdi = new Dipendente("Ugo Verdi", 1850.0);
        verdi.setIndirizzo("Roma, via G. Verdi, 30");
        for (Dipendente d : new Dipendente[]{rossi, verdi}) {
            out.print("Dipendente: "+d.getNomeCognome());
            out.println("  codice: "+d.getCodice());
            out.println("    stipendio:  " + d.getStipendio());
            Dipendente.Contatti con = d.getContatti();
            out.println("    Indirizzo: " + con.getIndirizzo());
            out.println("    Telefono: " + con.getTelefono());
        }

        //excep();
        //arrayEq();
        //equality();
    }

    public static void stampaStipendi(Dipendente[] dd) {
        for (Dipendente d : dd)
            out.println("Stipendio di "+d.getNomeCognome()+" è "+d.getStipendio());
        out.println();
    }

    private static void equality() {
        String s1 = "una stringa";
        String s2 = "una ";
        s2 += "stringa";
        out.println("\"" + s1 + "\" == \"" + s2 + "\" " + (s1 == s2));
        out.println(s1.equals(s2));
    }

    private static void arrayEq() {
        int[] arrA = {1,2,3}, arrB = {1,2,3};
        out.println("arrA.equals(arrB) -> "+arrA.equals(arrB));
        out.println("Arrays.equals(arrA, arrB) -> "+ Arrays.equals(arrA, arrB));
    }

    private static void excep() {
        Scanner input = new Scanner(System.in);
        try {
            out.print("Inserire due interi: ");
            int n = input.nextInt();    // Può lanciare InputMismatchException
            int m = input.nextInt();    // Può lanciare InputMismatchException
            int quoziente = n/m;        // Può lanciare ArithmeticException
            out.println(n+" / "+m+"  fa  "+quoziente);
            out.print("Inserire una parola e una posizione: ");
            String p = input.next();
            int i = input.nextInt(); // Può lanciare InputMismatchException
            char c = p.charAt(i-1);  // Può lanciare StringIndexOutOfBoundsException
            out.println("Il carattere in pos. "+i+" di \""+p+"\" è "+c);
        } catch (InputMismatchException ex) {
            out.print("Eccezione InputMismatchException: ");
            out.println(ex.getMessage());
        } catch (ArithmeticException ex) {
            out.print("Eccezione ArithmeticException: ");
            out.println(ex.getMessage());
        } finally {
            out.println("Questa stampa viene sempre eseguita");
        }
        out.println("Il programma è terminato normalmente");
    }
}
