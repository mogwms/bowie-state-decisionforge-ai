import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class ScenarioCController {

    @FXML
    private ComboBox<?> chooseLaptopcom;

    @FXML
    public void initialize() {
        chooseLaptopcom.getItems().addAll("CodeBook Pro 14", "DevLite Air 13", "WorkStation X15", "CampusBook 14", "UltraDev14");
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

}
