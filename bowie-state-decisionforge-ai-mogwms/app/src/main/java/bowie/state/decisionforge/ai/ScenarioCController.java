package bowie.state.decisionforge.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ScenarioCController extends AnchorPane {

    private final LaptopCatalog catalog = new LaptopCatalog();
    private final List<Laptop> laptops = catalog.getLaptops();
    private Laptop selectedLaptop;

    @FXML private ComboBox<String> LaptopCom;
    @FXML private Label priceInsertlbl;
    @FXML private Label pricelbl;
    @FXML private Label ramInsertlbl;

    // The GridPane is ONLY for displaying the ranked laptop results.
    @FXML private GridPane rankingGrid;

    // Filter checkbox: when checked, only laptops with a warranty qualify.
    @FXML private CheckBox warrantyChk;

    // Ranking-priority checkboxes are separate FXML controls, not GridPane children.
    @FXML private CheckBox lowestPriceChk;
    @FXML private CheckBox highestRAMChk;
    @FXML private CheckBox lowestWeightChk;
    @FXML private CheckBox warrantyPriorityChk;

    @FXML private Label warrantyInsertlbl;
    @FXML private Label warrantylbl;
    @FXML private Label weightInsertlbl;
    @FXML private Label weightlbl;
    @FXML private TextField MaxPriceField;
    @FXML private TextField MaxWeightField;
    @FXML private TextField MinRAMField;
    @FXML private Button startButton;
    @FXML private Label errorMessageLabel;

    @FXML
    public void initialize() {
        // Populate the existing laptop selector.
        LaptopCom.getItems().clear();
        for (Laptop laptop : laptops) {
            LaptopCom.getItems().add(laptop.name());
        }

        // Default ranking preference; users may select any combination.
        lowestPriceChk.setSelected(true);
        highestRAMChk.setSelected(false);
        lowestWeightChk.setSelected(false);
        warrantyPriorityChk.setSelected(false);

        // IMPORTANT: Clear and configure the results GridPane only.
        // No preference CheckBoxes are added to rankingGrid.
        rankingGrid.getChildren().clear();
        rankingGrid.getColumnConstraints().clear();

        double[] widths = {8, 27, 14, 10, 14, 14, 13};
        for (double width : widths) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(width);
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);
            rankingGrid.getColumnConstraints().add(column);
        }

        errorMessageLabel.setText("");
        errorMessageLabel.setVisible(false);
    }

    @FXML
    private void ChooseLaptopModel(ActionEvent event) {
        String selected = LaptopCom.getValue();
        if (selected == null) {
            return;
        }

        for (Laptop laptop : laptops) {
            if (laptop.name().equals(selected)) {
                selectedLaptop = laptop;
                priceInsertlbl.setText("$" + Double.toString(laptop.price()));
                ramInsertlbl.setText(Integer.toString(laptop.RAM()));
                weightInsertlbl.setText(Double.toString(laptop.weight()));
                warrantyInsertlbl.setText(laptop.hasWarranty() ? "Yes" : "No");
                break;
            }
        }
    }

    @FXML
    private void OnStartButtonPress() {
        errorMessageLabel.setVisible(false);
        errorMessageLabel.setText("");

        // Remove only previous result rows. rankingGrid contains no preference controls.
        rankingGrid.getChildren().clear();

        final Double maxPrice;
        final Integer minRAM;
        final Double maxWeight;

        // Validate the three optional numeric filter fields.
        try {
            maxPrice = parseOptionalDecimal(MaxPriceField, "Maximum price");
            minRAM = parseOptionalInteger(MinRAMField, "Minimum RAM");
            maxWeight = parseOptionalDecimal(MaxWeightField, "Maximum weight");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        // At least one ranking priority must be selected.
        List<CheckBox> priorities = new ArrayList<>();
        priorities.add(lowestPriceChk);
        priorities.add(highestRAMChk);
        priorities.add(lowestWeightChk);
        priorities.add(warrantyPriorityChk);

        boolean hasPriority = false;
        for (CheckBox priority : priorities) {
            if (priority.isSelected()) {
                hasPriority = true;
                break;
            }
        }

        if (!hasPriority) {
            showError("Select at least one ranking priority.");
            return;
        }

        // FILTER: all active conditions must be satisfied.
        List<Laptop> eligible = new ArrayList<>();
        for (Laptop laptop : laptops) {
            boolean qualifies =
                (maxPrice == null || laptop.price() <= maxPrice) &&
                (minRAM == null || laptop.RAM() >= minRAM) &&
                (maxWeight == null || laptop.weight() <= maxWeight) &&
                (!warrantyChk.isSelected() || laptop.hasWarranty());

            if (qualifies) {
                eligible.add(laptop);
            }
        }

        if (eligible.isEmpty()) {
            showError("No laptops match the selected filters.");
            return;
        }

        // RANK: add the ordinal position for every selected priority.
        Map<Laptop, Integer> scores = new IdentityHashMap<>();
        for (Laptop laptop : eligible) {
            scores.put(laptop, 0);
        }

        if (lowestPriceChk.isSelected()) {
            addOrdinalRanks(eligible, Comparator.comparingDouble(Laptop::price), scores);
        }
        if (highestRAMChk.isSelected()) {
            addOrdinalRanks(eligible,
                Comparator.comparingInt(Laptop::RAM).reversed(), scores);
        }
        if (lowestWeightChk.isSelected()) {
            addOrdinalRanks(eligible, Comparator.comparingDouble(Laptop::weight), scores);
        }
        if (warrantyPriorityChk.isSelected()) {
            addOrdinalRanks(eligible,
                Comparator.comparing(Laptop::hasWarranty).reversed(), scores);
        }

        // Lowest total score wins. Alphabetical name order breaks score ties.
        eligible.sort(Comparator
            .comparingInt((Laptop laptop) -> scores.get(laptop))
            .thenComparing(Laptop::name, String.CASE_INSENSITIVE_ORDER));

        // Display only the ranked products in rankingGrid.
        displayResults(eligible, scores);
    }

    /*
     * Rank products for one selected attribute, then add those ranks to the
     * running total. Equal attribute values share a competition rank (1, 1, 3).
     */
    private void addOrdinalRanks(
        List<Laptop> products,
        Comparator<Laptop> comparator,
        Map<Laptop, Integer> scores
    ) {
        List<Laptop> ordered = new ArrayList<>(products);
        ordered.sort(comparator.thenComparing(
            Laptop::name, String.CASE_INSENSITIVE_ORDER));

        int rank = 0;
        Laptop previous = null;

        for (int i = 0; i < ordered.size(); i++) {
            Laptop current = ordered.get(i);

            if (previous == null || comparator.compare(previous, current) != 0) {
                rank = i + 1;
            }

            scores.put(current, scores.get(current) + rank);
            previous = current;
        }
    }

    // Blank input means that the corresponding filter is not applied.
    private Double parseOptionalDecimal(TextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            double number = Double.parseDouble(value);
            if (!Double.isFinite(number)) {
                throw new NumberFormatException();
            }
            if (number < 0) {
                throw new IllegalArgumentException(label + " cannot be negative.");
            }
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                "Enter a valid number for " + label + ".");
        }
    }

    // RAM must be entered as a whole number; blank input is optional.
    private Integer parseOptionalInteger(TextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            int number = Integer.parseInt(value);
            if (number < 0) {
                throw new IllegalArgumentException(label + " cannot be negative.");
            }
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                label + " must be a whole number (for example, 8 or 16).");
        }
    }

    // Add the result headings and ranked laptop rows to the results-only GridPane.
    private void displayResults(List<Laptop> products, Map<Laptop, Integer> scores) {
        String[] headers = {
            "Rank", "Laptop", "Price", "RAM", "Weight", "Warranty", "Score"
        };

        for (int column = 0; column < headers.length; column++) {
            Label header = new Label(headers[column]);
            header.setStyle("-fx-font-weight: bold;");
            header.setWrapText(true);
            header.setPadding(new Insets(5, 3, 5, 3));
            rankingGrid.add(header, column, 0);
        }

        int row = 1;
        int position = 1;

        for (Laptop laptop : products) {
            String[] values = {
                Integer.toString(position++),
                laptop.name(),
                String.format("$%.2f", laptop.price()),
                Integer.toString(laptop.RAM()),
                Double.toString(laptop.weight()),
                laptop.hasWarranty() ? "Yes" : "No",
                Integer.toString(scores.get(laptop))
            };

            for (int column = 0; column < values.length; column++) {
                Label cell = new Label(values[column]);
                cell.setWrapText(true);
                cell.setPadding(new Insets(5, 3, 5, 3));
                rankingGrid.add(cell, column, row);
            }
            row++;
        }

        // Expand the existing GridPane enough to show the generated result rows.
        rankingGrid.setPrefHeight(Math.max(171, 35 + (row * 32)));
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }

    public ScenarioCController() {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/ScenarioC.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ScenarioC.fxml", e);
        }
    }
}
