package mp.gui;

import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Una classe per fare le prime sperimentazioni con JavaFX */
public class TestGui extends Application {
    public static void main(String[] args) {
        launch(args);  // Lancia l'applicazione, ritorna quando l'applicazione
    }                  // termina. Può essere invocato una sola volta

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(textChange(), 600, 200);
        primaryStage.setScene(scene);             // La scena della finestra
        primaryStage.show(); // Rende visibile la finestra (o stage) principale
    }

    private Parent shapes() {
        Rectangle r = new Rectangle(20, 40, 200, 100); // Rettangolo in (20, 40)
        Ellipse e = new Ellipse(80, 70, 50, 60);  // Ellisse con centro (80, 70)
        e.setFill(Color.ALICEBLUE);
        return new Group(r, e);
    }

    private Parent texts() {
        Text t1 = new Text("Hello JavaFX");
        Text t2 = new Text("Programming");
        VBox vb = new VBox(t1, t2);
        vb.setAlignment(Pos.CENTER);   // Allineamento dei nodi
        vb.setSpacing(30);             // Spazio tra i nodi
        return vb;
    }

    private Parent textChange() {
        Text txt = new Text("Hello JavaFX");
        StackPane sp = new StackPane(txt);
        sp.setPrefHeight(100);
        Button showHide = new Button("Hide");
        showHide.setOnAction(e -> {
            if (txt.getOpacity() > 0.0) {
                txt.setOpacity(0.0);
                showHide.setText("Show");
            } else {
                txt.setOpacity(1.0);
                showHide.setText("Hide");
            }
        });
        Slider size = new Slider(8, 40, 12);     // Per la dimensione della fonte
        tSize(txt, 12);
        size.valueProperty().addListener((o,ov,nv) -> tSize(txt, (Double)nv));
        ComboBox<String> ff = new ComboBox<>();  // Per il nome della fonte
        ff.setMaxWidth(100);
        ff.getItems().addAll(Font.getFontNames());
        ff.setValue(txt.getFont().getName());
        ff.setOnAction(e -> tName(txt, ff.getValue()));
        ColorPicker cp = new ColorPicker();     // Per il colore del testo
        cp.setValue(Color.BLACK);
        cp.setOnAction(e -> {
            txt.setFill(cp.getValue());
            txt.setStroke(cp.getValue());
        });
        TextField tf = new TextField("Hello JavaFX");  // Per cambiare il testo
        tf.setMaxWidth(300);
        tf.setOnAction(e -> txt.setText(tf.getText()));
        HBox hb = new HBox(showHide, size, ff, cp);   // Layout per i controlli
        hb.setAlignment(Pos.CENTER);
        VBox vb = new VBox(sp, hb, tf);
        vb.setAlignment(Pos.CENTER);
        vb.setSpacing(20);

        //Button b = new Button("!");
        //b.setOnAction(e -> fun(vb));
        // vb.getChildren().add(b);      // Per divertimento

        return vb;
    }

    /** Modifica la dimensione della fonte del nodo Text specificato.
     * @param t  un nodo Text
     * @param s  la nuova dimensione della fonte */
    private static void tSize(Text t, double s) {
        t.setFont(new Font(t.getFont().getName(), s));
    }

    /** Modifica il nome completo della fonte del nodo Text specificato.
     * @param t  un nodo Text
     * @param fname  il nuovo nome completo della fonte */
    private static void tName(Text t, String fname) {
        t.setFont(new Font(fname, t.getFont().getSize()));
    }

    /** Just for fun */
    private static void fun(Node u) {
        if (u instanceof Control || u instanceof Shape) {
            RotateTransition rot = new RotateTransition(Duration.millis(500), u);
            rot.setToAngle(-180 + Math.random()*360);
            rot.setCycleCount(4);
            rot.setAutoReverse(true);
            rot.play();
        } else if (u instanceof Parent)
            ((Parent)u).getChildrenUnmodifiable().forEach(v -> fun(v));
    }
}
