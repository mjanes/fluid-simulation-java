package fluid.physics;

import fluid.entity.FluidEntity;

import java.util.List;

/**
 * Created by mjanes on 6/10/2014.
 */
public class UniversePhysics {

    /**
     * Run round of physics
     *
     * @param entities Entities to run universe physics on.
     */
    public static synchronized List<FluidEntity> updateUniverseState(List<FluidEntity> entities) {
        if (entities == null || entities.size() == 0) return entities;

        // TODO: Fluid physics

        return entities;
    }

}
