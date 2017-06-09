package fluid.main;

import fluid.camera.Camera;
import fluid.display.FluidEntityCanvas;
import fluid.physics.Universe;
import fluid.setup.Setup;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    private final int FRAME_DELAY = 80;
    private final boolean IS_RUNNING = true;

    private final Universe universe = Setup.create();
    private FluidEntityCanvas canvas;
    private Camera camera;

    private ExecutorService executorService;

    private volatile FluidEntityCanvas.DrawType drawType;

    /**
     * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Fluid simulation");

        camera = new Camera(0, 0, 0);

        StackPane root = new StackPane();

        VBox displayType = getDisplayTypeButtons();
        // TODO: Camera moving buttons

        // Canvas
        canvas = new FluidEntityCanvas(1400, 900, camera);

        HBox parentBox = new HBox();
        parentBox.getChildren().add(displayType);
        parentBox.getChildren().add(canvas);
        parentBox.setPadding(new Insets(20, 20, 20, 20));

        root.getChildren().add(parentBox);

        stage.setScene(new Scene(root));
        stage.show();

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        runSimulation();
    }

    private VBox getDisplayTypeButtons() {
        RadioButton inkButton = new RadioButton("Ink");
        RadioButton massButton = new RadioButton("Mass");
        RadioButton heatButton = new RadioButton("Temperature");
        RadioButton velocityButton = new RadioButton("Velocity");

        ToggleGroup displayTypeGroup = new ToggleGroup();
        inkButton.setToggleGroup(displayTypeGroup);
        massButton.setToggleGroup(displayTypeGroup);
        heatButton.setToggleGroup(displayTypeGroup);
        velocityButton.setToggleGroup(displayTypeGroup);

        displayTypeGroup.selectedToggleProperty().addListener((observableValue, oldToggle, newToggle) -> {
            if (displayTypeGroup.getSelectedToggle().equals(inkButton)) {
                drawType = FluidEntityCanvas.DrawType.INK;
            } else if (displayTypeGroup.getSelectedToggle().equals(massButton)) {
                drawType = FluidEntityCanvas.DrawType.MASS;
            } else if (displayTypeGroup.getSelectedToggle().equals(heatButton)) {
                drawType = FluidEntityCanvas.DrawType.TEMPERATURE;
            } else if (displayTypeGroup.getSelectedToggle().equals(velocityButton)) {
                drawType = FluidEntityCanvas.DrawType.VELOCITY;
            }
        });

        inkButton.setSelected(true);

        VBox box = new VBox();
        box.getChildren().add(inkButton);
        box.getChildren().add(massButton);
        box.getChildren().add(heatButton);
        box.getChildren().add(velocityButton);
        box.setPadding(new Insets(20, 20, 20, 20));

        return box;
    }

    public int getFrameDelay() {
        return FRAME_DELAY;
    }

    private boolean isRunning() {
        return IS_RUNNING;
    }

    private void runSimulation() {
        executorService = Executors.newSingleThreadExecutor();

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        KeyFrame increment = new KeyFrame(Duration.millis(1), e -> increment());
        timeline.getKeyFrames().add(increment);
        timeline.play();
    }


    private void increment() {
        // Perform physics simulations
        if (isRunning()) {

            SimulationTask incrementStep = new SimulationTask(universe);
            incrementStep.setOnSucceeded(e -> {
                camera.move();

                // tell graphics to repaint
                canvas.drawEntities(universe.getEntities(), drawType);
            });

            incrementStep.setOnFailed(e -> System.out.println("Error: " + e.toString()));

            executorService.submit(incrementStep);
        }
    }


    private static class SimulationTask extends Task<Void> {

        final Universe universe;

        SimulationTask(Universe universe) {
            this.universe = universe;
        }

        @Override
        protected Void call() throws Exception {
            universe.updateUniverseState();
            return null;
        }
    }

}
