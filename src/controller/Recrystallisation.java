package controller;

import javafx.concurrent.Task;
import javafx.scene.canvas.GraphicsContext;
import model.Coordinates;
import model.GrainCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Recrystallisation extends Task {
    private GrainCell[][] microstructure;
    private int width, height;
    private Random generator = new Random();
    private GraphicsContext gc;

    public Recrystallisation(GrainCell[][] microstructure, GraphicsContext gc) {
        this.microstructure = microstructure;
        this.gc = gc;
        this.width = microstructure.length;
        this.height = microstructure[0].length;
    }

    private void calculate() throws IOException {
        //TODO Make it work faster somehow
        final String recrystallisationData = System.getProperty("user.dir") + "/src/recrystallisation.xls";

        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(new File(recrystallisationData));
        } catch (FileNotFoundException e) {
            System.out.println("plik juz otwarto");
            return;
        }
        Sheet sheet = workbook.getSheetAt(0);
        workbook.close();

        int numberOfRows = sheet.getPhysicalNumberOfRows();
        double time, ro, nextRo, sigma, avgDislocationsInCell, chunk30percent, chunk10percent;

        double roCritical = sheet.getRow(69).getCell(1).getNumericCellValue() / (width * height);
        double sumDislocationsDensity = 0;
        List listOfBorderCells = initializeBorderList();
        List listOfCenterCells = initializeCenterList();
        for (int i = 4; i < numberOfRows - 1; i++) {
            time = sheet.getRow(i).getCell(0).getNumericCellValue();
            ro = sheet.getRow(i).getCell(1).getNumericCellValue();
            nextRo = sheet.getRow(i + 1).getCell(1).getNumericCellValue();
//            sigma = sheet.getRow(i).getCell(2).getNumericCellValue();
            avgDislocationsInCell = (nextRo - ro) / (width * height);
            chunk30percent = avgDislocationsInCell * 0.3;
            chunk10percent = avgDislocationsInCell * 0.1;
            double dislocations = nextRo - ro;

            for (int column = 0; column < width; column++) {
                for (int row = 0; row < height; row++) {
                    microstructure[row][column].addDislocationDensity(chunk30percent);
                    dislocations -= chunk30percent;
                    sumDislocationsDensity += chunk30percent;
                }
            }
            while (dislocations > chunk10percent) {
                addSmallChunk(chunk10percent, listOfBorderCells, listOfCenterCells);
                dislocations -= chunk10percent;
                sumDislocationsDensity += chunk10percent;
            }
            addSmallChunk(dislocations, listOfBorderCells, listOfCenterCells); //what's left
            sumDislocationsDensity += dislocations;
            updateFile(i, sumDislocationsDensity, time);
        }
        crystalliseNucleation(roCritical);
    }

    private void crystalliseNucleation(double roCritical) {
        double dislocations;
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                dislocations = microstructure[row][column].getDislocationDensity();
                if (dislocations > roCritical && microstructure[row][column].getEnergy() != 0) {
                    microstructure[row][column].setRecrystallised(true);
                    microstructure[row][column].setDislocationDensity(roCritical);
                }
            }
        }
        new CanvasController().print(microstructure, microstructure, gc, "recrystallisation");
    }

    private void updateFile(int i, double numberOfDislocations, double timeStep) throws IOException {
        final String newRecrystallisationData = System.getProperty("user.dir") + "/src/newrecrystallisation.xls";
        Workbook workbook;
        Sheet sheet;
        FileInputStream inputStream = new FileInputStream(new File(newRecrystallisationData));
        try {
            workbook = WorkbookFactory.create(inputStream);
            sheet = workbook.getSheetAt(0);

        } catch (Exception e) {
            workbook = new HSSFWorkbook();
            sheet = workbook.createSheet("Dislocation");
            FileOutputStream fileOut = new FileOutputStream(newRecrystallisationData);
            workbook.write(fileOut);
            fileOut.close();
        }

        Row headerRow = sheet.createRow(0);
        Cell cell0 = headerRow.createCell(0);
        Cell cell1 = headerRow.createCell(1);
        cell0.setCellValue("Time step");
        cell1.setCellValue("Dislocation");

        Row row = sheet.createRow(i - 3);
        Cell cellDislocation = row.createCell(1);
        Cell cellTime = row.createCell(0);
        cellDislocation.setCellValue(numberOfDislocations);
        cellTime.setCellValue(timeStep);

        inputStream.close();
        FileOutputStream outputStream = new FileOutputStream(newRecrystallisationData);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    private void addSmallChunk(double chunk, List listOfBorderCells, List listOfCenterCells) {
        int isOnGrainBorder = generator.nextInt(100);
        int nr;
        Coordinates index;
        if (isOnGrainBorder < 80) { //border gets dislocation
            nr = generator.nextInt(listOfBorderCells.size());
            index = (Coordinates) listOfBorderCells.get(nr);
        } else { //center gets dislocation
            nr = generator.nextInt(listOfCenterCells.size());
            index = (Coordinates) listOfCenterCells.get(nr);
        }
        microstructure[(int) index.getX()][(int) index.getY()].addDislocationDensity(chunk);
    }

    private List initializeBorderList() {
        List list = new LinkedList();
        for (int column = 0; column < width; column++)
            for (int row = 0; row < height; row++)
                if (microstructure[row][column].getEnergy() > 0)
                    list.add(new Coordinates(column, row));
        return list;
    }

    private List initializeCenterList() {
        List list = new LinkedList();
        for (int column = 0; column < width; column++)
            for (int row = 0; row < height; row++)
                if (microstructure[row][column].getEnergy() == 0)
                    list.add(new Coordinates(column, row));
        return list;
    }

    @Override
    protected Object call() throws Exception {
        calculate();
        return microstructure;
    }
}