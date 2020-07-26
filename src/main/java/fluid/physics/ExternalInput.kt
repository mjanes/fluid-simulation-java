package fluid.physics

import fluid.entity.FluidEntity
import javafx.scene.paint.Color

/**
 * Created by mjanes on 6/29/2014.
 */
internal object ExternalInput {
    fun applyInput(entities: Array<Array<FluidEntity>>, timestep: Int) {
        //inputExplosion(entities, timestep);
        if (timestep > 100) {
            inputCandle(entities)
        }
        //inputBreeze(entities);

        //inputHeat(entities, timestep);

        //smallInput(entities, timestep);

        //coolUpperBorder(entities);
        //inputHotplate(entities, timestep);

        //inputBreezeOnHalf(entities);

        //kelvinHelmholtz(entities);
    }

    private fun smallInput(entities: Array<Array<FluidEntity>>, timestep: Int) {
        entities[entities.size / 2][0].addMass(5.0, FluidEntity.DEFAULT_TEMPERATURE + 15, Color.RED)
    }

    private fun inputCandle(entities: Array<Array<FluidEntity>>) {
        entities[entities.size / 2 - 1][1].addMass(2.0, FluidEntity.DEFAULT_TEMPERATURE + 25, 0.0, 0.0, Color.ORANGERED)
        entities[entities.size / 2][1].addMass(2.5, FluidEntity.DEFAULT_TEMPERATURE + 30, 0.0, 0.0, Color.RED)
        entities[entities.size / 2 + 1][1].addMass(2.0, FluidEntity.DEFAULT_TEMPERATURE + 25, 0.0, 0.0, Color.ORANGERED)
    }

    private fun inputHeat(entities: Array<Array<FluidEntity>>, step: Int) {
        entities[entities.size / 2 - 1][1].addHeat(20.0)
        entities[entities.size / 2 - 1][1].color = Color.ORANGERED
        entities[entities.size / 2][1].addHeat(25.0)
        entities[entities.size / 2][1].color = Color.RED
        entities[entities.size / 2 + 1][1].addHeat(20.0)
        entities[entities.size / 2 + 1][1].color = Color.ORANGERED
    }

    private fun inputBreeze(entities: Array<Array<FluidEntity>>) {
        inputBreezeOnEntity(entities[0][entities[0].size * 2 / 3])
        inputBreezeOnEntity(entities[0][entities[0].size * 2 / 3 + 1])
    }

    fun kelvinHelmholtz(entities: Array<Array<FluidEntity>>) {
        for (i in 0 until entities[0].size) {
            if (i < entities[0].size / 3) {
                inputInverseBreezeOnEntity(entities[entities.size - 1][i])
            } else if (i > 2 * entities[0].size / 3) {
                inputBreezeOnEntity(entities[0][i])
            }
        }
    }

    private fun inputBreezeOnEntity(entity: FluidEntity) {
        //entity.addMass(1, FluidEntity.DEFAULT_TEMPERATURE + 2, Color.RED, 6, 0);
        entity.deltaX = 3.0
        entity.color = Color.WHITE
    }

    private fun inputInverseBreezeOnEntity(entity: FluidEntity) {
        entity.addMass(2.0, FluidEntity.DEFAULT_TEMPERATURE / 10, -2.0, 0.0, Color.BLUE)
        //        entity.setDeltaX(-3);
//        entity.setColor(Color.BLUE);
    }

    private fun inputExplosion(entities: Array<Array<FluidEntity>>, timestep: Int) {
        val entity = entities[80][40]
        if (timestep < 3) {
            entity.addMass(FluidEntity.DEFAULT_MASS * 100, FluidEntity.DEFAULT_TEMPERATURE * 10, Color.RED)
        }
    }

    /**
     * https://en.wikipedia.org/wiki/Rayleigh%E2%80%93B%C3%A9nard_convection
     */
    private fun inputHotplate(entities: Array<Array<FluidEntity>>, timestep: Int) {
        for (entityRow in entities) {
            entityRow[0].temperature = FluidEntity.DEFAULT_TEMPERATURE * 2
        }
    }

    private fun coolUpperBorder(entities: Array<Array<FluidEntity>>) {
        for (entityRow in entities) {
            entityRow[entityRow.size - 1].temperature = FluidEntity.DEFAULT_TEMPERATURE / 2
        }
    }
}