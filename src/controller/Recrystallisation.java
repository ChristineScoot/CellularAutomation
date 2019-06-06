package controller;

import javafx.concurrent.Task;
import javafx.scene.canvas.GraphicsContext;
import model.Coordinates;
import model.GrainCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Recrystallisation extends Task {
    private GrainCell[][] microstructure;
    private int width, height;
    private Random generator = new Random();
    private GraphicsContext gc;
    private double A, B;
    private String borderCond;
    private RecrystallisationGrowth recrystallisationGrowth;

    public Recrystallisation(GrainCell[][] microstructure, GraphicsContext gc, double A, double B, String borderCond) {
        this.microstructure = microstructure;
        this.gc = gc;
        this.width = microstructure.length;
        this.height = microstructure[0].length;
        this.A = A;
        this.B = B;
        this.borderCond = borderCond;
    }

    private void calculate() throws Exception {
        double time = 0, ro = 0, nextRo, avgDislocationsInCell, chunk30percent, chunk10percent;
        double roCritical = ((A / B) + (1 - A / B) * Math.pow(Math.E, (-B * 0.065))) / (width * height);
        double sumDislocationsDensity = 0;
        List listOfBorderCells = initializeBorderList();
        List listOfCenterCells = initializeCenterList();
        recrystallisationGrowth = new RecrystallisationGrowth(microstructure, gc, borderCond);
        for (int i = 0; i <= 200; i++) {
            System.out.println("Recrystalisation numero: " + (i + 1));
            nextRo = (A / B) + (1 - A / B) * Math.pow(Math.E, (-B * time));
            avgDislocationsInCell = (nextRo - ro) / (width * height);
            chunk30percent = avgDislocationsInCell * 0.3;
            double dislocations = nextRo - ro;

            for (int column = 0; column < width; column++) {
                for (int row = 0; row < height; row++) {
                    microstructure[row][column].addDislocationDensity(chunk30percent);
                    dislocations -= chunk30percent;
                    sumDislocationsDensity += chunk30percent;
                }
            }
            chunk10percent = dislocations * 0.001;
            while (dislocations > chunk10percent) {
                addSmallChunk(chunk10percent, listOfBorderCells, listOfCenterCells);
                dislocations -= chunk10percent;
                sumDislocationsDensity += chunk10percent;
            }
            addSmallChunk(dislocations, listOfBorderCells, listOfCenterCells); //what's left
            sumDislocationsDensity += dislocations;
            updateFile(i, sumDislocationsDensity, time);
            ro = nextRo;
            time += 0.001;
            crystalliseNucleation(roCritical);

            recrystallisationGrowth.call();
            recrystallisationGrowth.setMicrostructure(microstructure);
        }
    }

    private void crystalliseNucleation(double roCritical) {
        double dislocations;
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) {
                dislocations = microstructure[row][column].getDislocationDensity();
                if (dislocations > roCritical && microstructure[row][column].getEnergy() != 0) {
                    microstructure[row][column].setRecrystallised(true);
                    microstructure[row][column].setDislocationDensity(0);
                    int redish = generator.nextInt(255);
                    redish = redish << 16;
                    microstructure[row][column].setColour(redish);
                }
            }
        }
        new CanvasController().print(microstructure, gc, recrystallisationGrowth.getColourIndicator());
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

        Row row = sheet.createRow(i + 1);
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

    public void setColourIndicator(int clickCountDislocation) {
        if (recrystallisationGrowth.getColourIndicator().equals("dislocationColour"))
            recrystallisationGrowth.setColourIndicator("recrystallisation");
        else
            recrystallisationGrowth.setColourIndicator("dislocationColour");
        if (clickCountDislocation % 2 == 0)
            new CanvasController().print(microstructure, gc, "recrystallisation");
        else
            new CanvasController().print(microstructure, gc, "dislocationColour");
    }

    @Override
    protected Object call() throws Exception {
        calculate();
        return microstructure;
    }
}