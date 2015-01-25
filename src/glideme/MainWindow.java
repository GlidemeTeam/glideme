package glideme;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.EventListener;
import java.util.ResourceBundle;

public class MainWindow extends VBox implements Initializable {
    private static World world;

    @FXML
    private Rectangle trolley;
    @FXML
    private Circle wheel;
    @FXML
    private Line rail;
    @FXML
    private Line rope;
    @FXML
    private Circle weight;

    @FXML
    private Pane pane;

    private Task task=null;

    private double lineLenght;
    private double railLength;

    public MainWindow() {}

    public MainWindow(World world)
    {
        this.world = world;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        lineLenght = Math.abs(rope.getStartY()) + Math.abs(rope.getEndY());
        railLength = rail.getEndX() - rail.getStartX();

        pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(task==null)
                {
                    task = new Task() {
                        @Override
                        protected Object call() throws Exception {
                            Main.runWorld(true);

                            while (true) {
                                //  System.out.println(task.isCancelled());
                                if (!task.isCancelled()) {
                                    Platform.runLater(new Runnable() {
                                        public void run() {
                                            drawWindow();
                                        }
                                    });
                                    try {
                                        Thread.sleep(world.TIME_QUANTUM);
                                    } catch (InterruptedException _) {
                                        return null;
                                    }
                                } else
                                    return null;
                            }
                        }
                    };

                    Thread th = new Thread(task);
                    th.setDaemon(true);
                    th.start();
                }

                if (task != null && !task.isCancelled()) {
                    double x = event.getSceneX();
                    if (x > railLength) {
                        x = railLength;
                    }

                    world.setDestination((x / railLength) * (double) World.TRACK_LENGTH);
                }
            }
        });
    }

    public void drawWindow() {
        final World.CraneState state = world.getCraneState();
        final double pos = state.position,
                angle = state.angle;

        final double trolleyX = rail.getStartX() + (pos/World.TRACK_LENGTH)*railLength;

        trolley.setX(trolleyX);
        wheel.setCenterX(trolleyX);

        weight.setCenterX(trolleyX + lineLenght * Math.sin(angle));
        weight.setCenterY(-lineLenght * (1 - Math.cos(angle)));

        rope.setStartX(trolleyX);
        rope.setEndX(trolleyX + lineLenght * Math.sin(angle));
        rope.setEndY(lineLenght * (Math.cos(angle)));
    }
}
