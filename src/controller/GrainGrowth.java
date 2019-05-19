package controller;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import model.Coordinates;
import model.GrainCell;
import model.Neighbour;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;

public class GrainGrowth extends Task {
    private GraphicsContext gc;
    private GrainCell[][] previousGrainCells, currentGrainCells;
    private int width, height, relationRadius;
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
        initializeCentreOfGravity();
    }

    public void printStep(GrainCell[][] step) {
        double pointSize = getPointSize();
        BufferedImage bi = new BufferedImage((int) (width * pointSize), (int) (height * pointSize), BufferedImage.TYPE_INT_RGB);
        double canvasX;
        double canvasY = 0.0;
        for (int i = 0; i < height; i++) {
            canvasX = 0.0;
            for (int j = 0; j < width; j++) {
                if (step[i][j].isState())
                    for (int k = 0; k < pointSize; k++)
                        for (int l = 0; l < pointSize; l++) {
                            bi.setRGB((int) canvasX + k, (int) canvasY + l, step[i][j].getColour());
//                            bi.setRGB((int) step[i][j].getCoordinates().getX(), (int) step[i][j].getCoordinates().getY(), 16711680);
                        }
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
                case "w/radius":
                    grew = calculateWithRadius();
                    break;
            }
            updatePreviousGeneration();
            printStep(previousGrainCells);
            Thread.sleep(50);
        }
    }

    private boolean calculateVonNeumann() {
        boolean grew = false;
        for (int row = 0; row < height; row++)
            for (int column = 0; column < width; column++)
                if (!previousGrainCells[row][column].isState()) {
                    Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height, previousGrainCells);
                    Map<Integer, Integer> numberOfNeighbours = neighbour.checkVonNeumann();
                    if (numberOfNeighbours.size() > 0)
                        grew = setNewCell(row, column, numberOfNeighbours);
                }
        return grew;
    }

    private boolean calculateMoore() {
        boolean grew = false;
        for (int row = 0; row < height; row++)
            for (int column = 0; column < width; column++)
                if (!previousGrainCells[row][column].isState()) {
                    Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height, previousGrainCells);
                    Map<Integer, Integer> numberOfNeighbours = neighbour.checkMoore();
                    if (numberOfNeighbours.size() > 0)
                        grew = setNewCell(row, column, numberOfNeighbours);
                }
        return grew;
    }

    private boolean calculateHex() {
        boolean grew = false;
        for (int row = 0; row < height; row++)
            for (int column = 0; column < width; column++)
                if (!previousGrainCells[row][column].isState()) {
                    Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height, previousGrainCells);
                    Map<Integer, Integer> numberOfNeighbours = neighbour.checkHex();
                    if (numberOfNeighbours.size() > 0)
                        grew = setNewCell(row, column, numberOfNeighbours);
                }
        return grew;
    }

    private boolean calculatePent() {
        boolean grew = false;
        for (int row = 0; row < height; row++)
            for (int column = 0; column < width; column++)
                if (!previousGrainCells[row][column].isState()) {
                    Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height, previousGrainCells);
                    Map<Integer, Integer> numberOfNeighbours = neighbour.checkPent();
                    if (numberOfNeighbours.size() > 0)
                        grew = setNewCell(row, column, numberOfNeighbours);
                }
        return grew;
    }

    private boolean calculateWithRadius() {
        boolean grew = false;
        for (int row = 0; row < height; row++)
            for (int column = 0; column < width; column++)
                if (!previousGrainCells[row][column].isState()) {
                    Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height, previousGrainCells);
                    Map<Integer, Integer> numberOfNeighbours = neighbour.checkWithRadius(relationRadius, getPointSize());
                    if (numberOfNeighbours.size() > 0)
                        grew = setNewCell(row, column, numberOfNeighbours);
                }
        return grew;
    }

    private boolean setNewCell(int row, int column, Map<Integer, Integer> numberOfNeighbours) {
        currentGrainCells[row][column].setState(true);
        currentGrainCells[row][column].setColour(findMostCommonNeighbour(numberOfNeighbours));
        return true;
    }

    private void initializeCentreOfGravity() {
        for (int row = 0; row < height; row++)
            for (int column = 0; column < width; column++) {
                double xLeft = column * getPointSize();
                double xRight = ((column + 1) * getPointSize()) - 1;
                double yTop = row * getPointSize();
                double yBottom = ((row + 1) * getPointSize()) - 1;
                double x = xLeft + (xRight - xLeft) * generator.nextDouble();
                double y = yTop + (yBottom - yTop) * generator.nextDouble();
                currentGrainCells[row][column].setCoordinates(new Coordinates(x, y));
            }
    }

    private int findMostCommonNeighbour(Map<Integer, Integer> numberOfNeighbours) {
        Map.Entry<Integer, Integer> maxColour = null;
        for (Map.Entry<Integer, Integer> tmpColour : numberOfNeighbours.entrySet())
            if (maxColour == null || tmpColour.getValue().compareTo(maxColour.getValue()) > 0)
                maxColour = tmpColour;
        return maxColour.getKey();
    }

    @Override
    protected GrainCell[][] call() throws Exception {
        updatePreviousGeneration();
        printStep(previousGrainCells);
        calculate();
        return currentGrainCells;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public void setBorderCondition(String borderCondition) {
        this.borderCondition = borderCondition;
    }

    public void setRelationRadius(int relationRadius) {
        this.relationRadius = relationRadius;
    }

    public GrainCell[][] getCurrentGrainCells() {
        return currentGrainCells;
    }
}
