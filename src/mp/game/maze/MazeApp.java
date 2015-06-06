package mp.game.maze;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import mp.game.util.Maze;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


/** Applicazione per il gioco Find-Treasure */
public class MazeApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {         // Crea la GUI
        MenuItem newGame = new MenuItem("New Game");
        newGame.setOnAction(e -> newGame());
        Menu addDefPlayer = new Menu("Add Player");
        for (Class<? extends Player> c : Arrays.asList(RndPlayer.class,
                RndPlayer2.class)) {
            MenuItem mi = new MenuItem(c.getSimpleName());
            mi.setOnAction(e -> addPlayer(c));
            addDefPlayer.getItems().add(mi);
        }
        // Permette di caricare un nuovo giocatore scegliendo un file di tipo class
        // che contiene l'implementazione di un giocatore (cioè, una classe che
        // implementa l'interfaccia Player). Per semplicità si assume che la classe
        // appartenga al package mp.game.maze.
        MenuItem addPlayer = new MenuItem("Add Player...");
        addPlayer.setOnAction(e -> addPlayer());
        Menu menu = new Menu("File", null, newGame, addDefPlayer, addPlayer);
        MenuBar menuBar = new MenuBar(menu);
        menuBar.setUseSystemMenuBar(true);
        area = new Group(menuBar);
        primaryStage.setScene(new Scene(area));
        newGame();
        primaryStage.show();
    }

    /** Inizia un nuovo gioco. Se c'era un gioco in esecuzione lo termina. Il nuovo
     * gioco crea nuove istanze dei giocatori esistenti. */
    private void newGame() {
        if (game != null) {
            game.gameOver();
            area.getChildren().remove(game.getNode());
        }
        Maze m = new Maze(NR, NC);
        m.setWidths(WALL, PASSAGE, BORDER);
        game = new MazeAnim(m);
        for (Supplier<Player> p : players)
            game.add(p.get());
        area.getChildren().add(game.getNode());
        game.start();
    }

    /** Aggiunge un nuovo giocatore.
     * @param p  un giocatore */
    private void addPlayer(Class<? extends Player> p) {
        Supplier<Player> factory = () -> {  // Crea un'istanza del nuovo giocatore
            try {
                return p.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        };
        players.add(factory);
        if (game != null)
            game.add(factory.get());
    }

    /** Permette all'utente di scegliere un class file che implementa un giocatore
     * e di agiungerlo al gioco. */
    private void addPlayer() {
        Window owner = area.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Player Class");
        fileChooser.getExtensionFilters().add(new FileChooser
                .ExtensionFilter("Class Files", "*.class"));
        File selectedFile = fileChooser.showOpenDialog(owner);
        if (selectedFile != null) {
            try {
                String className = getPlayerClassName(selectedFile);
                // Crea un caricatore di class file che usa lo specificato URL per
                // trovare la classe da caricare e le eventuali altre classi o risorse.
                // L'URL (o gli URL) specificato deve puntare alla directory che
                // contiene la directory del package di base della classe che si
                // vuole caricare.
                URLClassLoader classLoader = new URLClassLoader(
                        new URL[] {getURL(selectedFile)});
                // Usa il caricatore di class file per caricare la il class file
                // scelto dall'utente. È necessario che il class file sia
                // effettivamente l'implementazione dell'interfaccia Player.
                Class<? extends Player> playerClass = classLoader
                        .loadClass(className).asSubclass(Player.class);
                Supplier<Player> factory = () -> {  // La factory che permette di
                    try {                           // creare istanze del giocatore
                        return playerClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                };
                players.add(factory);
                if (game != null)
                    game.add(factory.get());
            } catch (MalformedURLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /** Ritorna l'URL del classpath per il file (class file) specificato. Si assume
     * che il giocatore sia implementato in una classe in un package mp.game.maze.
     * @param file  il file che contiene il class file di un giocatore
     * @return  l'URL del classpath
     * @throws MalformedURLException se la creazione dell'URL fallisce */
    private static URL getURL(File file) throws MalformedURLException {
        return file.getParentFile().getParentFile().getParentFile()
                .getParentFile().toURI().toURL();
    }

    /** Ritorna il nome completo della classe del giocatore implementato nel
     * class file specificato. Si assume che la classe sia nel package mp.game.maze.
     * @param file  il file che contiene il class file di un giocatore
     * @return il nome completo della classe del giocatore */
    private static String getPlayerClassName(File file) {
        String name = file.getName();
        name = name.substring(0, name.indexOf("."));
        return "mp.game.maze."+name;
    }

    private final int WALL = 6, PASSAGE = 42, BORDER = 10;
    private final int NR = 23, NC = 31;
    private final List<Supplier<Player>> players = new ArrayList<>();
    private Group area;
    private MazeAnim game = null;
}
