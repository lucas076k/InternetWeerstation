package guiboard;

public class Button {
    private final int address;
    private int lastState;

    public Button(int address) {
        this.address = address;
        this.lastState = read();
    }

    /**
     * @return returns the current value of the button.
     */
    public int read() {
        return IO.readShort(address);
    }

    /**
     * @return returns true if the button state has changed since the last call.
     */
    public boolean changed() {
        int state = read();
        boolean changed = state != lastState;
        lastState = state;
        return changed;
    }

}
