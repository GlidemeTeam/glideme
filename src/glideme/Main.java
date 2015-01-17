package glideme;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    /**
     * The state of the world.
     */
    private static World world = new World();

    /**
     * World-updating background task.
     */
    private static Thread worldThread;

    /**
     * Spawn the main world-updating loop in a new thread.
     */
    private static void spawnWorldLoop() {
        worldThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    world.refresh();

                    try {
                        Thread.sleep(world.TIME_QUANTUM);
                    }
                    catch (InterruptedException _) {
                        return;
                    }
                }
            }
        });
        worldThread.setDaemon(true);
        worldThread.start();
    }

    /**
     * Start the GUI. (Called automatically from the GUI thread).
     *
     * @param primaryStage - the stage object used to display the interface.
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        primaryStage.setTitle("GlideMe");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    /**
     * Main.
     *
     * @param args - arguments passed to the program.
     */
    public static void main(String[] args) {
        spawnWorldLoop();
        launch(args);
    }
}
