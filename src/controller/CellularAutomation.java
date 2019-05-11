package controller;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CellularAutomation {
    private GraphicsContext gc;
    private int numberOfGenerations, length;
    private int rule;
    private boolean[] ruleBin, previousState, currentState;
    double canvasY = 0.0;

    public CellularAutomation(int rule, GraphicsContext gc, int numberOfGenerations, int length) {
        this.rule = rule;
        this.gc = gc;
        this.numberOfGenerations = numberOfGenerations;
        this.length = length;
        ruleToBinary();
        firstGeneration();
        nextGenerations();
    }

    private void ruleToBinary() {
        String tmp = Integer.toBinaryString(rule);
        ruleBin = new boolean[8];

        int start = 0;
        for (int i = tmp.length() - 1; i >= 0; i--) {
            if (tmp.charAt(i) == '0')
                ruleBin[start] = false;
            else
                ruleBin[start] = true;
            start++;
        }
        while (start < 8) {
            ruleBin[start] = false;
            start++;
        }
    }

    private void firstGeneration() {
        previousState = new boolean[length];
        currentState = new boolean[length];
        for (int i = 0; i < length; i++) {
            if (i == length / 2 - 1)
                previousState[i] = true;
            else
                previousState[i] = false;
        }
        printGeneration(previousState);
    }

    private void updatePreviousGeneration() {
        for (int i = 0; i < length; i++) {
            previousState[i] = currentState[i];
        }
    }

    private void printGeneration(boolean[] generation) {
        double pointSize = gc.getCanvas().getWidth() / length;
        double canvasX = 0.0;
        for (int i = 0; i < length; i++) {
            if (generation[i])
                gc.setFill(Color.BLACK);
            else
                gc.setFill(Color.WHITE);
            gc.fillRect(canvasX, canvasY, pointSize, pointSize);
            canvasX += pointSize;
        }
        canvasY += pointSize;
    }

    private void nextGenerations() {
        boolean leftNeighbour;
        boolean rightNeighbour;
        for (int gen = 0; gen < numberOfGenerations; gen++) {
            for (int i = 0; i < length; i++) {
                leftNeighbour=previousState[(i-1+length)%length];
                rightNeighbour=previousState[(i+1+length)%length];

                StringBuilder triplet = new StringBuilder();
                triplet.append(leftNeighbour ? 1 : 0);
                triplet.append(previousState[i] ? 1 : 0);
                triplet.append(rightNeighbour ? 1 : 0);
                int tri = Integer.parseInt(triplet.toString(), 2);
                currentState[i] = ruleBin[tri];
            }
            updatePreviousGeneration();
            printGeneration(previousState);
        }
    }
}
