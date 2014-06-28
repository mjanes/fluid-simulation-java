package fluid.physics;

import fluid.entity.FluidEntity;

/**
 * Created by mjanes on 6/10/2014.
 */
public class UniversePhysics {

//    private static int step = 0;

    /**
     * Run round of physics
     *
     * @param entities Entities to run universe physics on.
     */
    public static synchronized FluidEntity[][] updateUniverseState(FluidEntity[][] entities) {
        if (entities == null) return null;

//        double totalMass = 0;
//        double totalDeltaX = 0;
//        double totalDeltaY = 0;
//        double totalAbsoluteDeltaX = 0;
//        double totalAbsoluteDeltaY = 0;

        double maxMass = 0;
        for (FluidEntity[] row : entities) {
            for (FluidEntity entity : row) {
                if (entity.getMass() > maxMass) {
                    maxMass = entity.getMass();
                }
//                totalMass += entity.getMass();
//                totalDeltaX += entity.getDeltaX();
//                totalDeltaY += entity.getDeltaY();
//                totalAbsoluteDeltaX += Math.abs(entity.getDeltaX());
//                totalAbsoluteDeltaY += Math.abs(entity.getDeltaY());
            }
        }

//        System.out.println("Step " + step);
//        System.out.println("Total mass: " + totalMass);
//        System.out.println("Max entity mass: " + maxMass);
//        System.out.println("Total delta x: " + totalDeltaX);
//        System.out.println("Total delta y: " + totalDeltaY);
//        System.out.println("Total absolute delta x: " + totalAbsoluteDeltaX);
//        System.out.println("Total absolute delta y: " + totalAbsoluteDeltaY);

        FluidPhysics.incrementFluid(entities);

//        step++;

        return entities;
    }

}
