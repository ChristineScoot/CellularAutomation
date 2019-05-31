package model;

public class GrainCell implements Cloneable {
    private int colour;
    private boolean state;
    private Coordinates coordinates;
    private int energy = 0;

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

    public int getEnergyColour() {
        if (energy == 0)
            return 0;
        return (int) (0.125 * energy * 255); //very dark bluish
//        return (int)(0.125*energy*35280+255);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
