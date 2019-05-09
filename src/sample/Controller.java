package sample;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.util.Random;

public class Controller {
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

    private int heightInt;
    private int widthInt;
    private boolean[][] initializeState;

    private void initialize() {
        progressBar.progressProperty().unbind();
        heightInt = Integer.parseInt(this.height.getText());
        widthInt = Integer.parseInt(this.width.getText());
        initializeState = new boolean[heightInt][widthInt];
    }

    @FXML
    private void handleButtonOneD() {
        int rule = Integer.parseInt(textFieldRule.getText());
        int generations = Integer.parseInt(height.getText());
        int length = Integer.parseInt(width.getText());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        resetCanvas(gc);
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
        cellularAutomation2D = new CellularAutomation2D(canvas.getGraphicsContext2D(), heightInt, widthInt, initializeState);
        setUserDefinedListener();
        thread = new Thread(cellularAutomation2D);
    }

    @FXML
    private void handleButtonUserDefined() {
        initialize();
        for (boolean[] row : initializeState)
            for (boolean cell : row)
                cell = false;
        cellularAutomation2D = new CellularAutomation2D(canvas.getGraphicsContext2D(), heightInt, widthInt, initializeState);
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
        cellularAutomation2D = new CellularAutomation2D(canvas.getGraphicsContext2D(), heightInt, widthInt, initializeState);
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
        cellularAutomation2D = new CellularAutomation2D(canvas.getGraphicsContext2D(), heightInt, widthInt, initializeState);
        setUserDefinedListener();
        thread = new Thread(cellularAutomation2D);
    }

    private void setUserDefinedListener() {
        canvas.setOnMouseClicked(event -> {
            int x = (int) event.getX(), y = (int) event.getY();
            cellularAutomation2D.setCell(x, y);
        });
    }

    private void resetCanvas(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.clearRect(canvas.getLayoutX(),
                canvas.getLayoutY(),
                canvas.getWidth(),
                canvas.getHeight());
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
}
