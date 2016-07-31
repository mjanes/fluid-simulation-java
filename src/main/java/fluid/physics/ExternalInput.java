package fluid.physics;

import fluid.entity.FluidEntity;
import fluid.entity.IFluidEntity;
import javafx.scene.paint.Color;

/**
 * Created by mjanes on 6/29/2014.
 */
class ExternalInput {

    static void applyInput(FluidEntity[][] entities, int timestep) {
        //inputExplosion(entities, timestep);

        //inputCandle(entities);
        //inputBreeze(entities);

        inputHeat(entities, timestep);

        //smallInput(entities, timestep);

        //coolUpperBorder(entities);
        //inputHotplate(entities, timestep);

        //inputBreezeOnHalf(entities);

        //kelvinHelmholtz(entities);
    }

    private static void smallInput(FluidEntity[][] entities, int timestep) {
        entities[entities.length / 2][0].addMass(5, IFluidEntity.DEFAULT_TEMPERATURE + 15, Color.RED);
    }

    private static void inputCandle(FluidEntity[][] entities) {
        entities[79][0].addMass(12, IFluidEntity.DEFAULT_TEMPERATURE + 25, Color.ORANGERED, 0, 1);
        entities[80][0].addMass(20, IFluidEntity.DEFAULT_TEMPERATURE + 30, Color.RED, 0, 1);
        entities[81][0].addMass(12, IFluidEntity.DEFAULT_TEMPERATURE + 25, Color.ORANGERED, 0, 1);
    }

    private static void inputHeat(FluidEntity[][] entities, int step) {
        entities[entities.length / 2 - 1][0].addHeat(IFluidEntity.DEFAULT_TEMPERATURE + 25);
        entities[entities.length / 2 - 1][0].setColor(Color.ORANGERED);
        entities[entities.length / 2][0].addHeat(IFluidEntity.DEFAULT_TEMPERATURE + 30);
        entities[entities.length / 2][0].setColor(Color.RED);
        entities[entities.length / 2 + 1][0].addHeat(IFluidEntity.DEFAULT_TEMPERATURE + 25);
        entities[entities.length / 2 + 1][0].setColor(Color.ORANGERED);
    }

    private static void inputBreeze(FluidEntity[][] entities) {
        inputBreezeOnEntity(entities[0][entities[0].length * 2 / 3]);
        inputBreezeOnEntity(entities[0][entities[0].length * 2 / 3 + 1]);
    }

    public static void kelvinHelmholtz(FluidEntity[][] entities) {
        for (int i = 0; i < entities[0].length; i++) {
            if (i < entities[0].length / 3) {
                inputInverseBreezeOnEntity(entities[entities.length - 1][i]);
            } else if (i > 2 * entities[0].length / 3) {
                inputBreezeOnEntity(entities[0][i]);
            }
        }
    }

    private static void inputBreezeOnEntity(FluidEntity entity) {
        //entity.addMass(1, IFluidEntity.DEFAULT_TEMPERATURE + 2, Color.RED, 6, 0);
        entity.setDeltaX(3);
        entity.setColor(Color.WHITE);
    }

    private static void inputInverseBreezeOnEntity(FluidEntity entity) {
        entity.addMass(2, IFluidEntity.DEFAULT_TEMPERATURE / 10, Color.BLUE, -2, 0);
//        entity.setDeltaX(-3);
//        entity.setColor(Color.BLUE);
    }

    private static void inputExplosion(FluidEntity[][] entities, int timestep) {
        FluidEntity entity = entities[80][40];
        if (timestep < 3) {
            entity.addMass(IFluidEntity.DEFAULT_MASS * 100, IFluidEntity.DEFAULT_TEMPERATURE * 10, Color.RED);
        }
    }

    /**
     * https://en.wikipedia.org/wiki/Rayleigh%E2%80%93B%C3%A9nard_convection
     */
    private static void inputHotplate(FluidEntity[][] entities, int timestep) {
        for (FluidEntity[] entityRow : entities) {
            entityRow[0].setTemperature(IFluidEntity.DEFAULT_TEMPERATURE * 2);
        }
    }

    private static void coolUpperBorder(FluidEntity[][] entities) {
        for (FluidEntity[] entityRow : entities) {
            entityRow[entityRow.length - 1].setTemperature(IFluidEntity.DEFAULT_TEMPERATURE / 2);
        }
    }

}
