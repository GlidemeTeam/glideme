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
    private Button bStart;

    @FXML
    private Button bStop;

    @FXML
    private Pane pane;

    private Task task;

    private int lineLenght;

    public MainWindow() {}

    public MainWindow(World world)
    {
        this.world = world;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        bStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                 task = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        Main.runWorld(true);

                        while(true) {
                          //  System.out.println(task.isCancelled());
                            if (!task.isCancelled()) {
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        drawWindow();
                                    }
                                });
                                try {
                                    Thread.sleep(world.TIME_QUANTUM);
                                }
                                catch (InterruptedException _) {
                                    return null;
                                }
                            }
                            else
                                return null;
                        }
                    }
                };

                Thread th = new Thread(task);
                th.setDaemon(true);
                th.start();
            }
        });

        bStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.runWorld(false);
                task.cancel();
            }
        });

        lineLenght=(int)(Math.abs(rope.getStartY())+Math.abs(rope.getEndY()));

        pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(task!=null && !task.isCancelled()) {
                    world.setDestination((int) (event.getSceneX() - 300)); //300 is a position of trolley - to change

                }
            }
        });
    }

    public void drawWindow(){
        trolley.setX(world.getCraneState().position);
        wheel.setCenterX(world.getCraneState().position);
        weight.setCenterX(world.getCraneState().position+lineLenght*Math.sin(world.getCraneState().angle));
        weight.setCenterY(-lineLenght * (1 - Math.cos(world.getCraneState().angle)));
        rope.setStartX(world.getCraneState().position);
        rope.setEndX(world.getCraneState().position + lineLenght * Math.sin(world.getCraneState().angle));
        rope.setEndY(lineLenght * (Math.cos(world.getCraneState().angle)));
        rail.setStartX(-World.TRACK_LENGTH);
        rail.setEndX(World.TRACK_LENGTH);
    }
}
