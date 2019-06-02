package controller;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import model.GrainCell;
import model.Neighbour;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class RecrystallisationGrowth extends Task {
    private GrainCell[][] microstructure, previousState;
    private int width, height;
    private GraphicsContext gc;
    private String borderCondition;

    public RecrystallisationGrowth(GrainCell[][] microstructure, GraphicsContext gc, String borderCondition) {
        this.microstructure = microstructure;
        this.gc = gc;
        this.borderCondition = borderCondition;
        this.width = microstructure.length;
        this.height = microstructure[0].length;
        this.previousState = new GrainCell[height][width];
    }

    private void calculate() throws IOException, CloneNotSupportedException {
        updatePreviousGeneration();
        boolean grew = true;
        while (grew) {
            grew=false;
            for (int row = 0; row < height; row++)
                for (int column = 0; column < width; column++)
                    if (!previousState[row][column].isRecrystallised()) {
                        Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height, previousState);
                        if (neighbour.checkRecrystiallise()){
                            final String recrystallisationData = System.getProperty("user.dir") + "/src/recrystallisation.xls";

                            Workbook workbook;
                            try {
                                workbook = WorkbookFactory.create(new File(recrystallisationData));
                            } catch (FileNotFoundException e) {
                                System.out.println("plik juz otwarto");
                                return;
                            }
                            Sheet sheet = workbook.getSheetAt(0);
                            double roCritical = sheet.getRow(69).getCell(1).getNumericCellValue() / (width * height);
                            workbook.close();

                            microstructure[row][column].setDislocationDensity(roCritical);
                            microstructure[row][column].setRecrystallised(true);
                            grew=true;
                        }
//                            grew = setNewCell(row, column, numberOfNeighbours);
                    }
                    updatePreviousGeneration();
                    print();
        }
//        return grew;
    }

    private boolean setNewCell(int row, int column, Map<Integer, Integer> numberOfNeighbours) {
        microstructure[row][column].setState(true);
        microstructure[row][column].setColour(findMostCommonNeighbour(numberOfNeighbours));
        return true;
    }

    private int findMostCommonNeighbour(Map<Integer, Integer> numberOfNeighbours) {
        Map.Entry<Integer, Integer> maxColour = null;
        for (Map.Entry<Integer, Integer> tmpColour : numberOfNeighbours.entrySet())
            if (maxColour == null || tmpColour.getValue().compareTo(maxColour.getValue()) > 0)
                maxColour = tmpColour;
        return maxColour.getKey();
    }

    private void print() {
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
                            if (previousState[i][j].isRecrystallised())
                                bi.setRGB((int) canvasX + k, (int) canvasY + l, 0);
                            else
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

    private void updatePreviousGeneration() throws CloneNotSupportedException {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                previousState[i][j] = (GrainCell) microstructure[i][j].clone();
    }

    @Override
    protected Object call() throws Exception {
        updatePreviousGeneration();
        calculate();
        return microstructure;
    }
}
