package controller;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import model.Coordinates;
import model.GrainCell;
import model.Neighbour;

import java.awt.image.BufferedImage;
import java.util.*;

public class MonteCarlo extends Task {
    private int iterations;
    private GrainCell[][] microstructure;
    private String borderConditions, relation;
    private GraphicsContext gc;
    private double kt;
    private double relationRadius;

    private int width, height;
    private Random generator = new Random();

    public MonteCarlo(int iterations, GrainCell[][] microstructure, String borderConditions, GraphicsContext gc, double kt, String relation, int relationRadius) {
        this.iterations = iterations;
        this.microstructure = microstructure;
        this.borderConditions = borderConditions;
        this.gc = gc;
        this.kt = kt;
        this.relation = relation;
        this.relationRadius = relationRadius;
    }

    @Override
    protected Object call() {
        width = microstructure.length;
        height = microstructure[0].length;
        calculate();
        return null;
    }

    private void calculate() {
        for (int iter = 0; iter < iterations; iter++) {
            List listOfIndexes = initializeList();
            for (int i = 1; i < listOfIndexes.size(); ) {
                int nr = generator.nextInt(listOfIndexes.size() - 1);
                Coordinates index = (Coordinates) listOfIndexes.remove(nr);
                GrainCell grainCell = microstructure[(int) index.getX()][(int) index.getY()];
                Neighbour neighbours = new Neighbour((int) index.getX(), (int) index.getY(), borderConditions, width, height, microstructure);
                int J = 1;
                Map<Integer, Integer> numberOfNeighbours = countNumberOfNeighbours(neighbours);
                int sum = 0;

                if (numberOfNeighbours.size() > 0) {
                    for (Map.Entry<Integer, Integer> entry : numberOfNeighbours.entrySet()) {
                        if (entry.getKey() != grainCell.getColour())
                            sum += entry.getValue();
                    }
                    int energyBefore = J * sum;
                    int randomColourIndex = generator.nextInt(numberOfNeighbours.size());
                    int randomColour = 0;

                    //FIXME probably stupid solution
                    int counter = 0;
                    for (Map.Entry<Integer, Integer> entry : numberOfNeighbours.entrySet()) {
                        randomColour = entry.getKey();
                        if (counter <= randomColourIndex) break;
                    }
                    int newSum = 0;

                    if (numberOfNeighbours.size() > 0)
                        for (Map.Entry<Integer, Integer> entry : numberOfNeighbours.entrySet()) {
                            if (entry.getKey() != randomColour)
                                newSum += entry.getValue();
                        }
                    int energyAfter = J * newSum;
                    int energyDifference = energyAfter - energyBefore;
                    if (energyDifference <= 0) {
                        microstructure[(int) index.getX()][(int) index.getY()].setColour(randomColour);
                    } else { //TODO Is it ok?
                        double probability = Math.exp(-energyDifference / kt);
                        probability = 1.0 / probability;
                        double randomProbability = generator.nextDouble();
                        if (probability > randomProbability)
                            microstructure[(int) index.getX()][(int) index.getY()].setColour(randomColour);
                    }
                }
            }
            printStep(microstructure);
        }
    }

    private List initializeList() {
        List listOfIndexes = new LinkedList();
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                listOfIndexes.add(new Coordinates(column, row));
            }
        }
        return listOfIndexes;
    }

    private Map<Integer, Integer> countNumberOfNeighbours(Neighbour neighbours) {
        Map<Integer, Integer> numberOfNeighbours = new TreeMap<>();
        switch (relation) {
            case "von Neumann":
                numberOfNeighbours = neighbours.checkVonNeumann();
                break;
            case "Moore":
                numberOfNeighbours = neighbours.checkMoore();
                break;
            case "hex":
                numberOfNeighbours = neighbours.checkHex();
                break;
            case "pent":
                numberOfNeighbours = neighbours.checkPent();
                break;
            case "w/radius":
                numberOfNeighbours = neighbours.checkWithRadius(relationRadius, getPointSize());
                break;
        }
        return numberOfNeighbours;
    }

    private void printStep(GrainCell[][] step) {
        width = microstructure.length;
        height = microstructure[0].length;
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

    private double getPointSize() {
        double pointSizeWidth = gc.getCanvas().getWidth() / width;
        double pointSizeHeight = gc.getCanvas().getHeight() / height;
        return (pointSizeWidth < pointSizeHeight ? pointSizeWidth : pointSizeHeight);
    }
}
