package model;

public class GrainCell {
    private int colour;
    private boolean state;

    public GrainCell(boolean state) {
        this.state = state;
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

    public int getColour() {
        return colour;
    }
}
