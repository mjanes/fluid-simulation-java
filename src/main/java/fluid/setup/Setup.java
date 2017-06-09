package fluid.setup;

import fluid.entity.FluidEntity;
import fluid.entity.OpenMockFluidEntity;
import fluid.entity.ReflectiveMockFluidEntity;
import fluid.physics.Universe;
import javafx.scene.paint.Color;

import java.util.stream.IntStream;

/**
 * Created by mjanes on 6/14/2014.
 */
public class Setup {

    private static final double Z_DISTANCE = 5000;

    private static final int SIZE = 160;

    public static Universe create() {
        /**
         * NOTE: Because of the possibly effect of gravity, causing pressure to increase downwards, and since all of
         * these are starting with same mass and pressure everyhere, it will take a while to come to an equilibrium.
         *
         * Of course, the mock fluid entities we're using at the moment all have default mass, so the borders will also
         * provide an instability until we tweak that.
         */

        FluidEntity[][] entities = rectangle(SIZE, SIZE);
        //FluidEntity[][] entities = rayleighTaylor(SIZE);

        // Set boundary conditions of the universe
        FluidEntity otherEntity;
        // Right border
        for (int y = 0; y < entities[entities.length - 1].length; y++) {
            otherEntity = entities[entities.length - 1][y];
            entities[entities.length - 1][y] = new OpenMockFluidEntity(otherEntity.getX(), otherEntity.getY(), otherEntity.getZ());
        }
        // Left border
        for (int y = 0; y < entities[0].length; y++) {
            otherEntity = entities[0][y];
            entities[0][y] = new OpenMockFluidEntity(otherEntity.getX(), otherEntity.getY(), otherEntity.getZ());
        }
        // Top border
        for (int x = 0; x < entities.length; x++) {
            otherEntity = entities[x][entities[x].length - 1];
            entities[x][entities[x].length - 1] = new OpenMockFluidEntity(otherEntity.getX(), otherEntity.getY(), otherEntity.getZ());
        }
        // Bottom border
        for (int x = 0; x < entities.length; x++) {
            otherEntity = entities[x][0];
            entities[x][0] = new ReflectiveMockFluidEntity(otherEntity.getX(), otherEntity.getY(), otherEntity.getZ());
        }

        return new Universe(entities);
        //return rectangle(300, 50);
        //return rayleighTaylor(SIZE);
    }

    private static FluidEntity[][] rectangle(int width, int height) {
        FluidEntity[][] entities = new FluidEntity[width][height];

        IntStream.range(0, width).forEach(i -> {

            double x = (i - width / 2) * FluidEntity.SPACE;

            IntStream.range(0, height).forEach(j -> {
                double y = (j - height / 2) * FluidEntity.SPACE;

                FluidEntity entity = new FluidEntity(x, y, Z_DISTANCE, FluidEntity.DEFAULT_MASS, FluidEntity.DEFAULT_TEMPERATURE);
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
                double z = Z_DISTANCE;

                FluidEntity entity;
                if (j > numEntitiesOnSide / 2) {
                    entity = new FluidEntity(x, y, z, FluidEntity.DEFAULT_MASS * 4, FluidEntity.DEFAULT_TEMPERATURE);
                    entity.setColor(Color.BLUE);
                    if (Math.random() < .01) {
                        entity.addMass(FluidEntity.DEFAULT_MASS, FluidEntity.DEFAULT_MASS, Color.BLACK);
                    }
                } else {
                    entity = new FluidEntity(x, y, z, FluidEntity.DEFAULT_MASS / 4, FluidEntity.DEFAULT_TEMPERATURE);
                    entity.setColor(Color.RED);
                }
                entities[i][j] = entity;
            }
        });

        return entities;
    }
}
