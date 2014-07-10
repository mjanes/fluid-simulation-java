package fluid.setup;

import fluid.entity.FluidEntity;
import fluid.physics.FluidPhysics;
import javafx.scene.paint.Color;

import java.util.stream.IntStream;

/**
 * Created by mjanes on 6/14/2014.
 */
public class Setup {

    private static double sZDistance = 5000;

    private static final int SIZE = 175;
    public static FluidEntity[][] create() {
        return grid(SIZE);
        //return rayleighTaylor(SIZE);
    }

    private static FluidEntity[][] grid(int numEntitiesOnSide) {
        FluidEntity[][] entities = new FluidEntity[numEntitiesOnSide][numEntitiesOnSide];

        IntStream.range(0, numEntitiesOnSide).forEach(i -> {

            double x = (i - numEntitiesOnSide / 2) * FluidEntity.SPACE;

            IntStream.range(0, numEntitiesOnSide).forEach(j -> {
                double y = (j - numEntitiesOnSide / 2) * FluidEntity.SPACE;
                double z = sZDistance;

                FluidEntity entity = new FluidEntity(x, y, z, FluidPhysics.DEFAULT_MASS, FluidPhysics.ROOM_TEMPERATURE);
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
                    entity = new FluidEntity(x, y, z, FluidPhysics.DEFAULT_MASS * 4, FluidPhysics.ROOM_TEMPERATURE);
                    entity.setColor(Color.BLUE);
                    if (Math.random() < .01) {
                        entity.addMass(FluidPhysics.DEFAULT_MASS, FluidPhysics.DEFAULT_MASS, Color.BLACK);
                    }
                } else {
                    entity = new FluidEntity(x, y, z, FluidPhysics.DEFAULT_MASS / 4, FluidPhysics.ROOM_TEMPERATURE);
                    entity.setColor(Color.RED);
                }
                entities[i][j] = entity;
            }
        });

        return entities;
    }
}
