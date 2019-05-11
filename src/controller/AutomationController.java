package controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class AutomationController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField textFieldRule;
    @FXML
    private Canvas canvas;
    @FXML
    private TextField width;
    @FXML
    private TextField height;
    @FXML
    private ProgressBar progressBar;

    private CellularAutomation2D cellularAutomation2D;
    private Thread thread = new Thread();
    private GraphicsContext gc;


    private int heightInt;
    private int widthInt;
    private boolean[][] initializeState;

    private void initialize() {
        progressBar.progressProperty().unbind();
        heightInt = Integer.parseInt(this.height.getText());
        widthInt = Integer.parseInt(this.width.getText());
        initializeState = new boolean[heightInt][widthInt];
        gc = canvas.getGraphicsContext2D();
        resetCanvas();
    }

    @FXML
    private void handleButtonOneD() {
        initialize();
        int rule = Integer.parseInt(textFieldRule.getText());
        int generations = Integer.parseInt(height.getText());
        int length = Integer.parseInt(width.getText());
        new CellularAutomation(rule, gc, generations, length);
    }

    @FXML
    private void handleButtonFixed() {
        initialize();
        for (boolean[] row : initializeState)
            for (boolean cell : row)
                cell = false;
        initializeState[2][3] = true;
        initializeState[2][4] = true;
        initializeState[3][2] = true;
        initializeState[3][5] = true;
        initializeState[4][3] = true;
        initializeState[4][4] = true;
        cellularAutomation2D = new CellularAutomation2D(canvas.getGraphicsContext2D(), heightInt, widthInt, initializeState);
        setUserDefinedListener();
        thread = new Thread(cellularAutomation2D);
    }

    @FXML
    private void handleButtonGlider() {
        initialize();
        for (boolean[] row : initializeState)
            for (boolean cell : row)
                cell = false;
        initializeState[3][2] = true;
        initializeState[3][3] = true;
        initializeState[4][1] = true;
        initializeState[4][2] = true;
        initializeState[5][3] = true;
        cellularAutomation2D = new CellularAutomation2D(gc, heightInt, widthInt, initializeState);
        setUserDefinedListener();
        thread = new Thread(cellularAutomation2D);
    }

    @FXML
    private void handleButtonUserDefined() {
        initialize();
        for (boolean[] row : initializeState)
            for (boolean cell : row)
                cell = false;
        cellularAutomation2D = new CellularAutomation2D(gc, heightInt, widthInt, initializeState);
        setUserDefinedListener();
        thread = new Thread(cellularAutomation2D);
    }

    @FXML
    private void handleButtonOscillator() {
        initialize();
        for (boolean[] row : initializeState)
            for (boolean cell : row)
                cell = false;
        initializeState[3][3] = true;
        initializeState[4][3] = true;
        initializeState[5][3] = true;
        cellularAutomation2D = new CellularAutomation2D(gc, heightInt, widthInt, initializeState);
        setUserDefinedListener();
        thread = new Thread(cellularAutomation2D);
    }

    @FXML
    private void handleButtonRandom() {
        initialize();
        for (boolean[] row : initializeState)
            for (boolean cell : row)
                cell = false;
        Random generator = new Random();
        int numberOfRandomCells = generator.nextInt((heightInt * widthInt) / 2);
        for (int i = 0; i < numberOfRandomCells; i++) {
            int row = generator.nextInt(heightInt);
            int column = generator.nextInt(widthInt);
            initializeState[row][column] = true;
        }
        cellularAutomation2D = new CellularAutomation2D(gc, heightInt, widthInt, initializeState);
        setUserDefinedListener();
        thread = new Thread(cellularAutomation2D);
    }

    private void setUserDefinedListener() {
        canvas.setOnMouseClicked(event -> {
            int x = (int) event.getX(), y = (int) event.getY();
            cellularAutomation2D.setCell(x, y);
        });
    }

    private void resetCanvas() {
        gc.setFill(Color.WHITE);
        gc.clearRect(canvas.getLayoutX(),
                canvas.getLayoutY(),
                canvas.getWidth(),
                canvas.getHeight());

        BufferedImage bi = new BufferedImage((int) canvas.getWidth(), (int) canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < (int) canvas.getWidth(); i++) {
            for (int j = 0; j < (int) canvas.getHeight(); j++) {
                bi.setRGB(i, j, 16777215);
            }
        }

        gc.drawImage(SwingFXUtils.toFXImage(bi, null), 0, 0);
    }

    public void textFieldKeyAction(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            handleButtonOneD();
        }
    }

    @FXML
    private void handleStartButtonAction() {
        progressBar.setProgress(-1.0f);
        //TODO change to newer, not deprecated method
        thread.resume();
        if (!thread.isAlive()) {
            thread.start();
        }
    }

    @FXML
    private void handleStopButtonAction() {
        if (thread.isAlive()) {
            thread.suspend();
            progressBar.setProgress(0f);
        }
        setUserDefinedListener();
    }

    @FXML
    public void handleLoadGrainGrowth() throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("../view/grainGrowthView.fxml"));
        anchorPane.getChildren().setAll(pane);
    }
}
