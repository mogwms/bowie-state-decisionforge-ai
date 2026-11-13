package bowie.state.decisionforge.ai;

import java.util.List;

public class LaptopCatalog {
    private final List<Laptop> availableLaptops = List.of(
            new Laptop("CodeBook Pro 14", 1299.99, 16, 1.4, true),
            new Laptop("DevLite Air 13", 999.00, 8, 1.1, true),
            new Laptop("WorkStation X15", 2199.50, 32, 2.2, false),
            new Laptop("CampusBook 14", 649.99, 8, 1.5, true),
            new Laptop("UltraDev14", 1499.00, 16, 1.2, false));

    public List<Laptop> getLaptops() {
        return availableLaptops;
    }
    LaptopCatalog() {
        
    }
}
