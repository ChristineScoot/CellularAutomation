package controller;

import javafx.concurrent.Task;
import javafx.scene.canvas.GraphicsContext;
import model.Coordinates;
import model.GrainCell;
import model.Neighbour;

import java.util.*;

public class MonteCarlo extends Task {
    private int iterations;
    private GrainCell[][] microstructure;
    private String borderConditions, relation;
    private GraphicsContext gc;
    private double kt;
    private double relationRadius;
    private String colourIndicator;

    private int width, height;
    private Random generator = new Random();

    public MonteCarlo(int iterations, GrainCell[][] microstructure, String borderConditions, GraphicsContext gc, double kt, String relation, int relationRadius, String colourIndicator) {
        this.iterations = iterations;
        this.microstructure = microstructure;
        this.borderConditions = borderConditions;
        this.gc = gc;
        this.kt = kt;
        this.relation = relation;
        this.relationRadius = relationRadius;
        this.colourIndicator = colourIndicator;
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
                int nr = generator.nextInt(listOfIndexes.size());
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
                    grainCell.setEnergy(energyBefore);
                    int randomColourIndex = generator.nextInt(numberOfNeighbours.size());
                    int randomColour = 0;

                    int counter = 0;
                    for (Map.Entry<Integer, Integer> entry : numberOfNeighbours.entrySet()) {
                        randomColour = entry.getKey();
                        if (counter == randomColourIndex) break;
                        counter++;
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
                    } else {
                        double probability = Math.exp(-energyDifference / kt);
                        double randomProbability = generator.nextDouble();
                        if (probability > randomProbability)
                            microstructure[(int) index.getX()][(int) index.getY()].setColour(randomColour);
                    }
                }
            }
            new CanvasController().print(microstructure, gc, colourIndicator);
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
                numberOfNeighbours = neighbours.checkWithRadius(relationRadius, getPointSize());
                break;
        }
        return numberOfNeighbours;
    }

    private double getPointSize() {
        double pointSizeWidth = gc.getCanvas().getWidth() / width;
        double pointSizeHeight = gc.getCanvas().getHeight() / height;
        return (pointSizeWidth < pointSizeHeight ? pointSizeWidth : pointSizeHeight);
    }

    public String getColourIndicator() {
        return colourIndicator;
    }

    public void setColourIndicator(String colourIndicator) {
        this.colourIndicator = colourIndicator;
    }
}
