package controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import model.GrainCell;

import java.awt.image.BufferedImage;

public class CanvasController {
    private int width, height;
    private GraphicsContext gc;

    public void print(GrainCell[][] microstructure, GrainCell[][] previousState, GraphicsContext gc, String colourIndicator) {
        this.gc = gc;
        this.width = microstructure.length;
        this.height = microstructure[0].length;
        double pointSize = getPointSize();
        BufferedImage bi = new BufferedImage((int) (width * pointSize), (int) (height * pointSize), BufferedImage.TYPE_INT_RGB);
        double canvasX;
        double canvasY = 0.0;
        for (int i = 0; i < height; i++) {
            canvasX = 0.0;
            for (int j = 0; j < width; j++) {
                if (microstructure[i][j].isState())
                    for (int k = 0; k < pointSize; k++)
                        for (int l = 0; l < pointSize; l++) {
                            if (colourIndicator.equals("recrystallisation"))
                                if (previousState[i][j].isRecrystallised())
                                    bi.setRGB((int) canvasX + k, (int) canvasY + l, 0);
                                else
                                    bi.setRGB((int) canvasX + k, (int) canvasY + l, microstructure[i][j].getColour());
                            else if (colourIndicator.equals("energyColour")) {
                                if (colourIndicator.equals("energyColour"))
                                    bi.setRGB((int) canvasX + k, (int) canvasY + l, microstructure[i][j].getEnergyColour());
                                else
                                    bi.setRGB((int) canvasX + k, (int) canvasY + l, microstructure[i][j].getColour());
                            } else
                                bi.setRGB((int) canvasX + k, (int) canvasY + l, microstructure[i][j].getColour());
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

    private double getPointSize() {
        double pointSizeWidth = gc.getCanvas().getWidth() / width;
        double pointSizeHeight = gc.getCanvas().getHeight() / height;
        return (pointSizeWidth < pointSizeHeight ? pointSizeWidth : pointSizeHeight);
    }
}
