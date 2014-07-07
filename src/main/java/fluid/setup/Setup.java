package fluid.setup;

import fluid.entity.FluidEntity;
import fluid.physics.FluidPhysics;

import java.util.stream.IntStream;

/**
 * Created by mjanes on 6/14/2014.
 */
public class Setup {

    private static double sZDistance = 5000;

    private static final int SIZE = 175;
    public static FluidEntity[][] create() {
        return grid(SIZE);
    }

    private static FluidEntity[][] grid(int numEntitiesOnSide) {
        FluidEntity[][] entities = new FluidEntity[numEntitiesOnSide][numEntitiesOnSide];

        IntStream.range(0, numEntitiesOnSide).forEach(i -> {

            double x = (i - numEntitiesOnSide / 2) * FluidEntity.SPACE;

            IntStream.range(0, numEntitiesOnSide).forEach(j -> {
                double y = (j - numEntitiesOnSide / 2) * FluidEntity.SPACE;

                double z = sZDistance;

                FluidEntity entity = new FluidEntity(x, y, z, 10, FluidPhysics.ROOM_TEMPERATURE);
                entities[i][j] = entity;
            });
        });

        return entities;
    }
}
