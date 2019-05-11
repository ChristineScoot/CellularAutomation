package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class GrainGrowthController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private void handleCellularAutomation() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("../view/automationView.fxml"));
        anchorPane.getChildren().setAll(pane);
    }
}
