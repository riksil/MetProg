package mp.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Un mini web browser */
public class Browser extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(createUI(), 700, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** Crea e ritorna la componente principale della UI.
     * @return la componente principale delle UI */
    private Parent createUI() {
        wView = new WebView();
        WebEngine we = wView.getEngine();
        TextField url = new TextField();
        url.setOnAction(e -> we.load(url.getText()));

        WebHistory h = we.getHistory();
        ObservableList<WebHistory.Entry> hList = h.getEntries();

        Image backIcon = new Image(getClass().getResource("left16.png").toString());
        Button back = new Button(null, new ImageView(backIcon));
        back.setOnAction(e -> h.go(-1));
        back.disableProperty().bind(h.currentIndexProperty().isEqualTo(0));
        back.setOnContextMenuRequested(e -> {
            ContextMenu cm = new ContextMenu();
            int curr = h.getCurrentIndex();
            for (int i = curr - 1; i >= 0; i--) {
                int offset = i - curr;
                MenuItem mi = new MenuItem(hList.get(i).getTitle());
                mi.setOnAction(ev -> h.go(offset));
                cm.getItems().add(mi);
            }
            cm.setAutoHide(true);
            cm.show(back.getScene().getWindow(), e.getScreenX(), e.getScreenY());
        });

        Image forthIcon = new Image(getClass().getResource("right16.png").toString());
        Button forth = new Button(null, new ImageView(forthIcon));
        forth.setOnAction(e -> h.go(1));
        forth.disableProperty().bind(Bindings.createBooleanBinding(
                () -> h.getCurrentIndex() >= hList.size() - 1,
                h.currentIndexProperty()));
        forth.setOnContextMenuRequested(e -> {
            ContextMenu cm = new ContextMenu();
            int curr = h.getCurrentIndex();
            for (int i = curr + 1; i < hList.size(); i++) {
                int offset = i - curr;
                MenuItem mi = new MenuItem(hList.get(i).getTitle());
                mi.setOnAction(ev -> h.go(offset));
                cm.getItems().add(mi);
            }
            cm.setAutoHide(true);
            cm.show(forth.getScene().getWindow(), e.getScreenX(), e.getScreenY());
        });

        SplitPane inspSP = new SplitPane();
        InspectorTT insp = new InspectorTT();
        Image inspIcon = new Image(getClass().getResource("insp16.png").toString());
        ToggleButton inspB = new ToggleButton(null, new ImageView(inspIcon));
        inspB.setOnAction(e -> {
            if (inspB.isSelected()) {
                insp.set(we.getDocument());
                inspSP.getItems().add(insp.getNode());
            } else
                inspSP.getItems().remove(insp.getNode());
        });

        we.getLoadWorker().stateProperty().addListener((o, ov, nv) -> {
            if (nv == Worker.State.SUCCEEDED) {
                url.setText(we.getLocation());
                if (inspB.isSelected())
                    insp.set(we.getDocument());
            } else if (nv == Worker.State.FAILED || nv == Worker.State.CANCELLED) {
                if (inspB.isSelected())
                    insp.set(we.getDocument());
                System.out.println("Failed");
            }
        });

        Node dl = downloadImages(we);

        HBox hb = new HBox(back, forth, url, inspB, dl);
        HBox.setHgrow(url, Priority.ALWAYS);      // Si estende in orizzontale
        VBox vb = new VBox(hb, wView);
        VBox.setVgrow(wView, Priority.ALWAYS);    // Si estende in verticale

        inspSP.getItems().add(vb);
        return inspSP;
    }

    /** Gestisce la visualizzazione dell'albero di parsing di una pagina */
    private static class Inspector {
        Inspector() {
            treeV = new TreeView<>();
        }

        /** @return il nodo che visualizza l'albero */
        Node getNode() { return treeV; }

        /** Visualizza l'albero di parsing dell'ogetto {@link org.w3c.dom.Document}
         * specificato. Se è null, non visualizza nulla.
         * @param dom l'oggetto che rappresenta l'albero di parsing o null */
        void set(Document dom) {
            TreeItem<String> root = null;
            if (dom != null) {
                root = new TreeItem<>(dom.getDocumentURI());
                createTree(root, dom);
            }
            treeV.setRoot(root);
        }

        /** Crea ricorsivamente l'albero di visualizzazione. Più precisamente
         * traduce il sotto-albero radicato in u in un corrispondente albero di
         * {@link javafx.scene.control.TreeItem} con la radice data root.
         * @param root  la radice dell'albero per la visualizzazione
         * @param u  la radice dell'albero di parsing */
        private void createTree(TreeItem<String> root, org.w3c.dom.Node u) {
            NodeList children = u.getChildNodes();
            for (int i = 0 ; i < children.getLength() ; i++) {
                org.w3c.dom.Node child = children.item(i);
                TreeItem<String> v = new TreeItem<>(child.getNodeName());
                root.getChildren().add(v);
                createTree(v, child);
            }
        }

        private TreeView<String> treeV;
    }

    /** Ritorna una stringa che contiene informazioni sul nodo dato.
     * @param u  u nodo di un albero di parsing HTML
     * @return una stringa che contiene informazioni sul nodo */
    private static String info(org.w3c.dom.Node u) {
        switch (u.getNodeType()) {
            case org.w3c.dom.Node.DOCUMENT_NODE:
                return u.getBaseURI();
            case org.w3c.dom.Node.ELEMENT_NODE:
                NamedNodeMap map = u.getAttributes();
                String s = "";
                for (int i = 0 ; i < map.getLength() ; i++) {
                    org.w3c.dom.Node a = map.item(i);
                    s += (i > 0 ? "\n":"")+a.getNodeName()+": "+a.getNodeValue();
                }
                return s;
            default:
                return u.getNodeValue();
        }
    }

    private static class InspectorTT {
        InspectorTT() {
            treeV = new TreeView<>();
            treeV.setCellFactory(t -> {
                TreeCell<org.w3c.dom.Node> cell = new TreeCell<>();
                cell.itemProperty().addListener((o,ov,u) ->{
                    cell.setText(u != null ? u.getNodeName() : "");
                    cell.setTooltip(u != null ? new Tooltip(info(u)) : null);
                });
                return cell;
            });
        }

        Node getNode() { return treeV; }

        void set(Document dom) {
            TreeItem<org.w3c.dom.Node> root = null;
            if (dom != null) {
                root = new TreeItem<>(dom);
                createTree(root, dom);
            }
            treeV.setRoot(root);
        }

        private void createTree(TreeItem<org.w3c.dom.Node> root, org.w3c.dom.Node u) {
            NodeList children = u.getChildNodes();
            for (int i = 0 ; i < children.getLength() ; i++) {
                org.w3c.dom.Node child = children.item(i);
                TreeItem<org.w3c.dom.Node> v = new TreeItem<>(child);
                root.getChildren().add(v);
                createTree(v, child);
            }
        }

        private TreeView<org.w3c.dom.Node> treeV;
    }

    /** Ritorna l'insieme degli URI delle immagini contenute nella pagina relativa
     * al Document dato. Eventuali URI malformati sono ignorati. Se il Document
     * non ha un URI, ritorna un insieme vuoto.
     * @param doc  il Document di ina pagina web
     * @return l'insieme degli URI delle immagini della pagina */
    private static Set<URI> imgURIs(Document doc) {
        Set<URI> uris = new HashSet<URI>();
        try {
            URI base = new URI(doc.getDocumentURI());
            NodeList ii = doc.getElementsByTagName("img");
            for (int i = 0 ; i < ii.getLength() ; i++) {
                org.w3c.dom.Node a = ii.item(i).getAttributes()
                        .getNamedItem("src");
                if (a != null)
                    try {
                        uris.add(base.resolve(a.getNodeValue()));
                    } catch (Exception e) {}
            }
        } catch (Exception e) {}
        return uris;
    }

    /** Gestisce una finestra di primo livello che visualizza immagini che possono
     * essere aggiunte dinamicamente. */
    private static class DownloadWin {
        /** Crea il gestore della finestra per le immagini. La finestra non è
         * resa visibile. */
        DownloadWin() {
            stage = new Stage();
            imgPane = new FlowPane();
            ScrollPane sp = new ScrollPane(imgPane);
            pageList = new VBox();
            ScrollPane sp2 = new ScrollPane(pageList);
            SplitPane split = new SplitPane(sp, sp2);
            stage.setScene(new Scene(split, 500, 400));
        }

        /** Aggiunge una nuova pagina di cui è iniziato il downloading delle
         * immagini tramite lo specificato task.
         * @param task  il task che esegue il dowloading delle immagini
         * @param page  l'URL della pagina */
        void add(Task task, String page) {
            ProgressIndicator pi = new ProgressIndicator();
            pi.progressProperty().bind(task.progressProperty());
            Button stop = new Button("X");
            stop.setOnAction(e -> task.cancel());
            stop.disableProperty().bind(task.runningProperty().not());
            HBox hb = new HBox(pi, stop);
            hb.setAlignment(Pos.BOTTOM_LEFT);
            pageList.getChildren().add(new Label(page, hb));
        }

        /** Aggiunge un'immagine e la visualizza
         * @param img  un'immagine */
        void add(Image img) {
            imgPane.getChildren().add(new ImageView(img));
        }

        /** Rende la finestra visibile e la porta in primo piano */
        void show() {
            stage.show();       // Rende visibile la finestra se già non lo era
            stage.toFront();    // Porta la finestra in primo piano
        }

        private final Stage stage;
        private final FlowPane imgPane;
        private final VBox pageList;
    }

    /** Un task che può essere eseguito in modo asincrono per scaricare un insieme
     * di immagini */
    private static class ImgTask extends Task<Void> {
        /** Crea un task asincrono per scaricare le immagini relative all'insieme
         * di URI dato e per seguire su ognuna di esse l'azione specificata.
         * L'azione è eseguita nel JavaFX Thread.
         * @param uu  insieme di URI di immagini
         * @param act  azione da compiere per ogni immagine scaricata */
        ImgTask(Set<URI> uu, Consumer<Image> act) {
            uris = uu;
            action = act;
        }

        @Override
        protected Void call() throws Exception {
            int count = 0;        // Per il conteggio delle immagini scaricate
            for (URI u : uris) {
                Image img = new Image(u.toString());     // Scarica l'immagine
                        // Esegue l'azione in modo asincrono nel JavaFX Thread
                Platform.runLater(() -> action.accept(img));
                count++;
                updateProgress(count, uris.size());
                if (isCancelled())
                    break;
            }
            return null;
        }

        private final Set<URI> uris;
        private final Consumer<Image> action;
    }

    /** Ritorna un componente grafico (un Node) che contiene due bottoni per
     * gestire il dowloading asincrono e la visualizzazione delle immagini
     * contenute nelle pagine scaricate dalla web engine data.
     * @param wEng  una web engine
     * @return un componente grafico (un Node) con due bottoni */
    private static Node downloadImages(WebEngine wEng) {
        ExecutorService exec = Executors.newCachedThreadPool(r -> {  // Factory
            Thread t = new Thread(r);     // dei thread usati dall'esecutore per
            t.setDaemon(true);            // far sì che siano daemon thread, cioè
            return t;                     // non bloccano la chiusura del programma
        });
        DownloadWin win = new DownloadWin();      // Per visualizzare le immagini
        Button loadBtn = new Button(null, new ImageView(Browser.class.
                getResource("load16.png").toString()));
        loadBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> wEng.getDocument() == null, wEng.documentProperty()));
        loadBtn.setOnAction(e -> {
            Document d = wEng.getDocument();
            Task t = new ImgTask(imgURIs(d),win::add);
            win.add(t, d.getDocumentURI());
            exec.submit(t);
        });
        Button winBtn = new Button(null, new ImageView(Browser.class.
                getResource("win16.png").toString()));
        winBtn.setOnAction(e -> win.show());
        return new HBox(loadBtn, winBtn);
    }




    private WebView wView;
}
