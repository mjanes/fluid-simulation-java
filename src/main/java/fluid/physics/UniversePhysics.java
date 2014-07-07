package fluid.physics;

import fluid.entity.FluidEntity;

/**
 * Created by mjanes on 6/10/2014.
 */
public class UniversePhysics {

    private static int step = 0;

    /**
     * Run round of physics
     *
     * @param entities Entities to run universe physics on.
     */
    public static synchronized void updateUniverseState(FluidEntity[][] entities) {
        if (entities == null) return;

        ExternalInput.applyInput(entities, step);
        FluidPhysics.incrementFluid(entities);

        step++;
    }

}
