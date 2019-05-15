package model;

public class Neighbour {
    private int row, column, width, height;
    private int left, right, top, bottom;
    private String boundaryCondition;

    public Neighbour(int row, int column, String boundaryCondition, int width, int height) {
        this.row = row;
        this.column = column;
        this.boundaryCondition = boundaryCondition;
        this.width = width;
        this.height = height;
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

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }
}
