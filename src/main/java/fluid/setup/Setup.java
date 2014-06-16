package fluid.setup;

import fluid.entity.FluidEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by mjanes on 6/14/2014.
 */
public class Setup {

    private static double sZDistance = 5000;

    public static List<FluidEntity> create() {
        ArrayList<FluidEntity> entities = new ArrayList<>();

        entities.addAll(grid(10));

        return entities;
    }

    private static List<FluidEntity> grid(int numEntitiesOnSide) {
        FluidEntity[][][] entitiesArray = new FluidEntity[numEntitiesOnSide][numEntitiesOnSide][numEntitiesOnSide];
        ArrayList<FluidEntity> entitiesList = new ArrayList<>();

        final int SPACE = 10;

        IntStream.range(0, numEntitiesOnSide).forEach(i -> {

                double x = (i - numEntitiesOnSide / 2) * SPACE;

                IntStream.range(0, numEntitiesOnSide).forEach(j -> {
                        double y = (j - numEntitiesOnSide / 2) * SPACE;

                        IntStream.range(0, numEntitiesOnSide).forEach(k -> {

                                double z = sZDistance + (k - numEntitiesOnSide / 2) * SPACE;

                                FluidEntity entity = new FluidEntity(x, y, z);
                                entitiesArray[i][j][k] = entity;

                                if (i > 0) {
                                    entity.addConnection(entitiesArray[i - 1][j][k]);
                                    entitiesArray[i - 1][j][k].addConnection(entity);
                                }
                                if (j > 0) {
                                    entity.addConnection(entitiesArray[i][j - 1][k]);
                                    entitiesArray[i][j - 1][k].addConnection(entity);
                                }
                                if (k > 0) {
                                    entity.addConnection(entitiesArray[i][j][k - 1]);
                                    entitiesArray[i][j][k - 1].addConnection(entity);
                                }

                                entitiesList.add(entity);
                            }
                        );
                    }
                );
            }
        );


        return entitiesList;
    }
}
