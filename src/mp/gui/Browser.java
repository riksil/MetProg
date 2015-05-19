package mp.gui;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

        HBox hb = new HBox(back, forth, url, inspB);
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


    private WebView wView;
}
