package fluid.physics;

import fluid.entity.FluidEntity;
import javafx.scene.paint.Color;

/**
 * Created by mjanes on 6/29/2014.
 */
class ExternalInput {

    static void applyInput(FluidEntity[][] entities, int timestep) {
        //inputExplosion(entities, timestep);

        if (timestep > 100) {
            inputCandle(entities);
        }
        //inputBreeze(entities);

        //inputHeat(entities, timestep);

        //smallInput(entities, timestep);

        //coolUpperBorder(entities);
        //inputHotplate(entities, timestep);

        //inputBreezeOnHalf(entities);

        //kelvinHelmholtz(entities);
    }

    private static void smallInput(FluidEntity[][] entities, int timestep) {
        entities[entities.length / 2][0].recordMassChange(5);
        entities[entities.length / 2][0].recordHeatChange(15);
        entities[entities.length / 2][0].setColor(Color.RED);
    }

    private static void inputCandle(FluidEntity[][] entities) {
        entities[entities.length / 2 - 1][1].recordMassChange(2);
        entities[entities.length / 2 - 1][1].recordHeatChange(25);
        entities[entities.length / 2 - 1][1].setColor(Color.ORANGERED);

        entities[entities.length / 2][1].recordMassChange(2.5);
        entities[entities.length / 2][1].recordHeatChange(30);
        entities[entities.length / 2][1].setColor(Color.RED);

        entities[entities.length / 2 + 1][1].recordMassChange(2);
        entities[entities.length / 2 + 1][1].recordHeatChange(25);
        entities[entities.length / 2 + 1][1].setColor(Color.ORANGERED);
    }

    private static void inputHeat(FluidEntity[][] entities, int step) {
        entities[entities.length / 2 - 1][1].recordHeatChange(20);
        entities[entities.length / 2 - 1][1].setColor(Color.ORANGERED);
        entities[entities.length / 2][1].recordHeatChange(25);
        entities[entities.length / 2][1].setColor(Color.RED);
        entities[entities.length / 2 + 1][1].recordHeatChange(20);
        entities[entities.length / 2 + 1][1].setColor(Color.ORANGERED);
    }

    private static void inputBreeze(FluidEntity[][] entities) {
        inputBreezeOnEntity(entities[0][entities[0].length * 2 / 3]);
        inputBreezeOnEntity(entities[0][entities[0].length * 2 / 3 + 1]);
    }

    /*
    public static void kelvinHelmholtz(FluidEntity[][] entities) {
        for (int i = 0; i < entities[0].length; i++) {
            if (i < entities[0].length / 3) {
                inputInverseBreezeOnEntity(entities[entities.length - 1][i]);
            } else if (i > 2 * entities[0].length / 3) {
                inputBreezeOnEntity(entities[0][i]);
            }
        }
    }
    */

    private static void inputBreezeOnEntity(FluidEntity entity) {
        //entity.addMass(1, FluidEntity.DEFAULT_TEMPERATURE + 2, Color.RED, 6, 0);
        entity.recordForceChange(3, 0);
        entity.setColor(Color.WHITE);
    }

    /*
    private static void inputInverseBreezeOnEntity(FluidEntity entity) {
        entity.addMass(2, FluidEntity.DEFAULT_TEMPERATURE / 10, -2, 0, Color.BLUE);
//        entity.setForceX(-3);
//        entity.setColor(Color.BLUE);
    }

    private static void inputExplosion(FluidEntity[][] entities, int timestep) {
        FluidEntity entity = entities[80][40];
        if (timestep < 3) {
            entity.addMass(FluidEntity.DEFAULT_MASS * 100, FluidEntity.DEFAULT_TEMPERATURE * 10, Color.RED);
        }
    }
    */

    /**
     * https://en.wikipedia.org/wiki/Rayleigh%E2%80%93B%C3%A9nard_convection
     */
    private static void inputHotplate(FluidEntity[][] entities, int timestep) {
        for (FluidEntity[] entityRow : entities) {
            entityRow[0].setHeat(FluidEntity.DEFAULT_TEMPERATURE * 2);
        }
    }

    private static void coolUpperBorder(FluidEntity[][] entities) {
        for (FluidEntity[] entityRow : entities) {
            entityRow[entityRow.length - 1].setHeat(FluidEntity.DEFAULT_TEMPERATURE / 2);
        }
    }

}
