package fluid.physics;

import fluid.entity.FluidEntity;
import fluid.setup.Setup;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.stream.IntStream;

/**
 * Created by mjanes on 6/10/17.
 */
public class UniverseTest {

    @Test
    public void assertNeighborsApplied() {
        FluidEntity[][] entities = new FluidEntity[2][2];
        entities[0][0] = Mockito.mock(FluidEntity.class);
        entities[1][0] = Mockito.mock(FluidEntity.class);
        entities[0][1] = Mockito.mock(FluidEntity.class);
        entities[1][1] = Mockito.mock(FluidEntity.class);
        Universe universe = new Universe(entities);
        universe.applyNeighborInteractions();

        Mockito.verify(entities[0][0]).applyNeighborInteractions(entities[1][0]);
        Mockito.verify(entities[0][0]).applyNeighborInteractions(entities[0][1]);
        Mockito.verify(entities[0][0], Mockito.never()).applyNeighborInteractions(entities[1][1]);
        Mockito.verify(entities[0][0], Mockito.never()).applyNeighborInteractions(entities[0][0]);

        Mockito.verify(entities[0][1]).applyNeighborInteractions(entities[0][0]);
        Mockito.verify(entities[0][1]).applyNeighborInteractions(entities[1][1]);
        Mockito.verify(entities[0][1], Mockito.never()).applyNeighborInteractions(entities[1][0]);
        Mockito.verify(entities[0][1], Mockito.never()).applyNeighborInteractions(entities[0][1]);

        Mockito.verify(entities[1][0]).applyNeighborInteractions(entities[0][0]);
        Mockito.verify(entities[1][0]).applyNeighborInteractions(entities[1][1]);
        Mockito.verify(entities[1][0], Mockito.never()).applyNeighborInteractions(entities[1][0]);
        Mockito.verify(entities[1][0], Mockito.never()).applyNeighborInteractions(entities[0][1]);

        Mockito.verify(entities[1][1]).applyNeighborInteractions(entities[1][0]);
        Mockito.verify(entities[1][1]).applyNeighborInteractions(entities[0][1]);
        Mockito.verify(entities[1][1], Mockito.never()).applyNeighborInteractions(entities[0][0]);
        Mockito.verify(entities[1][1], Mockito.never()).applyNeighborInteractions(entities[1][1]);
    }

    @Test
    public void testHeatConstanceAndSymmetryOfConduction() {
        FluidEntity[][] entities = Setup.rectangle(3, 3);
        entities[1][1].setTemperature(entities[1][1].getTemperature() * 4);

        double totalUniverseHeat = 0;
        for (FluidEntity[] entityColumn : entities) {
            for (FluidEntity entity : entityColumn) {
                totalUniverseHeat += entity.getTemperature() * entity.getMass();
            }
        }

        Universe universe = new Universe(entities);
        universe.applyNeighborInteractions();
        IntStream.range(0, entities.length).forEach(x -> IntStream.range(0, entities[x].length).forEach(y -> entities[x][y].changeHeat()));

        double totalUniverseHeatAfter = 0;
        for (FluidEntity[] entityColumn : entities) {
            for (FluidEntity entity : entityColumn) {
                totalUniverseHeatAfter += entity.getTemperature() * entity.getMass();
            }
        }

        Assert.assertEquals(totalUniverseHeat, totalUniverseHeatAfter, FluidEntity.FUZZ);

        Assert.assertEquals(entities[0][1].getMass(), entities[1][0].getMass(), FluidEntity.FUZZ);
        Assert.assertEquals(entities[0][1].getTemperature(), entities[1][0].getTemperature(), FluidEntity.FUZZ);

        Assert.assertEquals(entities[1][2].getMass(), entities[1][0].getMass(), FluidEntity.FUZZ);
        Assert.assertEquals(entities[1][2].getTemperature(), entities[1][0].getTemperature(), FluidEntity.FUZZ);

        Assert.assertEquals(entities[1][2].getMass(), entities[2][1].getMass(), FluidEntity.FUZZ);
        Assert.assertEquals(entities[1][2].getTemperature(), entities[2][1].getTemperature(), FluidEntity.FUZZ);

        Assert.assertNotEquals(entities[0][1].getTemperature(), entities[0][0].getTemperature(), FluidEntity.FUZZ);
    }

    /**
     * TODO: Test every cell and every value
     */
    @Test
    public void verifyNoChangeUntilPendingProcessed() {
        FluidEntity[][] entities = Setup.rectangle(3, 3);
        entities[1][1].setTemperature(entities[1][1].getTemperature() * 4);
        entities[1][2].setMass(entities[1][2].getMass() * 4);
        entities[0][2].setMass(entities[1][2].getMass() * 5);
        entities[1][0].setMass(entities[1][2].getMass() * 12);

        double entity00mass = entities[0][0].getMass();
        double entity11Pressure = entities[1][1].getPressure();
        double entity22DeltaX = entities[2][2].getDeltaX();
        double entity20DeltaY = entities[2][0].getDeltaY();

        Universe universe = new Universe(entities);
        universe.applySoloEffects();
        universe.applyNeighborInteractions();

        Assert.assertEquals(entity00mass, entities[0][0].getMass(), FluidEntity.FUZZ);
        Assert.assertEquals(entity11Pressure, entities[1][1].getPressure(), FluidEntity.FUZZ);
        Assert.assertEquals(entity22DeltaX, entities[2][2].getDeltaY(), FluidEntity.FUZZ);
        Assert.assertEquals(entity20DeltaY, entities[2][0].getDeltaY(), FluidEntity.FUZZ);
    }
}