package mp;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.System.out;


class LPoint {
    public final int x, y;
    public final String label;

    public LPoint(int x, int y, String lab) {
        this.x = x;
        this.y = y;
        label = lab;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass())
            return false;
        LPoint p = (LPoint)o;
        return (p.x == x && p.y == y && Objects.equals(label, p.label));
    }

    @Override
    public String toString() {
        return getClass().getName()+"[x="+x+",y="+y+",label="+label+"]";
    }
}



public class Tests {
    public static void stampaStipendi(Dipendente[] dd) {
        for (Dipendente d : dd)
            out.println(d.getNomeCognome()+" Stipendio: "+
                    d.getStipendio());
    }

    public static void main(String[] args) {
        Object o = new int[3];
        Object d = new Dirigente("Carlo", 100);
        Object[] arrO = new int[2][2];

        LPoint p1 = new LPoint(0, 0, "origine");
        LPoint p2 = new LPoint(0, 0, "origine");
        if (p1.equals(p2))
            out.println("uguali");
        else
            out.println("diversi");

        out.println(p1);

        /*
        Dirigente dir = new Dirigente("Carla Bianchi", 500);
        Dipendente[] dd = new Dipendente[3];
        dd[0] = new Dipendente("Mario Rossi", 1000);
        dd[1] = dir;
        dd[2] = new Dirigente("Ugo Gialli", 200);
        stampaStipendi(dd);
        dd[1].setStipendio(1000);
        if (dd[0] instanceof Dirigente)
            ((Dirigente)dd[0]).setBonus(200);
        stampaStipendi(dd);


        Dirigente[] dirs = new Dirigente[4];
        dd = dirs;
        dd[0] = new Dipendente("Giorgio");
        */

        /*
        Dirigente dir = new Dirigente("Carla Bianchi", 500);
        out.print(dir.getCodice()+ " "+dir.getNomeCognome());
        out.println("  Stipendio: "+dir.getStipendio());
        */

        /*
        Dipendente rossi = new Dipendente("Mario Rossi", 2500.0);
        rossi.setIndirizzo("Roma, via Rossini, 15");
        rossi.setTelefono("06 8989898");
        Dipendente verdi = new Dipendente("Ugo Verdi", 2000.0);
        verdi.setIndirizzo("Roma, via G. Verdi, 30");
        for (Dipendente d : new Dipendente[]{rossi, verdi}) {
            out.print("Dipendente: "+d.getNomeCognome());
            out.println("  codice: "+d.getCodice());
            out.println("    stipendio:  " + d.getStipendio());
            Dipendente.Contatti con = d.getContatti();
            out.println("    Indirizzo: " + con.getIndirizzo());
            out.println("    Telefono: " + con.getTelefono());
        }
        */

        //excep();
        //arrayEq();
        //equality();
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
