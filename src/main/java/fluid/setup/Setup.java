package fluid.setup;

import fluid.entity.FluidEntity;

import java.util.stream.IntStream;

/**
 * Created by mjanes on 6/14/2014.
 */
public class Setup {

    private static double sZDistance = 5000;

    private static final int SIZE = 5;
    public static FluidEntity[][] create() {
        return grid(SIZE);
    }

    private static FluidEntity[][] grid(int numEntitiesOnSide) {
        FluidEntity[][] entities = new FluidEntity[numEntitiesOnSide][numEntitiesOnSide];

        IntStream.range(0, numEntitiesOnSide).forEach(i -> {

            //double x = (i - numEntitiesOnSide / 2) * FluidEntity.SPACE;
            double x = i * FluidEntity.SPACE;

            IntStream.range(0, numEntitiesOnSide).forEach(j -> {
                //double y = (j - numEntitiesOnSide / 2) * FluidEntity.SPACE;
                double y = j * FluidEntity.SPACE;

                double z = sZDistance;

                FluidEntity entity = new FluidEntity(x, y, z, 10);
                entities[i][j] = entity;
            });
        });

        //entities[0][0].setMass(400);
        //entities[SIZE / 2][SIZE / 2].setMass(100);

        for (int i = 0; i < entities.length; i ++) {
            entities[i][0].setDeltaX(5);
            entities[i][0].setDeltaY(5);
        }

        return entities;
    }
}
