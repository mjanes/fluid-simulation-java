package fluid.physics;

import fluid.entity.FluidEntity;

/**
 * Created by mjanes on 6/10/2014.
 */
public class UniversePhysics {

    private static int sStep = 0;

    /**
     * Run round of physics
     *
     * @param entities Entities to run universe physics on.
     */
    public static synchronized void updateUniverseState(FluidEntity[][] entities) {
        if (entities == null) return;

        getStats(entities);

        ExternalInput.applyInput(entities, sStep);
        FluidPhysics.incrementFluid(entities);

        sStep++;
    }

    private static void getStats(FluidEntity[][] entities) {
        double totalMass = 0;
        double totalHeat = 0;
        double totalDeltaX = 0;
        double totalDeltaY = 0;
        double totalAbsoluteDeltaX = 0;
        double totalAbsoluteDeltaY = 0;

        double maxMass = 0;
        double minMass = entities[0][0].getMass();

        for (FluidEntity[] row : entities) {
            for (FluidEntity entity : row) {
                if (entity.getMass() > maxMass) {
                    maxMass = entity.getMass();
                }
                if (entity.getMass() < minMass) {
                    minMass = entity.getMass();
                }

                totalMass += entity.getMass();
                totalHeat += entity.getHeat();
                totalDeltaX += entity.getDeltaX();
                totalDeltaY += entity.getDeltaY();
                totalAbsoluteDeltaX += Math.abs(entity.getDeltaX());
                totalAbsoluteDeltaY += Math.abs(entity.getDeltaY());
            }
        }

        System.out.println("Step " + sStep);
        System.out.println("Total mass: " + totalMass);
        System.out.println("Max entity mass: " + maxMass);
        System.out.println("Min entity mass: " + minMass);
        System.out.println("Average mass: " + totalMass / (entities.length * entities[0].length));
        System.out.println("Total heat: " + totalHeat);
        System.out.println("Average heat: " + totalHeat / (entities.length * entities[0].length));
        System.out.println("Total delta x: " + totalDeltaX);
        System.out.println("Total delta y: " + totalDeltaY);
        System.out.println("Total absolute delta x: " + totalAbsoluteDeltaX);
        System.out.println("Total absolute delta y: " + totalAbsoluteDeltaY);
    }

}
