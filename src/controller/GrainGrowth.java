package controller;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import model.GrainCell;
import model.Neighbour;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class GrainGrowth extends Task {
    private GraphicsContext gc;
    private GrainCell[][] previousGrainCells, currentGrainCells;
    private int width, height;
    private String relation, borderCondition;
    private Random generator = new Random();

    public GrainGrowth(GraphicsContext gc, GrainCell[][] initializeGrainCells, int width, int height) throws CloneNotSupportedException {
        this.gc = gc;
        this.currentGrainCells = initializeGrainCells;
        this.previousGrainCells = new GrainCell[height][width];
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
                int colour = generator.nextInt(16777215);
                currentGrainCells[row][column].setColour(colour);
            }
            updatePreviousGeneration();
            printStep(currentGrainCells);
        }
    }

    private void calculate() throws InterruptedException, CloneNotSupportedException {
        boolean grew = true;
        while (grew) {
            switch (relation) {
                case "von Neumann":
                    grew = calculateVonNeumann();
                    break;
                case "Moore":
                    grew = calculateMoore();
                    break;
                case "hex":
                    grew = calculateHex();
                    break;
                case "pent":
                    grew = calculatePent();
                    break;
            }
            updatePreviousGeneration();
            printStep(previousGrainCells);
            Thread.sleep(50);

        }
        Thread.sleep(50);
    }

    private boolean calculateVonNeumann() {
        boolean grew = false;
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (!previousGrainCells[row][column].isState()) {
                    Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height);
                    Map<Integer, Integer> numberOfNeighbours = new TreeMap<>();

                    checkNeighbour(row, neighbour.getLeft(), numberOfNeighbours);
                    checkNeighbour(row, neighbour.getRight(), numberOfNeighbours);
                    checkNeighbour(neighbour.getTop(), column, numberOfNeighbours);
                    checkNeighbour(neighbour.getBottom(), column, numberOfNeighbours);

                    if (numberOfNeighbours.size() > 0) {
                        grew = setNewCell(row, column, numberOfNeighbours);
                    }
                }
            }
        }
        return grew;
    }

    private boolean calculateMoore() {
        boolean grew = false;
        for (int row = 0; row < height; row++)
            for (int column = 0; column < width; column++)
                if (!previousGrainCells[row][column].isState()) {
                    Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height);
                    Map<Integer, Integer> numberOfNeighbours = new TreeMap<>();
                    checkNeighbour(neighbour.getTop(), neighbour.getLeft(), numberOfNeighbours);
                    checkNeighbour(neighbour.getTop(), column, numberOfNeighbours);
                    checkNeighbour(neighbour.getTop(), neighbour.getRight(), numberOfNeighbours);

                    checkNeighbour(row, neighbour.getLeft(), numberOfNeighbours);
                    checkNeighbour(row, neighbour.getRight(), numberOfNeighbours);

                    checkNeighbour(neighbour.getBottom(), neighbour.getLeft(), numberOfNeighbours);
                    checkNeighbour(neighbour.getBottom(), column, numberOfNeighbours);
                    checkNeighbour(neighbour.getBottom(), neighbour.getRight(), numberOfNeighbours);
                    if (numberOfNeighbours.size() > 0) {
                        grew = setNewCell(row, column, numberOfNeighbours);
                    }
                }
        return grew;
    }

    private boolean calculateHex() {
        boolean grew = false;
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (!previousGrainCells[row][column].isState()) {
                    int hexagonalSide = generator.nextInt(2);
                    Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height);
                    Map<Integer, Integer> numberOfNeighbours = new TreeMap<>();
                    switch (hexagonalSide) {
                        case 0: //left
                            checkNeighbour(neighbour.getTop(), column, numberOfNeighbours);
                            checkNeighbour(neighbour.getTop(), neighbour.getRight(), numberOfNeighbours);

                            checkNeighbour(row, neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(row, neighbour.getRight(), numberOfNeighbours);

                            checkNeighbour(neighbour.getBottom(), neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(neighbour.getBottom(), column, numberOfNeighbours);
                            if (numberOfNeighbours.size() > 0) {
                                grew = setNewCell(row, column, numberOfNeighbours);
                            }
                            break;
                        case 1: //right
                            checkNeighbour(neighbour.getTop(), neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(neighbour.getTop(), column, numberOfNeighbours);

                            checkNeighbour(row, neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(row, neighbour.getRight(), numberOfNeighbours);

                            checkNeighbour(neighbour.getBottom(), column, numberOfNeighbours);
                            checkNeighbour(neighbour.getBottom(), neighbour.getRight(), numberOfNeighbours);
                            if (numberOfNeighbours.size() > 0) {
                                grew = setNewCell(row, column, numberOfNeighbours);
                            }
                            break;
                    }
                }
            }}
            return grew;
    }

    private boolean calculatePent() {
        boolean grew = false;
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (!previousGrainCells[row][column].isState()) {
                    int pentagonalSide = generator.nextInt(4);
                    Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height);
                    Map<Integer, Integer> numberOfNeighbours = new TreeMap<>();
                    switch (pentagonalSide) {
                        case 0: //left
                            checkNeighbour(neighbour.getTop(), neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(neighbour.getTop(), column, numberOfNeighbours);
                            checkNeighbour(row, neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(neighbour.getBottom(), neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(neighbour.getBottom(), column, numberOfNeighbours);

                            if (numberOfNeighbours.size() > 0) {
                                grew = setNewCell(row, column, numberOfNeighbours);
                            }
                            break;
                        case 1: //right
                            checkNeighbour(neighbour.getTop(), column, numberOfNeighbours);
                            checkNeighbour(neighbour.getTop(), neighbour.getRight(), numberOfNeighbours);
                            checkNeighbour(row, neighbour.getRight(), numberOfNeighbours);
                            checkNeighbour(neighbour.getBottom(), column, numberOfNeighbours);
                            checkNeighbour(neighbour.getBottom(), neighbour.getRight(), numberOfNeighbours);

                            if (numberOfNeighbours.size() > 0) {
                                grew = setNewCell(row, column, numberOfNeighbours);
                            }
                            break;
                        case 2: //top
                            checkNeighbour(neighbour.getTop(), neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(neighbour.getTop(), column, numberOfNeighbours);
                            checkNeighbour(neighbour.getTop(), neighbour.getRight(), numberOfNeighbours);

                            checkNeighbour(row, neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(row, neighbour.getRight(), numberOfNeighbours);

                            if (numberOfNeighbours.size() > 0) {
                                grew = setNewCell(row, column, numberOfNeighbours);
                            }
                            break;
                        case 3: //bottom
                            checkNeighbour(row, neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(row, neighbour.getRight(), numberOfNeighbours);

                            checkNeighbour(neighbour.getBottom(), neighbour.getLeft(), numberOfNeighbours);
                            checkNeighbour(neighbour.getBottom(), column, numberOfNeighbours);
                            checkNeighbour(neighbour.getBottom(), neighbour.getRight(), numberOfNeighbours);

                            if (numberOfNeighbours.size() > 0) {
                                grew = setNewCell(row, column, numberOfNeighbours);
                            }
                            break;
                    }
                }
            }
        }
        return grew;
    }

    private void checkNeighbour(int row, int column, Map<Integer, Integer> numberOfNeighbours) {
        if (previousGrainCells[row][column].isState())
            getNeighboursValues(row, column, numberOfNeighbours);
    }

    private boolean setNewCell(int row, int column, Map<Integer, Integer> numberOfNeighbours) {
        currentGrainCells[row][column].setState(true);
        currentGrainCells[row][column].setColour(findMostCommonNeighbour(numberOfNeighbours));
        return true;
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

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public void setBorderCondition(String borderCondition) {
        this.borderCondition = borderCondition;
    }
}
