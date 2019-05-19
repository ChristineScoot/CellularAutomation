package model;

public class GrainCell implements Cloneable {
    private int colour;
    private boolean state;
    private Coordinates coordinates;

    public GrainCell(boolean state) {
        this.state = state;
        coordinates = new Coordinates(0, 0);
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public int getColour() {
        return colour;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
