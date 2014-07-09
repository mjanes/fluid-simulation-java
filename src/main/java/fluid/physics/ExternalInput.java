package fluid.physics;

import fluid.entity.FluidEntity;
import javafx.scene.paint.Color;

/**
 * Created by mjanes on 6/29/2014.
 */
public class ExternalInput {



    public static void applyInput(FluidEntity[][] entities, int timestep) {
        neutralizeBorder(entities);
        //inputOriginal(entities);
        inputHeat(entities);
        //inputExplosion(entities, timestep);
        //smallInput(entities, timestep);
    }

    private static void neutralizeBorder(FluidEntity[][] entities) {
        // left side
        for (FluidEntity entity : entities[0]) {
            setNeutral(entity);
        }

        // right side
        for (FluidEntity entity : entities[entities.length - 1]) {
            setNeutral(entity);
        }

        for (FluidEntity[] entityRow : entities) {
            // bottom side
            setNeutral(entityRow[0]);

            // top side
            setNeutral(entityRow[entityRow.length - 1]);
        }
    }

    private static void setNeutral(FluidEntity entity) {
        entity.setMass(FluidPhysics.DEFAULT_MASS);
        entity.setTemperature(FluidPhysics.ROOM_TEMPERATURE);
    }

    private static void smallInput(FluidEntity[][] entities, int timestep) {
        entities[entities.length / 2][0].addMass(5, FluidPhysics.ROOM_TEMPERATURE + 15, Color.RED);
    }

//    private static void inputExplosion(FluidEntity[][] entities, int timestep) {
//        if (timestep < 5) {
//            entities[70][20].addHeat(5000);
//            entities[70][20].setColor(Color.RED);
//        } else if (timestep < 50) {
//            entities[70][20].addHeat(2000);
//            entities[70][20].setColor(Color.RED);
//        }
//    }

//    private static void inputOriginal(FluidEntity[][] entities, int timestep) {
//        double factor = Math.abs(Math.sin(Math.toRadians(timestep * 3)));
//
//        entities[0][0].setMass(factor * 600 + 50);
//        entities[0][0].setColor(Color.ORANGE);
//        entities[0][0].applyForceX(factor * 14);
//        entities[0][0].applyForceY(factor * 8);
//        entities[0][0].setTemperature(factor * 20);
//
//        entities[1][0].setMass(factor * 500 + 20);
//        entities[1][0].setColor(Color.RED);
//        entities[1][0].applyForceX(factor * 11);
//        entities[1][0].applyForceY(factor * 7);
//        entities[1][0].setTemperature(10);
//
//        factor = Math.abs(Math.sin(Math.toRadians(timestep * 2)));
//        entities[0][1].setMass(factor * 500 + 100);
//        entities[0][1].setColor(Color.ORANGE);
//        entities[0][1].addDeltaX(12);
//        entities[0][1].addDeltaY(7);
//        entities[0][1].setTemperature(10);
//
//        entities[1][1].setMass(factor * 200);
//        entities[1][1].setColor(Color.YELLOW);
//        entities[1][1].addDeltaX(12);
//        entities[1][1].addDeltaY(6);
//        entities[1][1].setTemperature(factor * 20);
//
//
//        factor = Math.abs(Math.cos(Math.toRadians(timestep * 2)));
//        entities[120][80].setMass(600 * factor);
//        entities[120][80].setDeltaX(-30 * factor + 10);
//        entities[120][80].setDeltaY(-4);
//        entities[120][80].setColor(Color.BLUE);
//
//
//        factor = Math.abs(Math.sin(Math.toRadians(timestep)));
//        entities[100][10].setMass(400 * (1 - factor));
//        entities[100][10].setDeltaX(-20 * factor + 5);
//        entities[100][10].setDeltaY(4);
//        entities[100][10].setTemperature((1 - factor) * 20);
//        entities[100][10].setColor(Color.GREEN);
//    }

    private static void inputHeat(FluidEntity[][] entities) {
        // Hot
        entities[79][0].addMass(5, FluidPhysics.ROOM_TEMPERATURE + 25, Color.RED);
        entities[80][0].addMass(5, FluidPhysics.ROOM_TEMPERATURE + 30, Color.RED);
        entities[81][0].addMass(5, FluidPhysics.ROOM_TEMPERATURE + 25, Color.RED);

        // Neutral - smoke
        entities[40][40].setColor(Color.BLACK);
    }

}
