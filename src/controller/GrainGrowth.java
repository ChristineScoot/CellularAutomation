package controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import model.GrainCell;

import java.awt.image.BufferedImage;
import java.util.Random;

public class GrainGrowth {
    private GraphicsContext gc;
    private GrainCell[][] grainCells;
    private int width, height;

    public GrainGrowth(GraphicsContext gc, GrainCell[][] initializeGrainCells, int width, int height) {
        this.gc = gc;
        this.grainCells = initializeGrainCells;
        this.width = width;
        this.height = height;
        printStep(initializeGrainCells);
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

    private double getPointSize() {
        double pointSizeWidth = gc.getCanvas().getWidth() / width;
        double pointSizeHeight = gc.getCanvas().getHeight() / height;
        return (pointSizeWidth < pointSizeHeight ? pointSizeWidth : pointSizeHeight);
    }

    public void setCell(int x, int y) {
        int row, column;
        column = (int) (x / getPointSize());
        row = (int) (y / getPointSize());
        if (row < height && column < width) {
            grainCells[row][column].setState(!grainCells[row][column].isState());
            if(grainCells[row][column].isState()) {
                Random generator = new Random();
                int colour = generator.nextInt(16777215);
                grainCells[row][column].setColour(colour);
            }
//            updatePreviousGeneration();
//            printStep(previousState);
            printStep(grainCells);
        }
    }
}
