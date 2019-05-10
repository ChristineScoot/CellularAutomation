package sample;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;

import java.awt.image.BufferedImage;

public class CellularAutomation2D extends Task {
    private GraphicsContext gc;
    private int height, width;
    private boolean[][] previousState, currentState;

    public CellularAutomation2D(GraphicsContext gc, int height, int width, boolean[][] currentState) {
        this.gc = gc;
        this.height = height;
        this.width = width;
        this.currentState = currentState;
        printStep(this.currentState);
        previousState = new boolean[height][width];
        updatePreviousGeneration();
    }

    private void printStep(boolean[][] step) {
        double pointSize = getPointSize();
        BufferedImage bi = new BufferedImage((int) (width * pointSize), (int) (height * pointSize), BufferedImage.TYPE_INT_RGB);
        double canvasX;
        double canvasY = 0.0;
        for (int i = 0; i < height; i++) {
            canvasX = 0.0;
            for (int j = 0; j < width; j++) {
                if (step[i][j])
                    for (int k = 0; k < pointSize; k++)
                        for (int l = 0; l < pointSize; l++)
                            bi.setRGB((int) canvasX + k, (int) canvasY + l, 0);
                else
                    for (int k = 0; k < pointSize; k++)
                        for (int l = 0; l < pointSize; l++)
                            bi.setRGB((int) canvasX + k, (int) canvasY + l, 16777200); //16777215
                canvasX += pointSize;
            }
            canvasY += pointSize;
        }
        gc.drawImage(SwingFXUtils.toFXImage(bi, null), 0, 0);
    }

    private void updatePreviousGeneration() {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                previousState[i][j] = currentState[i][j];
    }

    public void calculate() throws InterruptedException {
        while (true) {
            Thread.sleep(50);
            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++) {
                    int numberOfNeighbours = countNeighbours(i, j);
                    if (!previousState[i][j] && numberOfNeighbours == 3) {
                        currentState[i][j] = true;
                    } else if (previousState[i][j] && (numberOfNeighbours == 2 || numberOfNeighbours == 3)) {
                        currentState[i][j] = true;
                    } else if (previousState[i][j] && (numberOfNeighbours > 3 || numberOfNeighbours < 2)) {
                        currentState[i][j] = false;
                    } else {
                        currentState[i][j] = previousState[i][j];
                    }
                }
            updatePreviousGeneration();
            printStep(previousState);
        }
    }

    private int countNeighbours(int row, int column) {
        int numberOfNeighbours = 0;
        int left, right, top, bottom;
        left = (column - 1 + width) % width;
        right = (column + 1 + width) % width;
        top = (row - 1 + height) % height;
        bottom = (row + 1 + height) % height;
        if (previousState[top][left]) numberOfNeighbours++;
        if (previousState[top][column]) numberOfNeighbours++;
        if (previousState[top][right]) numberOfNeighbours++;

        if (previousState[row][left]) numberOfNeighbours++;
        if (previousState[row][right]) numberOfNeighbours++;

        if (previousState[bottom][left]) numberOfNeighbours++;
        if (previousState[bottom][column]) numberOfNeighbours++;
        if (previousState[bottom][right]) numberOfNeighbours++;
        return numberOfNeighbours;
    }

    public void setCell(int x, int y) {
        int row, column;
        column = (int) (x / getPointSize());
        row = (int) (y / getPointSize());
        if (row < height && column < width) {
            currentState[row][column] = !currentState[row][column];
            updatePreviousGeneration();
            printStep(previousState);
        }
    }

    private double getPointSize() {
        double pointSizeWidth = 600.0 / width;
        double pointSizeHeight = 400.0 / height;
        return (pointSizeWidth < pointSizeHeight ? pointSizeWidth : pointSizeHeight);
    }

    @Override
    protected Object call() throws Exception {
        previousState = new boolean[height][width];
        updatePreviousGeneration();
        printStep(previousState);
        calculate();
        return null;
    }
}
