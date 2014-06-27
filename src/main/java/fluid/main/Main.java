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

    private int mFrameDelay = 20;
    private boolean mRunning = true;

    protected FluidEntity[][] mEntities;
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

        // TODO: Add all the other buttons later, but for now, just start things.
        mEntities = Setup.create();

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
        KeyFrame increment = new KeyFrame(Duration.millis(getFrameDelay()), e -> increment());
        timeline.getKeyFrames().add(increment);
        timeline.play();
    }


    protected void increment() {
        // Perform physics simulations
        if (isRunning()) {
            //mEntities = UniversePhysics.updateUniverseState(mEntities);
            SimulationTask incrementStep = new SimulationTask(mEntities);
            incrementStep.setOnSucceeded(e -> {
                mEntities = incrementStep.getValue();

                Platform.runLater(() -> {
                    // TODO: Perhaps create a separate pause camera button?
                    mCamera.move();

                    // tell graphics to repaint

                    double time = System.currentTimeMillis();
                    mCanvas.drawEntities(mEntities);
                    System.out.println("Time to draw entities: " + (System.currentTimeMillis() - time));
                });
            });

            incrementStep.setOnFailed(e ->  {
                System.out.println(e.toString());
            });
            mExecutorService.submit(incrementStep);
        }
    }


    private static class SimulationTask extends Task<FluidEntity[][]> {

        FluidEntity[][] mEntities;

        public SimulationTask(FluidEntity[][] entities) {
            mEntities = entities;
        }

        @Override
        protected FluidEntity[][] call() throws Exception {
            return UniversePhysics.updateUniverseState(mEntities);
        }
    }
}
