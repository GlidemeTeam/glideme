package glideme;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    /**
     * The state of the world.
     */
    private static World world = new World();
    private static MainWindow mainWindow = new MainWindow();

    /**
     * World-updating background task.
     */
    private static Thread worldThread;

    /**
     * Should world be running?
     */
    private static boolean worldRunning = false;

    /**
     * Spawn the main world-updating loop in a new thread.
     */
    private static void spawnWorldLoop() {
        worldThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (worldRunning) {
                        world.refresh();
                    }

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
        mainWindow = new MainWindow(world);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));

        fxmlLoader.setRoot(mainWindow);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        primaryStage.setScene(new Scene(mainWindow));
        primaryStage.setTitle("GlideMe");
        primaryStage.show();
    }

    /**
     * Notify the world-updating thread that it can start processing.
     */
    public static void runWorld(final boolean run) {
        worldRunning = run;
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
