package fluid.entity;

import javafx.scene.paint.Color;

public class OpenMockFluidEntity extends MockFluidEntity {

    public OpenMockFluidEntity(double x, double y, double z) {
        super(x, y, z);
    }

    @Override
    public synchronized Color getColor() {
        return Color.BLUE;
    }
}
