package model;

public class GrainCell implements Cloneable {
    private int colour;
    private boolean state;
    private CentreOfGravityCoordinates coordinates;

    public GrainCell(boolean state) {
        this.state = state;
        coordinates=new CentreOfGravityCoordinates(0,0);
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

    public void setCoordinates(CentreOfGravityCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public int getColour() {
        return colour;
    }

    public CentreOfGravityCoordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
