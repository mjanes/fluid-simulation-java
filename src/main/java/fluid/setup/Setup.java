package fluid.setup;

import fluid.entity.FluidEntity;
import fluid.entity.IFluidEntity;
import javafx.scene.paint.Color;

import java.util.stream.IntStream;

/**
 * Created by mjanes on 6/14/2014.
 */
public class Setup {

    private static double sZDistance = 5000;

    private static final int SIZE = 160;

    public static FluidEntity[][] create() {
        return square(SIZE);
        //return rectangle(300, 50);
        //return rayleighTaylor(SIZE);
    }

    private static FluidEntity[][] square(int numEntitiesOnSide) {
        FluidEntity[][] entities = new FluidEntity[numEntitiesOnSide][numEntitiesOnSide];

        IntStream.range(0, numEntitiesOnSide).forEach(i -> {

            double x = (i - numEntitiesOnSide / 2) * FluidEntity.SPACE;

            IntStream.range(0, numEntitiesOnSide).forEach(j -> {
                double y = (j - numEntitiesOnSide / 2) * FluidEntity.SPACE;
                double z = sZDistance;

                FluidEntity entity = new FluidEntity(x, y, z, IFluidEntity.DEFAULT_MASS, IFluidEntity.DEFAULT_TEMPERATURE);
                entities[i][j] = entity;
            });
        });

        return entities;
    }

    private static FluidEntity[][] rectangle(int width, int height) {
        FluidEntity[][] entities = new FluidEntity[width][height];

        IntStream.range(0, width).forEach(i -> {

            double x = (i - width / 2) * FluidEntity.SPACE;

            IntStream.range(0, height).forEach(j -> {
                double y = (j - height / 2) * FluidEntity.SPACE;
                double z = sZDistance;

                FluidEntity entity = new FluidEntity(x, y, z, IFluidEntity.DEFAULT_MASS, IFluidEntity.DEFAULT_TEMPERATURE);
                entities[i][j] = entity;
            });
        });

        return entities;
    }


    /**
     * https://en.wikipedia.org/wiki/Rayleigh%E2%80%93Taylor_instability
     */
    private static FluidEntity[][] rayleighTaylor(int numEntitiesOnSide) {
        FluidEntity[][] entities = new FluidEntity[numEntitiesOnSide][numEntitiesOnSide];

        IntStream.range(0, numEntitiesOnSide).forEach(i -> {

            double x = (i - numEntitiesOnSide / 2) * FluidEntity.SPACE;

            for (int j = numEntitiesOnSide - 1; j >= 0; j--) {

                double y = (j - numEntitiesOnSide / 2) * FluidEntity.SPACE;
                double z = sZDistance;

                FluidEntity entity;
                if (j > numEntitiesOnSide / 2) {
                    entity = new FluidEntity(x, y, z, IFluidEntity.DEFAULT_MASS * 4, IFluidEntity.DEFAULT_TEMPERATURE);
                    entity.setColor(Color.BLUE);
                    if (Math.random() < .01) {
                        entity.addMass(IFluidEntity.DEFAULT_MASS, IFluidEntity.DEFAULT_MASS, Color.BLACK);
                    }
                } else {
                    entity = new FluidEntity(x, y, z, IFluidEntity.DEFAULT_MASS / 4, IFluidEntity.DEFAULT_TEMPERATURE);
                    entity.setColor(Color.RED);
                }
                entities[i][j] = entity;
            }
        });

        return entities;
    }
}
