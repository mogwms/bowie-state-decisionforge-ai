package bowie.state.decisionforge.ai;

import java.util.List;

public class LaptopCell {
    private Laptop laptop;

    private boolean priceQualifies;
    private boolean RAMQualifies;
    private boolean weightQualifies;
    private boolean warrantyQualifies;
    private boolean output;

    public LaptopCell(Laptop laptop, List<Boolean> qualifiers) {
        this.laptop = laptop;

        this.priceQualifies = qualifiers.get(0);
        this.RAMQualifies = qualifiers.get(1);
        this.weightQualifies = qualifiers.get(2);
        this.warrantyQualifies = qualifiers.get(3);
        this.output = qualifiers.get(4);
    }

    public Laptop getLaptop() {
        return laptop;
    }

    public boolean getpriceQualifies() {
        return priceQualifies;
    }

    public boolean getRAMQualifies() {
        return RAMQualifies;
    }

    public boolean getweightQualifies() {
        return weightQualifies;
    }

    public boolean getwarrantyQualifies() {
        return warrantyQualifies;
    }
    public boolean getOutput() {
        return output;
    }
}
