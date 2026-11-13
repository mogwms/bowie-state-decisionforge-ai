
package bowie.state.decisionforge.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Truth Table
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem; // STEP 7 CHANGE
import javafx.scene.control.TreeView; // STEP 7 CHANGE
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
    @FXML private Label ramInsertlbl;

    // The GridPane is ONLY for displaying ranked laptop results.
    @FXML private GridPane rankingGrid;

    // Filter checkbox.
    @FXML private CheckBox warrantyChk;

    // Ranking-priority checkboxes remain separate from rankingGrid.
    @FXML private CheckBox lowestPriceChk;
    @FXML private CheckBox highestRAMChk;
    @FXML private CheckBox lowestWeightChk;
    @FXML private CheckBox warrantyPriorityChk;

    @FXML private Label warrantyInsertlbl;
    @FXML private Label weightInsertlbl;
    @FXML private TextField MaxPriceField;
    @FXML private TextField MaxWeightField;
    @FXML private TextField MinRAMField;
    @FXML private Button startButton;
    @FXML private Label errorMessageLabel;

    
    // Truth Table Stuff
    @FXML
    private TableView<LaptopCell> truthTable;
    @FXML
    private TableColumn<LaptopCell, String> LaptopCol;
    @FXML
    private TableColumn<LaptopCell, String> BCol;
    @FXML
    private TableColumn<LaptopCell, String> MCol;
    @FXML
    private TableColumn<LaptopCell, String> LCol;
    @FXML
    private TableColumn<LaptopCell, String> WCol;
    @FXML
    private TableColumn<LaptopCell, String> OutputCol;

    // Existing FXML TreeViews. Keep the FXML spelling "expresstionTreeview".
    @FXML private TreeView<String> expresstionTreeview;
    @FXML private TreeView<String> adjacencyTreeview;

    @FXML
    public void initialize() {

        // Populate the existing laptop selector.
        // STEP 7 CHANGE: Handle null entries and missing laptop names.
        LaptopCom.getItems().clear();

        if (laptops != null) {
            for (int i = 0; i < laptops.size(); i++) {
                Laptop laptop = laptops.get(i);

                if (laptop != null) {
                    LaptopCom.getItems().add(
                            getLaptopDisplayName(laptop, i + 1)
                    );
                }
            }
        }

        // Default ranking preference.
        lowestPriceChk.setSelected(true);
        highestRAMChk.setSelected(false);
        lowestWeightChk.setSelected(false);
        warrantyPriorityChk.setSelected(false);

        // Configure the results-only GridPane.
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

        // Initialize the expression tree and the existing adjacency list.
        if (expresstionTreeview != null) {
            TreeItem<String> expressionRoot = new TreeItem<>(
                    "Expression Tree — press START to evaluate laptops"
            );
            expressionRoot.setExpanded(true);
            expresstionTreeview.setRoot(expressionRoot);
            expresstionTreeview.setShowRoot(true);
        }
        populateAdjacencyTreeView();

        // Truth Table
        LaptopCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        getLaptopDisplayName(cellData.getValue().getLaptop(), 0)));
        BCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(Boolean.toString(cellData.getValue().getpriceQualifies())));
        MCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(Boolean.toString(cellData.getValue().getRAMQualifies())));
        LCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(Boolean.toString(cellData.getValue().getweightQualifies())));
        WCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(Boolean.toString(cellData.getValue().getwarrantyQualifies())));
        OutputCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(Boolean.toString(cellData.getValue().getOutput())));

    }

    // STEP 7 CHANGE:
    // Provides a safe display name for laptops with missing names.
    private String getLaptopDisplayName(Laptop laptop, int index) {

        if (laptop == null) {
            return "Unavailable Laptop #" + index;
        }

        String name = laptop.name();

        if (name == null || name.trim().isEmpty()) {
            return "Unnamed Laptop #" + index;
        }

        return name.trim();
    }

    // STEP 7 CHANGE:
    // Builds the knowledge graph using the existing LaptopCatalog objects.
    // Each laptop is a vertex with four specification relationship edges.
    private void populateAdjacencyTreeView() {

        if (adjacencyTreeview == null) {
            return;
        }

        TreeItem<String> root = new TreeItem<>(
                "Laptop Knowledge Graph — Adjacency List"
        );

        root.setExpanded(true);

        int laptopCount = 0;

        // Handle a null or empty catalog safely.
        if (laptops != null) {

            for (int i = 0; i < laptops.size(); i++) {

                Laptop laptop = laptops.get(i);

                // Ignore null laptop entries.
                if (laptop == null) {
                    continue;
                }

                // Create the laptop vertex.
                TreeItem<String> laptopNode = new TreeItem<>(
                        getLaptopDisplayName(laptop, i + 1)
                );

                // Create the HAS_PRICE relationship.
                laptopNode.getChildren().add(
                        new TreeItem<>(
                                String.format(
                                        Locale.US,
                                        "HAS_PRICE → Price: $%,.2f",
                                        laptop.price()
                                )
                        )
                );

                // Create the HAS_RAM relationship.
                laptopNode.getChildren().add(
                        new TreeItem<>(
                                "HAS_RAM → RAM: " + laptop.RAM() + " GB"
                        )
                );

                // Create the HAS_WEIGHT relationship.
                laptopNode.getChildren().add(
                        new TreeItem<>(
                                String.format(
                                        Locale.US,
                                        "HAS_WEIGHT → Weight: %.2f kg",
                                        laptop.weight()
                                )
                        )
                );

                // Create the HAS_WARRANTY relationship.
                laptopNode.getChildren().add(
                        new TreeItem<>(
                                "HAS_WARRANTY → Warranty: "
                                        + (laptop.hasWarranty() ? "Yes" : "No")
                        )
                );

                // Attach this laptop vertex to the graph root.
                root.getChildren().add(laptopNode);

                laptopCount++;
            }
        }

        // STEP 7 CHANGE:
        // Display an informative node instead of an empty TreeView.
        if (laptopCount == 0) {
            root.getChildren().add(
                    new TreeItem<>("No laptops available.")
            );
        }

        // Display the completed adjacency list.
        adjacencyTreeview.setRoot(root);
        adjacencyTreeview.setShowRoot(true);
    }

    @FXML
    private void ChooseLaptopModel(ActionEvent event) {

        String selected = LaptopCom.getValue();

        if (selected == null || laptops == null) {
            return;
        }

        // STEP 7 CHANGE:
        // Use the same safe display names used by the selector and graph.
        for (int i = 0; i < laptops.size(); i++) {

            Laptop laptop = laptops.get(i);

            if (laptop == null) {
                continue;
            }

            if (getLaptopDisplayName(laptop, i + 1).equals(selected)) {

                selectedLaptop = laptop;

                priceInsertlbl.setText(
                        "$" + Double.toString(laptop.price())
                );

                ramInsertlbl.setText(
                        Integer.toString(laptop.RAM())
                );

                weightInsertlbl.setText(
                        Double.toString(laptop.weight())
                );

                warrantyInsertlbl.setText(
                        laptop.hasWarranty() ? "Yes" : "No"
                );

                break;
            }
        }
    }

    @FXML
    private void OnStartButtonPress() {

        errorMessageLabel.setVisible(false);
        errorMessageLabel.setText("");

        // Clear the previous run so invalid input cannot leave stale results visible.
        rankingGrid.getChildren().clear();
        if (truthTable != null) {
            truthTable.getItems().clear();
        }
        if (expresstionTreeview != null) {
            TreeItem<String> emptyRoot = new TreeItem<>(
                    "No current evaluation — press START"
            );
            emptyRoot.setExpanded(true);
            expresstionTreeview.setRoot(emptyRoot);
        }

        final Double maxPrice;
        final Integer minRAM;
        final Double maxWeight;

        // Validate the optional numeric filters.
        try {
            maxPrice = parseOptionalDecimal(
                    MaxPriceField, "Maximum price"
            );

            minRAM = parseOptionalInteger(
                    MinRAMField, "Minimum RAM"
            );

            maxWeight = parseOptionalDecimal(
                    MaxWeightField, "Maximum weight"
            );

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

        // FILTER + TRUTH TABLE:
        // STEP 7 CHANGE: Check for null entries before reading laptop properties.
        List<Laptop> eligible = new ArrayList<>();

        // LinkedHashMap preserves catalog order in the truth table.
        Map<Laptop, List<Boolean>> qualifiers = new LinkedHashMap<>();

        if (laptops != null) {
            for (Laptop laptop : laptops) {

                // STEP 7 CHANGE: Ignore null catalog entries safely.
                if (laptop == null) {
                    continue;
                }

                boolean priceQualifies =
                        maxPrice == null || laptop.price() <= maxPrice;
                boolean RAMQualifies =
                        minRAM == null || laptop.RAM() >= minRAM;
                boolean weightQualifies =
                        maxWeight == null || laptop.weight() <= maxWeight;
                boolean warrantyQualifies =
                        !warrantyChk.isSelected() || laptop.hasWarranty();

                boolean output = priceQualifies
                        && RAMQualifies
                        && weightQualifies
                        && warrantyQualifies;

                // Add each qualifying laptop exactly once.
                if (output) {
                    eligible.add(laptop);
                }

                // Keep the actual warranty value in the W truth-table column,
                // matching the existing LaptopCell data model.
                qualifiers.put(
                        laptop,
                        List.of(
                                priceQualifies,
                                RAMQualifies,
                                weightQualifies,
                                laptop.hasWarranty(),
                                output
                        )
                );
            }
        }

        // Build the Truth Table model once, then use those exact rows to build
        // the Expression Tree. This runs even when no laptop is eligible, so
        // FALSE rows remain visible and explain why each laptop was rejected.
        ObservableList<LaptopCell> truthRows = FXCollections.observableArrayList();
        for (Map.Entry<Laptop, List<Boolean>> entry : qualifiers.entrySet()) {
            truthRows.add(new LaptopCell(entry.getKey(), entry.getValue()));
        }
        if (truthTable != null) {
            truthTable.setItems(truthRows);
        }
        populateExpressionTree(truthRows, warrantyChk.isSelected());

        if (eligible.isEmpty()) {
            showError("No laptops match the selected filters. The Truth Table and Expression Tree show the evaluation.");
            return;
        }

        // RANK: Add ordinal positions for selected priorities.
        Map<Laptop, Integer> scores = new IdentityHashMap<>();
        for (Laptop laptop : eligible) {
            scores.put(laptop, 0);
        }

        if (lowestPriceChk.isSelected()) {

            addOrdinalRanks(
                    eligible,
                    Comparator.comparingDouble(Laptop::price),
                    scores
            );
        }

        if (highestRAMChk.isSelected()) {

            addOrdinalRanks(
                    eligible,
                    Comparator.comparingInt(Laptop::RAM).reversed(),
                    scores
            );
        }

        if (lowestWeightChk.isSelected()) {

            addOrdinalRanks(
                    eligible,
                    Comparator.comparingDouble(Laptop::weight),
                    scores
            );
        }

        if (warrantyPriorityChk.isSelected()) {

            addOrdinalRanks(
                    eligible,
                    Comparator.comparing(Laptop::hasWarranty).reversed(),
                    scores
            );
        }

        // Lowest total score wins.
        // STEP 7 CHANGE: Use safe laptop names when breaking ties.
        eligible.sort(
                Comparator
                        .comparingInt((Laptop laptop) -> scores.get(laptop))
                        .thenComparing(
                                laptop -> getLaptopDisplayName(laptop, 0),
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        // Display only the ranked products in rankingGrid.
        displayResults(eligible, scores);
    }

    /**
     * Builds one Boolean expression tree per row in the existing Truth Table.
     *
     * The Truth Table's W column stores the laptop's actual warranty value.
     * The warranty checkbox determines whether warranty is required. Therefore
     * the exact eligibility expression is:
     *
     * Output = B AND M AND L AND (NOT F OR W)
     *
     * where F means "the user enabled the warranty filter" and W means
     * "the laptop has a warranty". This preserves the current Truth Table
     * data model without incorrectly treating every no-warranty laptop as
     * ineligible when the warranty filter is turned off.
     */
    private void populateExpressionTree(
            ObservableList<LaptopCell> truthRows,
            boolean warrantyFilterEnabled) {

        if (expresstionTreeview == null) {
            return;
        }

        TreeItem<String> root = new TreeItem<>(
                "Eligibility: B AND M AND L AND (NOT F OR W)"
        );
        root.setExpanded(true);

        if (truthRows == null || truthRows.isEmpty()) {
            root.getChildren().add(new TreeItem<>("No laptop rows to evaluate."));
            expresstionTreeview.setRoot(root);
            expresstionTreeview.setShowRoot(true);
            return;
        }

        for (LaptopCell cell : truthRows) {
            if (cell == null || cell.getLaptop() == null) {
                continue;
            }

            boolean b = cell.getpriceQualifies();
            boolean m = cell.getRAMQualifies();
            boolean l = cell.getweightQualifies();
            boolean w = cell.getwarrantyQualifies(); // actual warranty value
            boolean notF = !warrantyFilterEnabled;
            boolean warrantyRule = notF || w;
            boolean output = cell.getOutput();

            TreeItem<String> laptopNode = new TreeItem<>(
                    getLaptopDisplayName(cell.getLaptop(), 0)
                            + " — Output: " + booleanText(output)
            );

            // (B AND M) AND (L AND (NOT F OR W))
            TreeItem<String> andRoot = new TreeItem<>(
                    "AND → " + booleanText(output)
            );
            TreeItem<String> priceRamAnd = new TreeItem<>(
                    "AND → " + booleanText(b && m)
            );
            priceRamAnd.getChildren().add(new TreeItem<>(
                    "B — Price qualifies: " + booleanText(b)
            ));
            priceRamAnd.getChildren().add(new TreeItem<>(
                    "M — RAM qualifies: " + booleanText(m)
            ));

            TreeItem<String> weightWarrantyAnd = new TreeItem<>(
                    "AND → " + booleanText(l && warrantyRule)
            );
            weightWarrantyAnd.getChildren().add(new TreeItem<>(
                    "L — Weight qualifies: " + booleanText(l)
            ));

            TreeItem<String> warrantyOr = new TreeItem<>(
                    "OR — Warranty rule: " + booleanText(warrantyRule)
            );
            warrantyOr.getChildren().add(new TreeItem<>(
                    "NOT F — Warranty filter disabled: " + booleanText(notF)
            ));
            warrantyOr.getChildren().add(new TreeItem<>(
                    "W — Laptop has warranty: " + booleanText(w)
            ));
            weightWarrantyAnd.getChildren().add(warrantyOr);

            andRoot.getChildren().add(priceRamAnd);
            andRoot.getChildren().add(weightWarrantyAnd);
            laptopNode.getChildren().add(andRoot);
            laptopNode.setExpanded(true);
            andRoot.setExpanded(true);
            priceRamAnd.setExpanded(true);
            weightWarrantyAnd.setExpanded(true);
            warrantyOr.setExpanded(true);

            root.getChildren().add(laptopNode);
        }

        if (root.getChildren().isEmpty()) {
            root.getChildren().add(new TreeItem<>("No valid laptop rows to display."));
        }

        expresstionTreeview.setRoot(root);
        expresstionTreeview.setShowRoot(true);
    }

    private String booleanText(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    /*
     * Rank products for one selected attribute, then add those ranks
     * to the running total.
     * Equal values share a competition rank (1, 1, 3).
     */
    private void addOrdinalRanks(
            List<Laptop> products,
            Comparator<Laptop> comparator,
            Map<Laptop, Integer> scores
    ) {

        List<Laptop> ordered = new ArrayList<>(products);

        // STEP 7 CHANGE: Safe name comparison for missing laptop names.
        ordered.sort(
                comparator.thenComparing(
                        laptop -> getLaptopDisplayName(laptop, 0),
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        int rank = 0;
        Laptop previous = null;

        for (int i = 0; i < ordered.size(); i++) {

            Laptop current = ordered.get(i);

            if (previous == null ||
                    comparator.compare(previous, current) != 0) {

                rank = i + 1;
            }

            scores.put(
                    current,
                    scores.get(current) + rank
            );

            previous = current;
        }
    }

    // Blank input means the corresponding filter is not applied.
    private Double parseOptionalDecimal(
            TextField field,
            String label
    ) {

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
                throw new IllegalArgumentException(
                        label + " cannot be negative."
                );
            }

            return number;

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "Enter a valid number for " + label + "."
            );
        }
    }

    // RAM must be entered as a whole number.
    private Integer parseOptionalInteger(
            TextField field,
            String label
    ) {

        String value = field.getText().trim();

        if (value.isEmpty()) {
            return null;
        }

        try {

            int number = Integer.parseInt(value);

            if (number < 0) {
                throw new IllegalArgumentException(
                        label + " cannot be negative."
                );
            }

            return number;

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    label + " must be a whole number (for example, 8 or 16)."
            );
        }
    }

    // Display the result headings and ranked laptop rows.
    private void displayResults(
            List<Laptop> products,
            Map<Laptop, Integer> scores) {

        String[] headers = {
                "Rank",
                "Laptop",
                "Price",
                "RAM",
                "Weight",
                "Warranty",
                "Score"
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

                    // STEP 7 CHANGE: Safe display for missing names.
                    getLaptopDisplayName(laptop, 0),

                    String.format(
                            Locale.US, "$%,.2f", laptop.price()
                    ),

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

        // Expand the existing results GridPane.
        rankingGrid.setPrefHeight(
                Math.max(171, 35 + (row * 32))
        );
    }

    private void showError(String message) {

        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }

    public ScenarioCController() {

        // Preserve the existing FXMLLoader configuration.
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ScenarioC.fxml")
        );

        loader.setRoot(this);
        loader.setController(this);

        try {

            loader.load();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to load ScenarioC.fxml",
                    e
            );
        }
    }
}
