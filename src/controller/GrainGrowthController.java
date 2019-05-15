package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.GrainCell;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GrainGrowthController implements Initializable {
    @FXML
    private Canvas canvas;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField textFieldRandom;
    @FXML
    private TextField textFieldHomogeneousRow;
    @FXML
    private TextField textFieldHomogeneousColumn;
    @FXML
    private TextField textFieldRadius;
    @FXML
    private ChoiceBox<String> choiceBoxRelation;
    @FXML
    private ToggleGroup toggleGroupBorderConditions;

    GrainGrowth grainGrowth;
    GrainCell[][] initializeGrainCells;
    GraphicsContext gc;
    Random generator = new Random();
    private int netSize = 30;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeChoiceBoxRelation();
        toggleGroupBorderConditions.getToggles().get(0).setSelected(true);
        gc = canvas.getGraphicsContext2D();
        clear();
    }

    public void clear() {
        setUserDefinedListener();
        initializeGrainCells = new GrainCell[netSize][netSize];
        for (int i = 0; i < netSize; i++)
            for (int j = 0; j < netSize; j++)
                initializeGrainCells[i][j] = new GrainCell(false);
    }

    private void initializeChoiceBoxRelation() {
        ObservableList relations = FXCollections.observableArrayList("von Neumann", "Moore", "hex", "pent");
        choiceBoxRelation.getItems().setAll(relations);
        choiceBoxRelation.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleHomogeneousNucleation() throws CloneNotSupportedException {
        clear();
        int numberOfRows = Integer.parseInt(textFieldHomogeneousRow.getText());
        int numberOfColumns = Integer.parseInt(textFieldHomogeneousColumn.getText());
        if (numberOfRows > netSize || numberOfColumns > netSize) {
            showAlert();
            return;
        }
        int numberOfCells = numberOfColumns * numberOfRows;
        int rowPositionSize = netSize / numberOfRows;
        int columnPositionSize = netSize / numberOfColumns;

        int i = 0;
        for (int row = 0; row < numberOfRows; row++) {
            for (int column = 0; column < numberOfColumns; column++) {
                initializeGrainCells[rowPositionSize * row][columnPositionSize * column].setState(true);
                initializeGrainCells[rowPositionSize * row][columnPositionSize * column].setColour(((16776960 - 125) / numberOfCells) * (i + 1));
                i++;
            }
        }

        grainGrowth = new GrainGrowth(gc, initializeGrainCells, netSize, netSize);
    }

    @FXML
    private void handleRadiusNucleation() throws CloneNotSupportedException {
        clear();
        int radius = Integer.parseInt(textFieldRadius.getText());
        int numberOfCells = Integer.parseInt(textFieldRandom.getText());

        if (numberOfCells > netSize * netSize) {
            showAlert();
            return;
        }

        for (int i = 0; i < numberOfCells; i++) {
            int row, column;
            boolean isWithinRadius;
            int maxNumberOfIterations = 1000;
            do {
                row = generator.nextInt(netSize);
                column = generator.nextInt(netSize);
                isWithinRadius = false;
                for (int j = -radius; j <= radius; j++) {
                    for (int k = -radius; k <= radius; k++) {
                        int rowNeighbour = (row + j + netSize) % netSize;
                        int columnNeighbour = (column + k + netSize) % netSize;
                        if (initializeGrainCells[rowNeighbour][columnNeighbour].isState())
                            isWithinRadius = true;
                    }
                }
                maxNumberOfIterations--;
            }
            while (isWithinRadius && maxNumberOfIterations > 0);
            if (maxNumberOfIterations <= 0) {
                showAlert();
                break;
            }
            initializeGrainCells[row][column].setState(true);
            int colour = ((16776960 - 125) / numberOfCells) * (i + 1);
            initializeGrainCells[row][column].setColour(colour);
        }

        grainGrowth = new GrainGrowth(gc, initializeGrainCells, netSize, netSize);
    }

    @FXML
    private void handleRandomNucleation() throws CloneNotSupportedException {
        clear();
        int numberOfCells = Integer.parseInt(textFieldRandom.getText());

        if (numberOfCells > netSize * netSize) {
            showAlert();
            return;
        }

        for (int i = 0; i < numberOfCells; i++) {
            int row, column;
            do {
                row = generator.nextInt(netSize);
                column = generator.nextInt(netSize);
            }
            while (initializeGrainCells[row][column].isState());
            initializeGrainCells[row][column].setState(true);
            int colour = ((16776960 - 125) / numberOfCells) * (i + 1);
            initializeGrainCells[row][column].setColour(colour);
        }

        grainGrowth = new GrainGrowth(gc, initializeGrainCells, netSize, netSize);
    }

    @FXML
    private void handleCellularAutomation() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("../view/automationView.fxml"));
        anchorPane.getChildren().setAll(pane);
    }

    public void textFieldKeyAction(KeyEvent keyEvent) throws CloneNotSupportedException {
        if (keyEvent.getCode() == KeyCode.ENTER)
            handleRandomNucleation();
    }

    public void textFieldRadiusKeyAction(KeyEvent keyEvent) throws CloneNotSupportedException {
        if (keyEvent.getCode() == KeyCode.ENTER)
            handleRadiusNucleation();
    }

    public void textFieldHomogeneousKeyAction(KeyEvent keyEvent) throws CloneNotSupportedException {
        if (keyEvent.getCode() == KeyCode.ENTER)
            handleHomogeneousNucleation();
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setHeaderText("Too many cells");
        alert.showAndWait();
    }

    @FXML
    private void handleButtonClear() throws CloneNotSupportedException {
        clear();
        grainGrowth = new GrainGrowth(gc, initializeGrainCells, netSize, netSize);
    }

    private void setUserDefinedListener() {
        canvas.setOnMouseClicked(event -> {
            int x = (int) event.getX(), y = (int) event.getY();
            try {
                grainGrowth.setCell(x, y);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleButtonStart() throws CloneNotSupportedException {
        RadioButton borderConditions=(RadioButton) toggleGroupBorderConditions.getSelectedToggle();
        grainGrowth = new GrainGrowth(gc, initializeGrainCells, netSize, netSize);
        grainGrowth.setRelation(choiceBoxRelation.getValue());
        grainGrowth.setBorderCondition(borderConditions.getText());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(grainGrowth);
    }

}
