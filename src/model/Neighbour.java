package model;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class Neighbour {
    private int row, column, width, height;
    private int left, right, top, bottom;
    private String boundaryCondition;
    private GrainCell[][] previousGrainCells;
    private Map<Integer, Integer> numberOfNeighbours = new TreeMap<>();
    private Map<Integer, Integer> numberOfRecrystallisedNeighbours = new TreeMap<>();
    private Random generator = new Random();


    public Neighbour(int row, int column, String boundaryCondition, int width, int height, GrainCell[][] previousGrainCells) {
        this.row = row;
        this.column = column;
        this.boundaryCondition = boundaryCondition;
        this.width = width;
        this.height = height;
        this.previousGrainCells = previousGrainCells;
        calculate();
    }

    private void calculate() {
        if ("absorbing".equals(boundaryCondition)) {
            left = (column - 1 < 0) ? column : column - 1;
            right = (column + 1 == width) ? width - 1 : column + 1;
            top = (row - 1 < 0) ? row : row - 1;
            bottom = (row + 1 == height) ? height - 1 : row + 1;
        } else {
            left = (column - 1 + width) % width;
            right = (column + 1 + width) % width;
            top = (row - 1 + height) % height;
            bottom = (row + 1 + height) % height;
        }
    }

    public Map<Integer, Integer> checkMoore() {
        numberOfNeighbours = checkVonNeumann();
        checkNeighbour(top, left, numberOfNeighbours);
        checkNeighbour(top, right, numberOfNeighbours);
        checkNeighbour(bottom, left, numberOfNeighbours);
        checkNeighbour(bottom, right, numberOfNeighbours);
        return numberOfNeighbours;
    }

    public Map<Integer, Integer> checkVonNeumann() {
        numberOfNeighbours = new TreeMap<>();
        checkNeighbour(row, left, numberOfNeighbours);
        checkNeighbour(row, right, numberOfNeighbours);
        checkNeighbour(top, column, numberOfNeighbours);
        checkNeighbour(bottom, column, numberOfNeighbours);
        return numberOfNeighbours;
    }

    public Map<Integer, Integer> checkHex(int hexagonalSide) {
        if (hexagonalSide != 0 && hexagonalSide != 1)
            hexagonalSide = generator.nextInt(2);
        switch (hexagonalSide) {
            case 0: //left
                numberOfNeighbours = checkVonNeumann();
                checkNeighbour(top, right, numberOfNeighbours);
                checkNeighbour(bottom, left, numberOfNeighbours);
                break;
            case 1: //right
                numberOfNeighbours = checkVonNeumann();
                checkNeighbour(top, left, numberOfNeighbours);
                checkNeighbour(bottom, right, numberOfNeighbours);
                break;
        }
        return numberOfNeighbours;
    }

    public Map<Integer, Integer> checkPent() {
        int pentagonalSide = generator.nextInt(4);
        switch (pentagonalSide) {
            case 0: //left
                checkNeighbour(top, left, numberOfNeighbours);
                checkNeighbour(top, column, numberOfNeighbours);
                checkNeighbour(row, left, numberOfNeighbours);
                checkNeighbour(bottom, left, numberOfNeighbours);
                checkNeighbour(bottom, column, numberOfNeighbours);
                break;
            case 1: //right
                checkNeighbour(top, column, numberOfNeighbours);
                checkNeighbour(top, right, numberOfNeighbours);
                checkNeighbour(row, right, numberOfNeighbours);
                checkNeighbour(bottom, column, numberOfNeighbours);
                checkNeighbour(bottom, right, numberOfNeighbours);
                break;
            case 2: //top
                checkNeighbour(top, left, numberOfNeighbours);
                checkNeighbour(top, column, numberOfNeighbours);
                checkNeighbour(top, right, numberOfNeighbours);

                checkNeighbour(row, left, numberOfNeighbours);
                checkNeighbour(row, right, numberOfNeighbours);
                break;
            case 3: //bottom
                checkNeighbour(row, left, numberOfNeighbours);
                checkNeighbour(row, right, numberOfNeighbours);

                checkNeighbour(bottom, left, numberOfNeighbours);
                checkNeighbour(bottom, column, numberOfNeighbours);
                checkNeighbour(bottom, right, numberOfNeighbours);
                break;
        }
        return numberOfNeighbours;
    }

    public Map<Integer, Integer> checkWithRadius(double relationRadius, double pointSize) {
        double maxX, minX, maxY, minY;

        for (int neighRow = 0; neighRow < height; neighRow++)
            for (int neighColumn = 0; neighColumn < width; neighColumn++)
                if (previousGrainCells[neighRow][neighColumn].isState()) {
                    maxX = previousGrainCells[neighRow][neighColumn].getCoordinates().getX();
                    minX = previousGrainCells[row][column].getCoordinates().getX();
                    maxY = previousGrainCells[neighRow][neighColumn].getCoordinates().getY();
                    minY = previousGrainCells[row][column].getCoordinates().getY();
                    double distance = Math.sqrt(Math.pow(maxX - minX, 2) +
                            Math.pow(maxY - minY, 2));
                    if (distance <= relationRadius)
                        getNeighboursValues(neighRow, neighColumn, numberOfNeighbours);
                    else if ("periodical".equals(boundaryCondition)) {
                        double maxXAbsorb = maxX > minX ? maxX - width * pointSize : maxX;
                        double minXAbsorb = minX > maxX ? minX - width * pointSize : minX;
                        double maxYAbsorb = maxY > minY ? maxY - height * pointSize : maxY;
                        double minYAbsorb = minY > maxY ? minY - height * pointSize : minY;
                        double distanceX = Math.sqrt(Math.pow(maxX - minX, 2) +
                                Math.pow(maxYAbsorb - minYAbsorb, 2));
                        double distanceY = Math.sqrt(Math.pow(maxXAbsorb - minXAbsorb, 2) +
                                Math.pow(maxY - minY, 2));
                        double distanceXY = Math.sqrt(Math.pow(maxXAbsorb - minXAbsorb, 2) +
                                Math.pow(maxYAbsorb - minYAbsorb, 2));
                        if (distanceX <= relationRadius || distanceY <= relationRadius || distanceXY <= relationRadius)
                            getNeighboursValues(neighRow, neighColumn, numberOfNeighbours);
                    }
                }
        return numberOfNeighbours;
    }

    private void checkNeighbour(int row, int column, Map<Integer, Integer> numberOfNeighbours) {
        if (previousGrainCells[row][column].isState())
            getNeighboursValues(row, column, numberOfNeighbours);
    }

    private void getNeighboursValues(int row, int column, Map<Integer, Integer> numberOfNeighbours) {
        int numberTmp = 1;
        int colour = previousGrainCells[row][column].getColour();
        if (numberOfNeighbours.containsKey(colour)) numberTmp = numberOfNeighbours.get(colour) + 1;
        numberOfNeighbours.put(colour, numberTmp);
    }

    public boolean checkRecrystallise() {
        double maxDensityInNeighbours = 0;
        boolean shouldRecrystallise = false;
        double densityInCurrent = previousGrainCells[this.row][this.column].getDislocationDensity();

        if (previousGrainCells[row][left].isRecrystallised()) {// && maxDensityInNeighbours < densityInCurrent) {
            int numberTmp = 1;
            int colour = previousGrainCells[row][left].getColour();
            if (numberOfRecrystallisedNeighbours.containsKey(colour))
                numberTmp = numberOfRecrystallisedNeighbours.get(colour) + 1;
            numberOfRecrystallisedNeighbours.put(colour, numberTmp);
            shouldRecrystallise = true;
        } else if (maxDensityInNeighbours < previousGrainCells[row][left].getDislocationDensity())
            maxDensityInNeighbours = previousGrainCells[row][left].getDislocationDensity();

        if (previousGrainCells[row][right].isRecrystallised()) {
            int numberTmp = 1;
            int colour = previousGrainCells[row][right].getColour();
            if (numberOfRecrystallisedNeighbours.containsKey(colour))
                numberTmp = numberOfRecrystallisedNeighbours.get(colour) + 1;
            numberOfRecrystallisedNeighbours.put(colour, numberTmp);
            shouldRecrystallise = true;
        } else if (maxDensityInNeighbours < previousGrainCells[row][right].getDislocationDensity())
            maxDensityInNeighbours = previousGrainCells[row][right].getDislocationDensity();

        if (previousGrainCells[top][column].isRecrystallised()) {
            int numberTmp = 1;
            int colour = previousGrainCells[top][column].getColour();
            if (numberOfRecrystallisedNeighbours.containsKey(colour))
                numberTmp = numberOfRecrystallisedNeighbours.get(colour) + 1;
            numberOfRecrystallisedNeighbours.put(colour, numberTmp);
            shouldRecrystallise = true;
        } else if (maxDensityInNeighbours < previousGrainCells[top][column].getDislocationDensity())
            maxDensityInNeighbours = previousGrainCells[top][column].getDislocationDensity();

        if (previousGrainCells[bottom][column].isRecrystallised()) {
            int numberTmp = 1;
            int colour = previousGrainCells[bottom][column].getColour();
            if (numberOfRecrystallisedNeighbours.containsKey(colour))
                numberTmp = numberOfRecrystallisedNeighbours.get(colour) + 1;
            numberOfRecrystallisedNeighbours.put(colour, numberTmp);
            shouldRecrystallise = true;
        } else if (maxDensityInNeighbours < previousGrainCells[bottom][column].getDislocationDensity())
            maxDensityInNeighbours = previousGrainCells[bottom][column].getDislocationDensity();

        if (shouldRecrystallise)
            if (maxDensityInNeighbours < densityInCurrent)
                shouldRecrystallise = true;
            else
                shouldRecrystallise = false;

        return shouldRecrystallise;
    }

    public Map<Integer, Integer> getRecrystallisedNeighbours() {
        return numberOfRecrystallisedNeighbours;
    }
}
