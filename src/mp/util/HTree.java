package mp.util;

import java.util.*;

/** Un oggetto {@link HTree} rappresenta un albero. È implementato tramite
 * {@link java.util.HashMap} e quindi gli oggetti che sono i nodi dell'albero
 * dovrebbero avere un'adeguata definizione dei metodi {@link Object#equals} e
 * {@link Object#hashCode}. Due oggetti che sono uguali (secondo
 * {@link Object#equals}) non possono appartenere allo stesso albero.
 * Per semplicità le condizioni di errore non sono state implementate.
 * @param <T>  tipo dei nodi dell'albero */
public class HTree<T> {
    /** Crea un albero con la radice data.
     * @param root  la radice dell'albero */
    public HTree(T root) {
        this.root = root;        // La radice
        parent.put(root, null);  // La radice ha come genitore null
        children.put(root, new ArrayList<>());  // Figli della radice: la lista vuota
    }

    /** Aggiunge uno o più nodi a questo albero.
     * @param p  il nodo genitore dei nodi da aggiungere
     * @param cc  oggetti da aggiungere come nodi figli di p */
    public void add(T p, T...cc) {
        for (T c : cc) {
            parent.put(c, p);        // Imposta genitore del nuovo nodo
            children.get(p).add(c);  // Aggiunge nuovo nodo come figlio del genitore
            children.put(c, new ArrayList<>());  // Figli nuovo nodo: la lista vuota
        }
    }

    /** @return la radice di questo albero */
    public T getRoot() { return root; }

    /**Ritorna {@code true} se l'oggetto dato è un nodo di questo albero.
     * @param u  un oggetto
     * @return {@code true} se l'oggetto è un nodo di questo albero */
    public boolean isNode(T u) { return parent.containsKey(u); }

    /** Ritorna il genitore del nodo dato.
     * @param u  un nodo di questo albero
     * @return il genitore del nodo */
    public T getParent(T u) { return parent.get(u); }

    /** @return una vista non modificabile della lista dei figli del nodo dato */
    public List<T> getChildren(T u) {
        return Collections.unmodifiableList(children.get(u));
    }

    /** @return una stringa che rappresenta pienamente questo albero */
    public String toFullString() {
        return toFullString(root, "");
    }

    private String toFullString(T u, String prefix) {
        String s = (prefix.isEmpty() ? "" : prefix + "---");
        s += u + "\n";
        for (T c : getChildren(u))
            s += toFullString(c, prefix + "    |");
        return s;
    }


    private T root;                                           // Radice dell'albero
    private final Map<T,T> parent = new HashMap<>();          // Mappa dei genitori
    private final Map<T,List<T>> children = new HashMap<>();  // Mappa dei figli
}