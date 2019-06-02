package controller;

import javafx.concurrent.Task;
import javafx.scene.canvas.GraphicsContext;
import model.GrainCell;
import model.Neighbour;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
            grew = false;
            for (int row = 0; row < height; row++)
                for (int column = 0; column < width; column++)
                    if (!previousState[row][column].isRecrystallised()) {
                        Neighbour neighbour = new Neighbour(row, column, borderCondition, width, height, previousState);
                        if (neighbour.checkRecrystiallise()) {
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
                            grew = true;
                        }
                    }
            updatePreviousGeneration();
            new CanvasController().print(microstructure, previousState, gc, "recrystallisation");
        }
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
