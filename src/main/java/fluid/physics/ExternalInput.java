package fluid.physics;

import fluid.entity.FluidEntity;
import javafx.scene.paint.Color;

/**
 * Created by mjanes on 6/29/2014.
 */
public class ExternalInput {

    public static void applyInput(FluidEntity[][] entities, int timestep) {
        //neutralizeBorder(entities);
        coolUpperBorder(entities);
        //inputExplosion(entities, timestep);

        //inputCandle(entities);
        //inputBreeze(entities);

        //inputHeat(entities, timestep);
        inputHotplate(entities, timestep);

        //smallInput(entities, timestep);
    }

    /**
     * TODO: Now that we're doing pressure differential from top to bottom, need to improve how this done.
     */
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

    public static void setNeutral(FluidEntity entity) {
        entity.setMass(FluidPhysics.DEFAULT_MASS);
        entity.setTemperature(FluidPhysics.ROOM_TEMPERATURE);
        entity.setDeltaX(0);
        entity.setDeltaY(0);
        entity.setDeltaZ(0);
    }

    private static void smallInput(FluidEntity[][] entities, int timestep) {
        entities[entities.length / 2][0].addMass(5, FluidPhysics.ROOM_TEMPERATURE + 15, Color.RED);
    }

    private static void inputCandle(FluidEntity[][] entities) {
        entities[79][0].addMass(12, FluidPhysics.ROOM_TEMPERATURE + 25, Color.ORANGERED, 0, 1);
        entities[80][0].addMass(20, FluidPhysics.ROOM_TEMPERATURE + 30, Color.RED, 0, 1);
        entities[81][0].addMass(12, FluidPhysics.ROOM_TEMPERATURE + 25, Color.ORANGERED, 0, 1);
    }

    private static void inputHeat(FluidEntity[][] entities, int step) {
        entities[79][0].addHeat(FluidPhysics.ROOM_TEMPERATURE + 25);
        entities[79][0].setColor(Color.ORANGERED);
        entities[80][0].addHeat(FluidPhysics.ROOM_TEMPERATURE + 30);
        entities[80][0].setColor(Color.RED);
        entities[81][0].addHeat(FluidPhysics.ROOM_TEMPERATURE + 25);
        entities[81][0].setColor(Color.ORANGERED);
    }

    private static void inputNeutral(FluidEntity[][] entities) {
        entities[40][40].setColor(Color.BLACK);
    }

    private static void inputBreeze(FluidEntity[][] entities) {
        entities[0][entities[0].length * 2/3].addMass(5, FluidPhysics.ROOM_TEMPERATURE - 1, Color.BLUE, 4, 0);
        entities[0][entities[0].length * 2/3 + 1].addMass(5, FluidPhysics.ROOM_TEMPERATURE - 1, Color.BLUE, 4, 0);
    }

    private static void inputExplosion(FluidEntity[][] entities, int timestep) {
        FluidEntity entity = entities[80][40];
        if (timestep < 3) {
            entity.addMass(FluidPhysics.DEFAULT_MASS * 100, FluidPhysics.ROOM_TEMPERATURE * 10, Color.RED);
        }
    }

    /**
     * https://en.wikipedia.org/wiki/Rayleigh%E2%80%93B%C3%A9nard_convection
     */
    private static void inputHotplate(FluidEntity[][] entities, int timestep) {
        for (FluidEntity[] entityRow : entities) {
            // bottom side
            entityRow[0].setTemperature(FluidPhysics.ROOM_TEMPERATURE * 2);
        }
    }

    private static void coolUpperBorder(FluidEntity[][] entities) {
        for (FluidEntity[] entityRow : entities) {
            entityRow[entityRow.length - 1].setTemperature(FluidPhysics.ROOM_TEMPERATURE / 2);
        }
    }

}
