package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
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
import model.Neighbour;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.TreeMap;
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
    @FXML
    private TextField textFieldWidth;
    @FXML
    private TextField textFieldHeight;
    @FXML
    private TextField textFieldRadiusRelation;
    @FXML
    private TextField textFieldMCkt;
    @FXML
    private TextField textFieldMCIterations;
    @FXML
    private Button energyButton;
    @FXML
    private Button dislocationButton;
    @FXML
    private TextField textFieldA;
    @FXML
    private TextField textFieldB;

    private GrainGrowth grainGrowth;
    private MonteCarlo monteCarlo;
    private Recrystallisation recrystallisation;
    private GrainCell[][] initializeGrainCells, microstructure;
    private GraphicsContext gc;
    private Random generator = new Random();
    private int width, height;
    int clickCountEnergy = 0;
    int clickCountDislocation = 0;
    int commonColour = 65535;//16777215;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeChoiceBoxRelation();
        toggleGroupBorderConditions.getToggles().get(0).setSelected(true);
        gc = canvas.getGraphicsContext2D();
        clear();
        energyButton.setOnMousePressed(event -> clickCountEnergy++);
        dislocationButton.setOnMousePressed(event -> clickCountDislocation++);
    }

    private void clear() {
        setUserDefinedListener();
        resetCanvas();
        width = Integer.parseInt(textFieldWidth.getText());
        height = Integer.parseInt(textFieldHeight.getText());
        if (width <= 0 || height <= 0) {
            showNegativeError();
        }
        initializeGrainCells = new GrainCell[height][width];
        microstructure = new GrainCell[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                initializeGrainCells[i][j] = new GrainCell(false);
                microstructure[i][j] = new GrainCell(false);
            }
        countEnergy();
    }

    private void resetCanvas() {
        BufferedImage bi = new BufferedImage((int) canvas.getWidth(), (int) canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < (int) canvas.getWidth(); i++)
            for (int j = 0; j < (int) canvas.getHeight(); j++) {
                bi.setRGB(i, j, 16777215);
            }
        gc.drawImage(SwingFXUtils.toFXImage(bi, null), 0, 0);
    }

    private void initializeChoiceBoxRelation() {
        ObservableList relations = FXCollections.observableArrayList("von Neumann", "Moore", "pent", "hex left", "hex right", "hex random", "w/radius");
        choiceBoxRelation.getItems().setAll(relations);
        choiceBoxRelation.getSelectionModel().select(1);
    }

    private void countEnergy() {
        RadioButton borderConditions = (RadioButton) toggleGroupBorderConditions.getSelectedToggle();
        String borderCond = borderConditions.getText();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                GrainCell grainCell = microstructure[row][column];
                Neighbour neighbours = new Neighbour(row, column, borderCond, width, height, microstructure);
                int J = 1;
                Map<Integer, Integer> numberOfNeighbours = countNumberOfNeighbours(neighbours);
                int sum = 0;

                if (numberOfNeighbours.size() > 0) {
                    for (Map.Entry<Integer, Integer> entry : numberOfNeighbours.entrySet()) {
                        if (entry.getKey() != grainCell.getColour())
                            sum += entry.getValue();
                    }
                    int energyBefore = J * sum;
                    grainCell.setEnergy(energyBefore);
                }
            }
        }
    }

    private Map<Integer, Integer> countNumberOfNeighbours(Neighbour neighbours) {
        Map<Integer, Integer> numberOfNeighbours = new TreeMap<>();
        switch (choiceBoxRelation.getValue()) {
            case "von Neumann":
                numberOfNeighbours = neighbours.checkVonNeumann();
                break;
            case "Moore":
                numberOfNeighbours = neighbours.checkMoore();
                break;
            case "hex left":
                numberOfNeighbours = neighbours.checkHex(0);
                break;
            case "hex right":
                numberOfNeighbours = neighbours.checkHex(1);
                break;
            case "hex random":
                numberOfNeighbours = neighbours.checkHex(3);
                break;
            case "pent":
                numberOfNeighbours = neighbours.checkPent();
                break;
            case "w/radius":
                numberOfNeighbours = neighbours.checkWithRadius(Integer.parseInt(textFieldRadiusRelation.getText()), getPointSize());
                break;
        }
        return numberOfNeighbours;
    }

    private int getPointSize() {
        double pointSizeWidth = gc.getCanvas().getWidth() / width;
        double pointSizeHeight = gc.getCanvas().getHeight() / height;
        return (int) (pointSizeWidth < pointSizeHeight ? pointSizeWidth : pointSizeHeight);
    }

    @FXML
    private void handleHomogeneousNucleation() throws CloneNotSupportedException {
        clear();
        int numberOfRows = Integer.parseInt(textFieldHomogeneousRow.getText());
        int numberOfColumns = Integer.parseInt(textFieldHomogeneousColumn.getText());
        if (numberOfRows <= 0 || numberOfColumns <= 0) {
            showNegativeError();
        }
        if (numberOfRows > height || numberOfColumns > width) {
            showAlert();
            return;
        }
        int numberOfCells = numberOfColumns * numberOfRows;
        int rowPositionSize = height / numberOfRows;
        int columnPositionSize = width / numberOfColumns;

        int i = 0;
        for (int row = 0; row < numberOfRows; row++) {
            for (int column = 0; column < numberOfColumns; column++) {
                initializeGrainCells[rowPositionSize * row][columnPositionSize * column].setState(true);
                initializeGrainCells[rowPositionSize * row][columnPositionSize * column].setColour(((commonColour - 125) / numberOfCells) * (i + 1));
                i++;
            }
        }

        grainGrowth = new GrainGrowth(gc, initializeGrainCells, width, height);
    }

    @FXML
    private void handleRadiusNucleation() throws CloneNotSupportedException {
        clear();
        int radius = Integer.parseInt(textFieldRadius.getText());
        int numberOfCells = Integer.parseInt(textFieldRandom.getText());
        if (numberOfCells <= 0 || radius <= 0) {
            showNegativeError();
        }
        if (numberOfCells > width * height || radius > width || radius > height) {
            showAlert();
            return;
        }

        for (int i = 0; i < numberOfCells; i++) {
            int row, column;
            boolean isWithinRadius;
            int maxNumberOfIterations = 1000;
            do {
                row = generator.nextInt(height);
                column = generator.nextInt(width);
                isWithinRadius = false;
                for (int j = -radius; j <= radius; j++) {
                    int rowNeighbour = (row + j + height) % height;
                    for (int k = -radius; k <= radius; k++) {
                        int columnNeighbour = (column + k + width) % width;
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
            int colour = ((commonColour - 125) / numberOfCells) * (i + 1);
            initializeGrainCells[row][column].setColour(colour);
        }

        grainGrowth = new GrainGrowth(gc, initializeGrainCells, width, height);
    }

    @FXML
    private void handleRandomNucleation() throws CloneNotSupportedException {
        clear();
        int numberOfCells = Integer.parseInt(textFieldRandom.getText());
        if (numberOfCells <= 0) {
            showNegativeError();
        }
        if (numberOfCells > width * height) {
            showAlert();
            return;
        }

        for (int i = 0; i < numberOfCells; i++) {
            int row, column;
            do {
                row = generator.nextInt(height);
                column = generator.nextInt(width);
            }
            while (initializeGrainCells[row][column].isState());
            initializeGrainCells[row][column].setState(true);
            int colour = ((commonColour - 125) / numberOfCells) * (i + 1);
            initializeGrainCells[row][column].setColour(colour);
        }

        grainGrowth = new GrainGrowth(gc, initializeGrainCells, width, height);
    }

    @FXML
    private void handleCellularAutomation() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("../view/automationView.fxml"));
        anchorPane.getChildren().setAll(pane);
    }

    public void textFieldRandomKeyAction(KeyEvent keyEvent) throws CloneNotSupportedException {
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

    private void showNegativeError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setHeaderText("Number must be positive");
        alert.showAndWait();
    }

    @FXML
    private void handleButtonClear() throws CloneNotSupportedException {
        clear();
        grainGrowth = new GrainGrowth(gc, initializeGrainCells, width, height);
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
        RadioButton borderConditions = (RadioButton) toggleGroupBorderConditions.getSelectedToggle();
        grainGrowth = new GrainGrowth(gc, initializeGrainCells, width, height);
        grainGrowth.setRelation(choiceBoxRelation.getValue());
        grainGrowth.setRelationRadius(Integer.parseInt(textFieldRadiusRelation.getText()));
        grainGrowth.setBorderCondition(borderConditions.getText());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(grainGrowth);
    }

    @FXML
    private void handleButtonMonteCarlo() {
        int iterations = Integer.parseInt(textFieldMCIterations.getText());
        if (iterations <= 0) {
            showNegativeError();
        }
        microstructure = grainGrowth.getCurrentGrainCells();
        RadioButton borderConditions = (RadioButton) toggleGroupBorderConditions.getSelectedToggle();
        String borderCond = borderConditions.getText();
        double kt = Double.parseDouble(textFieldMCkt.getText());

        monteCarlo = new MonteCarlo(iterations, microstructure, borderCond, gc, kt, choiceBoxRelation.getValue(), Integer.parseInt(textFieldRadiusRelation.getText()), "");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(monteCarlo);
    }

    @FXML
    private void handleButtonMonteCarloEnergyColour() {
        if (monteCarlo.getColourIndicator().equals("energyColour"))
            monteCarlo.setColourIndicator("");
        else
            monteCarlo.setColourIndicator("energyColour");
        if (clickCountEnergy % 2 == 0)
            new CanvasController().print(microstructure, gc, "");
        else
            new CanvasController().print(microstructure, gc, "energyColour");
    }

    @FXML
    private void handleButtonDislocationGrowth() {
        RadioButton borderConditions = (RadioButton) toggleGroupBorderConditions.getSelectedToggle();
        String borderCond = borderConditions.getText();
        double A = Double.parseDouble(textFieldA.getText());
        double B = Double.parseDouble(textFieldB.getText());
        recrystallisation = new Recrystallisation(microstructure, gc, A, B, borderCond);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(recrystallisation);
    }

    @FXML
    private void handleButtonShowDislocationDensity() {
        recrystallisation.setColourIndicator(clickCountDislocation);
    }

    public void textFieldMCKeyAction(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER)
            handleButtonMonteCarlo();
    }
}
