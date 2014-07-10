package fluid.physics;

import fluid.entity.FluidEntity;
import javafx.scene.paint.Color;

/**
 * Created by mjanes on 6/29/2014.
 */
public class ExternalInput {

    public static void applyInput(FluidEntity[][] entities, int timestep) {
        neutralizeBorder(entities);
        //inputExplosion(entities, timestep);
        inputHeat(entities);
        inputNeutral(entities);
        inputBreeze(entities);

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
        entity.setDeltaX(0);
        entity.setDeltaY(0);
        entity.setDeltaZ(0);
    }

    private static void smallInput(FluidEntity[][] entities, int timestep) {
        entities[entities.length / 2][0].addMass(5, FluidPhysics.ROOM_TEMPERATURE + 15, Color.RED);
    }

    private static void inputHeat(FluidEntity[][] entities) {
        entities[79][0].addMass(12, FluidPhysics.ROOM_TEMPERATURE + 25, Color.ORANGERED);
        entities[80][0].addMass(20, FluidPhysics.ROOM_TEMPERATURE + 30, Color.RED);
        entities[81][0].addMass(12, FluidPhysics.ROOM_TEMPERATURE + 25, Color.ORANGERED);
    }

    private static void inputNeutral(FluidEntity[][] entities) {
        entities[40][40].setColor(Color.BLACK);
    }

    private static void inputBreeze(FluidEntity[][] entities) {
        entities[0][entities[0].length * 2/3].addMass(3, FluidPhysics.ROOM_TEMPERATURE - 1, Color.BLUE, 2, 0);
        entities[0][entities[0].length * 2/3 + 1].addMass(3, FluidPhysics.ROOM_TEMPERATURE - 1, Color.BLUE, 2, 0);
    }

    private static void inputExplosion(FluidEntity[][] entities, int timestep) {
        FluidEntity entity = entities[80][40];
        if (timestep < 3) {
            entity.addMass(FluidPhysics.DEFAULT_MASS * 100, FluidPhysics.ROOM_TEMPERATURE * 10, Color.RED);
        }
    }

}
