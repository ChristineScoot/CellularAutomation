package controller;

import javafx.concurrent.Task;
import javafx.scene.canvas.GraphicsContext;
import model.GrainCell;
import model.Neighbour;

import java.util.Map;

public class RecrystallisationGrowth extends Task {
    private GrainCell[][] microstructure, previousState;
    private int width, height;
    private GraphicsContext gc;
    private String borderCondition;
    private String colourIndicator;

    public RecrystallisationGrowth(GrainCell[][] microstructure, GraphicsContext gc, String borderCondition) {
        this.microstructure = microstructure;
        this.gc = gc;
        this.borderCondition = borderCondition;
        this.width = microstructure.length;
        this.height = microstructure[0].length;
        this.previousState = new GrainCell[height][width];
        this.colourIndicator = "";
    }

    private void calculate() throws CloneNotSupportedException, InterruptedException {
        Thread.sleep(50);
        updatePreviousGeneration();
        for (int row = 0; row < height; row++)
            for (int column = 0; column < width; column++)
                if (!previousState[row][column].isRecrystallised()) {
                    Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height, previousState);
                    if (neighbour.checkRecrystallise()) {
                        int newColour = 16776960;
                        for (Map.Entry<Integer, Integer> entry : neighbour.getRecrystallisedNeighbours().entrySet()) {
                            newColour = entry.getKey();
                        }
                        microstructure[row][column].setColour(newColour);
                        microstructure[row][column].setDislocationDensity(0);
                        microstructure[row][column].setRecrystallised(true);
                    }
                }
        new CanvasController().print(microstructure, gc, colourIndicator);
    }

    private void updatePreviousGeneration() throws CloneNotSupportedException {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                previousState[i][j] = (GrainCell) microstructure[i][j].clone();
    }

    public void setColourIndicator(String colourIndicator) {
        this.colourIndicator = colourIndicator;
    }

    public String getColourIndicator() {
        return colourIndicator;
    }

    public void setMicrostructure(GrainCell[][] microstructure) {
        this.microstructure = microstructure;
    }

    @Override
    public Object call() throws Exception {
        calculate();
        return microstructure;
    }
}
