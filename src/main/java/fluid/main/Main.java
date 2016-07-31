package fluid.main;

import fluid.camera.Camera;
import fluid.display.FluidEntityCanvas;
import fluid.entity.FluidEntity;
import fluid.physics.UniversePhysics;
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

    private final int mFrameDelay = 80;
    private final boolean mRunning = true;

    private static final FluidEntity[][] ENTITIES = Setup.create();
    private FluidEntityCanvas mCanvas;
    private Camera mCamera;

    private ExecutorService mExecutorService;

    private volatile FluidEntityCanvas.DrawType mDrawType;

    /**
     * http://cowboyprogramming.com/2008/04/01/practical-fluid-mechanics/
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Fluid simulation");

        mCamera = new Camera(0, 0, 0);

        StackPane root = new StackPane();

        VBox displayType = getDisplayTypeButtons();
        // TODO: Camera moving buttons

        // Canvas
        mCanvas = new FluidEntityCanvas(1400, 900, mCamera);

        HBox parentBox = new HBox();
        parentBox.getChildren().add(displayType);
        parentBox.getChildren().add(mCanvas);
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
                mDrawType = FluidEntityCanvas.DrawType.INK;
            } else if (displayTypeGroup.getSelectedToggle().equals(massButton)) {
                mDrawType = FluidEntityCanvas.DrawType.MASS;
            } else if (displayTypeGroup.getSelectedToggle().equals(heatButton)) {
                mDrawType = FluidEntityCanvas.DrawType.TEMPERATURE;
            } else if (displayTypeGroup.getSelectedToggle().equals(velocityButton)) {
                mDrawType = FluidEntityCanvas.DrawType.VELOCITY;
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
        return mFrameDelay;
    }

    private boolean isRunning() {
        return mRunning;
    }

    private void runSimulation() {
        mExecutorService = Executors.newSingleThreadExecutor();

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        KeyFrame increment = new KeyFrame(Duration.millis(1), e -> increment());
        timeline.getKeyFrames().add(increment);
        timeline.play();
    }


    private void increment() {
        // Perform physics simulations
        if (isRunning()) {

            SimulationTask incrementStep = new SimulationTask();
            incrementStep.setOnSucceeded(e -> {
                mCamera.move();

                // tell graphics to repaint
                mCanvas.drawEntities(ENTITIES, mDrawType);
            });

            incrementStep.setOnFailed(e -> System.out.println(e.toString()));

            mExecutorService.submit(incrementStep);
        }
    }


    private static class SimulationTask extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            UniversePhysics.updateUniverseState(Main.ENTITIES);
            return null;
        }
    }

}
