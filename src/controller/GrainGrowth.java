package controller;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import model.GrainCell;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class GrainGrowth extends Task {
    private GraphicsContext gc;
    private GrainCell[][] previousGrainCells, currentGrainCells;
    private int width, height;

    public GrainGrowth(GraphicsContext gc, GrainCell[][] initializeGrainCells, int width, int height) throws CloneNotSupportedException {
        this.gc = gc;
        this.currentGrainCells = initializeGrainCells;
        this.previousGrainCells = new GrainCell[width][height];
        this.width = width;
        this.height = height;
        printStep(this.currentGrainCells);
        updatePreviousGeneration();
    }

    private void printStep(GrainCell[][] step) {
        double pointSize = getPointSize();
        BufferedImage bi = new BufferedImage((int) (width * pointSize), (int) (height * pointSize), BufferedImage.TYPE_INT_RGB);
        double canvasX;
        double canvasY = 0.0;
        for (int i = 0; i < height; i++) {
            canvasX = 0.0;
            for (int j = 0; j < width; j++) {
                if (step[i][j].isState())
                    for (int k = 0; k < pointSize; k++)
                        for (int l = 0; l < pointSize; l++)
                            bi.setRGB((int) canvasX + k, (int) canvasY + l, step[i][j].getColour());
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

    private void updatePreviousGeneration() throws CloneNotSupportedException {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                previousGrainCells[i][j] = (GrainCell) currentGrainCells[i][j].clone();
    }

    private double getPointSize() {
        double pointSizeWidth = gc.getCanvas().getWidth() / width;
        double pointSizeHeight = gc.getCanvas().getHeight() / height;
        return (pointSizeWidth < pointSizeHeight ? pointSizeWidth : pointSizeHeight);
    }

    public void setCell(int x, int y) throws CloneNotSupportedException {
        int row, column;
        column = (int) (x / getPointSize());
        row = (int) (y / getPointSize());
        if (row < height && column < width) {
            currentGrainCells[row][column].setState(!currentGrainCells[row][column].isState());
            if (currentGrainCells[row][column].isState()) {
                Random generator = new Random();
                int colour = generator.nextInt(16777215);
                currentGrainCells[row][column].setColour(colour);
            }
            updatePreviousGeneration();
            printStep(currentGrainCells);
        }
    }

    public void calculate() throws InterruptedException, CloneNotSupportedException {
        boolean grew = true;
        while (grew) {
            grew = calculateSingleStep();
            updatePreviousGeneration();
            Thread.sleep(50);
            printStep(previousGrainCells);
        }
    }

    private boolean calculateSingleStep() {
        boolean grew = false;
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (!previousGrainCells[row][column].isState()) {
                    int left, right, top, bottom;
                    left = (column - 1 + width) % width;
                    right = (column + 1 + width) % width;
                    top = (row - 1 + height) % height;
                    bottom = (row + 1 + height) % height;

                    Map<Integer, Integer> numberOfNeighbours = new TreeMap<>();
                    if (previousGrainCells[row][left].isState())
                        getNeighboursValues(row, left, numberOfNeighbours);
                    if (previousGrainCells[row][right].isState())
                        getNeighboursValues(row, right, numberOfNeighbours);
                    if (previousGrainCells[top][column].isState())
                        getNeighboursValues(top, column, numberOfNeighbours);
                    if (previousGrainCells[bottom][column].isState())
                        getNeighboursValues(bottom, column, numberOfNeighbours);

                    if (numberOfNeighbours.size() > 0) {
                        currentGrainCells[row][column].setState(true);
                        currentGrainCells[row][column].setColour(findMostCommonNeighbour(numberOfNeighbours));
                        grew = true;
                    }
                }
            }
        }
        return grew;
    }

    private void getNeighboursValues(int row, int column, Map<Integer, Integer> numberOfNeighbours) {
        int numberTmp = 1;
        int colour = previousGrainCells[row][column].getColour();
        if (numberOfNeighbours.containsKey(colour)) numberTmp = numberOfNeighbours.get(colour) + 1;
        numberOfNeighbours.put(colour, numberTmp);
    }

    private int findMostCommonNeighbour(Map<Integer, Integer> numberOfNeighbours) {
        Map.Entry<Integer, Integer> maxColour = null;
        for (Map.Entry<Integer, Integer> tmpColour : numberOfNeighbours.entrySet())
            if (maxColour == null || tmpColour.getValue().compareTo(maxColour.getValue()) > 0)
                maxColour = tmpColour;
        return maxColour.getKey();
    }

    @Override
    protected Object call() throws Exception {
        updatePreviousGeneration();
        printStep(previousGrainCells);
        calculate();
        return null;
    }
}
