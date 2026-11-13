package bowie.state.decisionforge.ai;

public class DecisionTree {
    private Laptop laptop;
    private double maxPrice;
    private int minRAM;
    private double maxWeight;
    private boolean warrantyRequired;

    public DecisionTree(Laptop laptop, double maxPrice, int minRAM,
            double maxWeight, boolean warrantyRequired) {
        this.laptop = laptop;
        this.maxPrice = maxPrice;
        this.minRAM = minRAM;
        this.maxWeight = maxWeight;
        this.warrantyRequired = warrantyRequired;
    }

    public String getPath() {
        String path = "";

        if (laptop.price() <= maxPrice) {
            path += "Within budget? Yes\n";
        } else {
            return path + "Within budget? No - Rejected: over budget.";
        }

        if (laptop.RAM() >= minRAM) {
            path += "RAM meets minimum? Yes\n";
        } else {
            return path + "RAM meets minimum? No - Rejected: not enough RAM.";
        }

        if (laptop.weight() <= maxWeight) {
            path += "Weight acceptable? Yes\n";
        } else {
            return path + "Weight acceptable? No - Rejected: too heavy.";
        }

        if (warrantyRequired) {
            path += "Warranty required? Yes\n";
            if (laptop.hasWarranty()) {
                path += "Has warranty? Yes\n";
            } else {
                return path + "Has warranty? No - Rejected: no warranty.";
            }
        } else {
            path += "Warranty required? No\n";
        }

        return path + "Recommended";
    }
}
