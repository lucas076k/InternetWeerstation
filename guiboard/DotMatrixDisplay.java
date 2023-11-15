package guiboard;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

public class DotMatrixDisplay {
    private final short pixelAddress;
    private final short textAddress;
    private final int height;
    private final int width;
    private static final int LINELENGTH = 21;
    private static final int MAXLINES = 3;

    private int currentLine;

    private static class Opcodes {
        public static final int CLEAR = 0;
        public static final int SET = 1;
        public static final int SHIFT = 2;
        public static final int CLEARALL = 3;
    }

    private final boolean[][] lastState;
    private boolean lastStateKnown;


    /**
     * @param pixelAddress Address used for writing to individual pixels.
     * @param textAddress  Address used for writing text.
     * @param height       Height in pixels.
     * @param width        Width in pixels.
     */
    public DotMatrixDisplay(short pixelAddress, short textAddress, int height, int width) {
        if (height - 1 > 0b11111 || width - 1 > 0b1111111)
            throw new IllegalArgumentException();

        this.pixelAddress = pixelAddress;
        this.textAddress = textAddress;
        this.height = height;
        this.width = width;
        this.currentLine = 0;

        this.lastState = new boolean[width][height];
        this.lastStateKnown = false;
    }

    /**
     * Set or clear specified pixel.
     *
     * @param x     X coordinate of pixel.
     * @param y     Y coordinate of pixel.
     * @param state State of pixel to set (true for on, false for off).
     */
    public void setPixel(int x, int y, boolean state) {
        if (y > 0b11111 || x > 0b1111111)
            throw new IllegalArgumentException(Integer.toBinaryString(x) + ":" + Integer.toBinaryString(y));

        if (!lastStateKnown || lastState[x][y] != state) {
            writeInstruction(state ? Opcodes.SET : Opcodes.CLEAR, x, y);
            lastState[x][y] = state;
        }
    }

    /**
     * @param fn     Function used to plot the graph.
     * @param xScale Scale of the x-axis.
     * @param yScale Scale of the y-axis
     * @param step   Step size used to smooth the graph.
     */
    public void plotGraph(DoubleUnaryOperator fn, double xScale, double yScale, double step) {
        for (int x = 0; x < width; x++) {
            setPixel(x, height / 2, true);
        }
        for (int y = 0; y < height; y++) {
            setPixel(width / 2, y, true);
        }

        for (double x = 0; x < width; x += step) {
            int y = (int) Math.round(-fn.applyAsDouble(
                    ((x - (width / 2.0)) * xScale)
            ) * yScale) + height / 2;
            if (y < height && y >= 0)
                setPixel((int) x, y, true);
        }

    }

    /**
     * Write a line of text to the dotmatrix display.
     *
     * @param s Text to write to the display.
     */
    public void writeLine(String s) {
        if (s.length() > LINELENGTH) throw new IllegalArgumentException();
        if (currentLine > MAXLINES - 1) throw new IllegalStateException();
        for (char c : s.toCharArray()) {
            IO.writeShort(textAddress, c);
        }
        IO.writeShort(textAddress, '\n');
        this.currentLine++;
    }

    public int getLineLength() {
        return LINELENGTH;
    }

    public int getMaxLines() {
        return MAXLINES;
    }

    /**
     * @return Current line number (0-indexed).
     */
    public int getCurrentLine() {
        return currentLine;
    }

    /**
     * Clears dot matrix display and resets cursor position.
     */
    public void clear() {
        // Use text address to clear display, because CLEARALL opcode does not reset cursor.
        IO.writeShort(textAddress, 0xFE);
        IO.writeShort(textAddress, 0x01);
        this.currentLine = 0;

        for (boolean[] row : lastState)
            Arrays.fill(row, false);
        lastStateKnown = true;
    }

    /**
     * Write an instruction to the dot matrix display.
     *
     * @param opcode Opcode (see DotMatrixDisplay.Opcodes).
     * @param x      X value.
     * @param y      Y value.
     */
    private void writeInstruction(int opcode, int x, int y) {
        int instruction = opcode << 12;
        instruction |= x << 5;
        instruction |= y;
        IO.writeShort(pixelAddress, instruction);
    }
}
