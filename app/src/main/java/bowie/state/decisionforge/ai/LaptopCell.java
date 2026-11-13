package bowie.state.decisionforge.ai;

import java.util.List;

public class LaptopCell {
    private Laptop laptop;
    private boolean priceQualifies;
    private boolean ramQualifies;
    private boolean weightQualifies;
    private boolean warrantyQualifies;
    private boolean output;

    public LaptopCell(Laptop laptop, List<Boolean> qualifiers) {
        this.laptop = laptop;
        priceQualifies = qualifiers.get(0);
        ramQualifies = qualifiers.get(1);
        weightQualifies = qualifiers.get(2);
        warrantyQualifies = qualifiers.get(3);
        output = qualifiers.get(4);
    }

    public Laptop getLaptop() {
        return laptop;
    }

    public boolean getPriceQualifies() {
        return priceQualifies;
    }

    public boolean getRamQualifies() {
        return ramQualifies;
    }

    public boolean getWeightQualifies() {
        return weightQualifies;
    }

    public boolean getWarrantyQualifies() {
        return warrantyQualifies;
    }

    public boolean getOutput() {
        return output;
    }
}
