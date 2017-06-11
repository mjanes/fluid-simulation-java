package fluid.entity;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mjanes on 6/10/17.
 */
public class FluidEntityTest {

    @Test
    public void testConductionNoTemperatureDifference() {
        FluidEntity a = new FluidEntity(0, 0, 0, FluidEntity.DEFAULT_MASS, FluidEntity.DEFAULT_TEMPERATURE);
        FluidEntity b = new FluidEntity(1, 0, 0, FluidEntity.DEFAULT_MASS, FluidEntity.DEFAULT_TEMPERATURE);

        a.applyConductionBetweenCells(b);

        Assert.assertEquals(FluidEntity.DEFAULT_TEMPERATURE, a.getTemperature(), FluidEntity.FUZZ);
        Assert.assertEquals(FluidEntity.DEFAULT_TEMPERATURE, b.getTemperature(), FluidEntity.FUZZ);
    }

    @Test
    public void testConductionTemperatureDifference() {
        double startingTempA = 10;
        double startingTempB = 20;
        FluidEntity a = new FluidEntity(0, 0, 0, FluidEntity.DEFAULT_MASS, startingTempA);
        FluidEntity b = new FluidEntity(1, 0, 0, FluidEntity.DEFAULT_MASS, startingTempB);

        double originalTotalEnergy = a.getMass() * a.getTemperature() + b.getMass() * b.getTemperature();

        a.applyConductionBetweenCells(b);
        a.changeHeat();
        b.changeHeat();

        Assert.assertNotEquals(startingTempA, a.getTemperature(), FluidEntity.FUZZ);
        Assert.assertNotEquals(startingTempB, b.getTemperature(), FluidEntity.FUZZ);

        double newTotalEnergy = a.getMass() * a.getTemperature() + b.getMass() * b.getTemperature();

        Assert.assertEquals(originalTotalEnergy, newTotalEnergy, FluidEntity.FUZZ);
    }
}