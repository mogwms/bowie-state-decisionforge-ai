package bowie.state.decisionforge.ai;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class ScenarioCController extends AnchorPane {
    LaptopCatalog catalog = new LaptopCatalog();
    List<Laptop> laptops = catalog.getLaptops();
    private Laptop selectedLaptop;

    @FXML
    private ComboBox<String> LaptopCom;

    @FXML
    public void initialize() {
        laptops.forEach(laptop -> LaptopCom.getItems().add(laptop.name()));
    }

    @FXML
    private void ChooseLaptopModel(ActionEvent event) {
        String selected = LaptopCom.getValue();
        for (Laptop laptop : laptops) {
            if (laptop.name() == selected) {
                selectedLaptop = laptop;
                priceInsertlbl.setText("$" + Double.toString(laptop.price()));
                ramInsertlbl.setText(Integer.toString(laptop.RAM()));
                weightInsertlbl.setText(Double.toString(laptop.weight()));
                
                if (laptop.hasWarranty()) {
                    warrantyInsertlbl.setText("Yes");
                } else {
                     warrantyInsertlbl.setText("No");
                }
            }
        }
    }

    @FXML
    private void OnStartButtonPress() {

    }

    @FXML
    private Label priceInsertlbl;

    @FXML
    private Label pricelbl;

    @FXML
    private Label ramInsertlbl;

    @FXML
    private GridPane rankingGrd;

    @FXML
    private CheckBox warrantyChk;

    @FXML
    private Label warrantyInsertlbl;

    @FXML
    private Label warrantylbl;

    @FXML
    private Label weightInsertlbl;

    @FXML
    private Label weightlbl;

    @FXML 
    private TextField MaxPriceField;

    @FXML 
    private TextField MaxWeightField;

    @FXML 
    private TextField MinRAMField;

    @FXML 
    private Button startButton;

    @FXML
    private Label errorMessageLabel;

    public ScenarioCController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ScenarioC.fxml"));
        loader.setRoot(this);
        loader.setController(this); // Acts as its own controller

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ScenarioC.fxml", e);
        }
    }
}
