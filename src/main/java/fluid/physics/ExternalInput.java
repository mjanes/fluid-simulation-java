package fluid.physics;

import fluid.entity.FluidEntity;
import javafx.scene.paint.Color;

/**
 * Created by mjanes on 6/29/2014.
 */
public class ExternalInput {



    public static void applyInput(FluidEntity[][] entities, int timestep) {
        //inputOriginal(entities);
        inputHeat(entities);
        //inputExplosion(entities, timestep);
    }

    private static void inputExplosion(FluidEntity[][] entities, int timestep) {
        if (timestep < 5) {
            entities[70][20].addHeat(5000);
            entities[70][20].setInk(1000, Color.RED);
        } else if (timestep < 50) {
            entities[70][20].addHeat(2000);
            entities[70][20].setInk(1000, Color.RED);
        }
    }

    private static void inputOriginal(FluidEntity[][] entities, int timestep) {
        double factor = Math.abs(Math.sin(Math.toRadians(timestep * 3)));

        entities[0][0].setMass(factor * 600 + 50);
        entities[0][0].setInk(entities[0][0].getMass() * factor, Color.ORANGE);
        entities[0][0].applyForceX(factor * 14);
        entities[0][0].applyForceY(factor * 8);
        entities[0][0].setHeat(factor * 20);

        entities[1][0].setMass(factor * 500 + 20);
        entities[1][0].setInk(entities[1][0].getMass() * factor, Color.RED);
        entities[1][0].applyForceX(factor * 11);
        entities[1][0].applyForceY(factor * 7);
        entities[1][0].setHeat(10);

        factor = Math.abs(Math.sin(Math.toRadians(timestep * 2)));
        entities[0][1].setMass(factor * 500 + 100);
        entities[0][1].setInk(factor * 500 + 100, Color.ORANGE);
        entities[0][1].addDeltaX(12);
        entities[0][1].addDeltaY(7);
        entities[0][1].setHeat(10);

        entities[1][1].setMass(factor * 200);
        entities[1][1].setInk(factor * 200, Color.YELLOW);
        entities[1][1].addDeltaX(12);
        entities[1][1].addDeltaY(6);
        entities[1][1].setHeat(factor * 20);


        factor = Math.abs(Math.cos(Math.toRadians(timestep * 2)));
        entities[120][80].setMass(600 * factor);
        entities[120][80].setDeltaX(-30 * factor + 10);
        entities[120][80].setDeltaY(-4);
        entities[120][80].setInk(600, Color.BLUE);


        factor = Math.abs(Math.sin(Math.toRadians(timestep)));
        entities[100][10].setMass(400 * (1 - factor));
        entities[100][10].setDeltaX(-20 * factor + 5);
        entities[100][10].setDeltaY(4);
        entities[100][10].setHeat((1 - factor) * 20);
        entities[100][10].setInk(600, Color.GREEN);
    }

    private static void inputHeat(FluidEntity[][] entities) {
        // Hot
        entities[60][0].setMass(400);
        entities[79][0].setHeat(90);
        entities[79][0].setInk(100, Color.RED);
        entities[79][0].setMass(10);
        entities[80][0].setHeat(110);
        entities[80][0].setInk(200, Color.RED);
        entities[80][0].setMass(10);
        entities[81][0].setHeat(90);
        entities[81][0].setInk(100, Color.RED);
        entities[81][0].setMass(10);

        // Cold
        entities[10][0].setInk(10, Color.BLUE);
        entities[10][0].setMass(10);
        entities[10][0].setHeat(0);
        entities[20][0].setInk(10, Color.BLUE);
        entities[20][0].setMass(10);
        entities[30][0].setHeat(0);
        entities[30][0].setInk(10, Color.BLUE);
        entities[30][0].setMass(10);
        entities[30][0].setHeat(0);
        entities[40][0].setInk(10, Color.BLUE);
        entities[40][0].setMass(10);
        entities[40][0].setHeat(0);
        entities[50][0].setInk(10, Color.BLUE);
        entities[50][0].setMass(10);
        entities[50][0].setHeat(0);
        entities[60][0].setInk(10, Color.BLUE);
        entities[60][0].setMass(10);
        entities[60][0].setHeat(0);
        entities[70][0].setInk(10, Color.BLUE);
        entities[70][0].setMass(10);
        entities[70][0].setHeat(0);
        entities[90][0].setInk(10, Color.BLUE);
        entities[90][0].setMass(10);
        entities[90][0].setHeat(0);
        entities[100][0].setInk(10, Color.BLUE);
        entities[100][0].setMass(10);
        entities[100][0].setHeat(0);
        entities[110][0].setInk(10, Color.BLUE);
        entities[110][0].setMass(10);
        entities[110][0].setHeat(0);
        entities[120][0].setInk(10, Color.BLUE);
        entities[120][0].setMass(10);
        entities[120][0].setHeat(0);
        entities[130][0].setInk(10, Color.BLUE);
        entities[130][0].setMass(10);
        entities[130][0].setHeat(0);


        // Neutral
        entities[40][40].setInk(40, Color.BLACK);
    }
}
