package mp.gui;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

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

        Button back = new Button("<");
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

        Button forth = new Button(">");
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

        we.getLoadWorker().stateProperty().addListener((o, ov, nv) -> {
            if (nv == Worker.State.SUCCEEDED) {
                url.setText(we.getLocation());
            } else if (nv == Worker.State.FAILED || nv == Worker.State.CANCELLED) {
                System.out.println("Failed");
            }
        });

        SplitPane inspSP = new SplitPane();
        Inspector insp = new Inspector();
        ToggleButton inspB = new ToggleButton("I");
        inspB.setOnAction(e -> {
            if (inspB.isSelected()) {
                insp.set();
                if (!inspSP.getItems().contains(insp.getNode()))
                    inspSP.getItems().add(insp.getNode());
            } else {
                inspSP.getItems().remove(insp.getNode());
            }

        });

        HBox hb = new HBox(back, forth, url, inspB);
        HBox.setHgrow(url, Priority.ALWAYS);      // Si estende in orizzontale
        VBox vb = new VBox(hb, wView);
        VBox.setVgrow(wView, Priority.ALWAYS);    // Si estende in verticale

        inspSP.getItems().add(vb);
        return inspSP;
    }

    private class Inspector {
        Inspector() {
            treeV = new TreeView<>();
        }

        Node getNode() { return treeV; }

        void set() {
             // Da completare
        }

        private TreeView<String> treeV;
    }


    private WebView wView;
}
