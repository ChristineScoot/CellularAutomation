package model;

public class GrainCell implements Cloneable {
    private int colour;
    private boolean state;
    private Coordinates coordinates;
    private int energy = 0;
    private double dislocationDensity = 0;
    private boolean recrystallised = false;

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
    }

    public int getDislocationColour() {
        if (!recrystallised) { //blue
            return 100;
        }
        return (int) ((dislocationDensity / 4.68) * 65536);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getEnergy() {
        return energy;
    }

    public void addDislocationDensity(double density) {
        this.dislocationDensity += density;
    }

    public void setDislocationDensity(double dislocationDensity) {
        this.dislocationDensity = dislocationDensity;
    }

    public double getDislocationDensity() {
        return dislocationDensity;
    }

    public void setRecrystallised(boolean recrystallised) {
        this.recrystallised = recrystallised;
    }

    public boolean isRecrystallised() {
        return recrystallised;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
