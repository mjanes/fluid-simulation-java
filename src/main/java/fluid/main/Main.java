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
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    private int mFrameDelay = 80;
    private boolean mRunning = true;

    protected static final FluidEntity[][] ENTITIES = Setup.create();
    private FluidEntityCanvas mCanvas;
    private Camera mCamera;

    private ExecutorService mExecutorService;

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

        Group root = new Group();
        mCanvas = new FluidEntityCanvas(1600, 1000, mCamera);

        root.getChildren().add(mCanvas);
        stage.setScene(new Scene(root));
        stage.show();

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        // TODO: Add all the other buttons later, but for now, just start things.
        runSimulation();
    }

    public int getFrameDelay() {
        return mFrameDelay;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void runSimulation() {
        mExecutorService = Executors.newSingleThreadExecutor();

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        KeyFrame increment = new KeyFrame(Duration.millis(1), e -> increment());
        timeline.getKeyFrames().add(increment);
        timeline.play();
    }


    protected void increment() {
        // Perform physics simulations
        if (isRunning()) {

            SimulationTask incrementStep = new SimulationTask();
            incrementStep.setOnSucceeded(e -> {
                mCamera.move();

                // tell graphics to repaint
                mCanvas.drawEntities(ENTITIES, FluidEntityCanvas.DrawType.HEAT);
            });

            incrementStep.setOnFailed(e -> {
                System.out.println(e.toString());
            });

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
