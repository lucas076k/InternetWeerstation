package guiboard;

import java.util.HashMap;
import java.util.Map;

public class SegmentDisplay {

    public static final short ZERO = 0b100111111;
    public static final short ONE = 0b100000110;
    public static final short TWO = 0b101011011;
    public static final short THREE = 0b101001111;
    public static final short FOUR = 0b101100110;
    public static final short FIVE = 0b101101101;
    public static final short SIX = 0b101111101;
    public static final short SEVEN = 0b100000111;
    public static final short EIGHT = 0b101111111;
    public static final short NINE = 0b101101111;
    public static final short DOT = (0x100 | 1 << 7);
    public static final short MINUS = (0x100 | 1 << 6);
    public static final short EMPTY = (0x100);

    private static final HashMap<Short, Integer> reverseNumMap;
    private static final HashMap<Integer, Short> numMap;

    static {
        HashMap<Short, Integer> rNMap = new HashMap<>();
        HashMap<Integer, Short> nMap = new HashMap<>();
        rNMap.put(ZERO, 0);
        rNMap.put(ONE, 1);
        rNMap.put(TWO, 2);
        rNMap.put(THREE, 3);
        rNMap.put(FOUR, 4);
        rNMap.put(FIVE, 5);
        rNMap.put(SIX, 6);
        rNMap.put(SEVEN, 7);
        rNMap.put(EIGHT, 8);
        rNMap.put(NINE, 9);
        for (Map.Entry<Short, Integer> entry : rNMap.entrySet()) {
            nMap.put(entry.getValue(), entry.getKey());
        }
        reverseNumMap = rNMap;
        numMap = nMap;
    }


    private final short[] addresses;

    /**
     * @param addresses array containing addresses of segments to make up the segment display
     *                  (order from small to large for reading and writing numbers)
     */
    public SegmentDisplay(short[] addresses) {
        this.addresses = addresses;
    }

    /**
     * @return Count of segments which make up the display.
     */
    public int getSegmentCount() {
        return addresses.length;
    }

    /**
     * Clear all segments of the display.
     */
    public void clear() {
        for (short address : addresses) {
            IO.writeShort(address, 0x100);
        }
    }

    /**
     * Clear a single segment of the display.
     *
     * @param segment Index of segment to clear.
     */
    public void clear(int segment) {
        if (segment > addresses.length - 1)
            throw new IllegalArgumentException("Invalid segment provided");

        IO.writeShort(addresses[segment], 0x100);
    }

    /**
     * Writes raw values to segments.
     *
     * @param values Raw values to write to segments.
     */
    public void writeRaw(short[] values) {
        int length = Math.min(values.length, addresses.length);
        for (int i = 0; i < length; i++) {
            IO.writeShort(addresses[i], values[i]);
        }
    }

    /**
     * Write raw value to a single segment of the display.
     *
     * @param value   Raw value to write to segment.
     * @param segment Index of segment to write to.
     */
    public void writeRaw(short value, int segment) {
        if (segment > addresses.length - 1)
            throw new IllegalArgumentException("Invalid segment provided");

        IO.writeShort(addresses[segment], value);
    }

    /**
     * @return Raw values currently displayed on the segment display.
     */
    public short[] readRaw() {
        short[] results = new short[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            results[i] = IO.readShort(addresses[i]);
        }
        return results;
    }

    /**
     * @param segment Index of segment to read.
     * @return Raw value currently displayed on the segment
     */
    public short readRaw(int segment) {
        if (segment > addresses.length - 1)
            throw new IllegalArgumentException("Invalid segment provided");

        return IO.readShort(addresses[segment]);
    }

    /**
     * Write a number to the display.
     *
     * @param number Number to write to the display.
     */
    public void writeNumber(int number) {
        writeNumber(number, false, true);
    }

    /**
     * Write a number to the display.
     *
     * @param number      Number to write to the display.
     * @param leadingZero Add leading zeroes to the display.
     */
    public void writeNumber(int number, boolean leadingZero) {
        writeNumber(number, leadingZero, true);
    }


    /**
     * Write a number to the display.
     *
     * @param number Number to write to the display.
     */
    public void writeNumber(double number) {
        writeNumber(number, -1, false, true);
    }

    /**
     * Write a number to the display.
     *
     * @param number    Number to write to the display.
     * @param sigDigits Number of digits to display after the dot. -1 for maximum.
     */
    public void writeNumber(double number, int sigDigits) {
        writeNumber(number, sigDigits, false, true);
    }

    /**
     * Write a number to the display.
     *
     * @param number      Number to write to the display.
     * @param sigDigits   Number of digits to display after the dot. -1 for maximum.
     * @param leadingZero Add leading zeroes to the display.
     */
    public void writeNumber(double number, int sigDigits, boolean leadingZero) {
        writeNumber(number, sigDigits, leadingZero, true);
    }

    /**
     * Write a number to the display.
     *
     * @param number       Number to write to the display.
     * @param leadingZero  Add leading zeroes to the display.
     * @param clearDisplay Clear display prior to writing the number.
     */
    public void writeNumber(int number, boolean leadingZero, boolean clearDisplay) {

        boolean negative = number < 0;
        number = negative ? -number : number;
        int segments = getSegmentCount();

        if (number > (Math.pow(10, (segments - (negative ? 1 : 0))) - 1))
            throw new IllegalArgumentException("Number too large for display");

        int digits = 0;
        for (int i = 0; i < segments; i++) {
            if (number != 0 || leadingZero || i == 0) {
                writeRaw(numMap.get(number % 10), i);
                digits++;
            } else if (clearDisplay) clear(i);
            number /= 10;
        }

        if (negative) {
            writeRaw(MINUS, leadingZero ? segments - 1 : digits);
        }
    }

    /**
     * Write a number to the display.
     *
     * @param number       Number to write to the display.
     * @param sigDigits    Number of digits to display after the dot. -1 for maximum.
     * @param leadingZero  Add leading zeroes to the display.
     * @param clearDisplay Clear display prior to writing the number.
     */
    public void writeNumber(double number, int sigDigits, boolean leadingZero, boolean clearDisplay) {
        int maxSigDigits = getSegmentCount() - ((int) Math.log10(number < 0 ? -number : number) + 1) - (number < 0 ? 1 : 0);

        if (sigDigits > maxSigDigits)
            throw new IllegalArgumentException("Too many significant digits for display");

        if (sigDigits == -1) sigDigits = maxSigDigits;

        writeNumber((int) (number * Math.pow(10, sigDigits)), leadingZero, clearDisplay);
        writeRaw((short) (readRaw(sigDigits) | DOT), sigDigits);
    }

    /**
     * @return Number currently displayed on the segment display.
     */
    public double readNumber() {
        int result = 0;
        int dot = 0;
        int segments = getSegmentCount();

        for (int i = 0; i < segments; i++) {
            short read = readRaw(i);

            // If there are no more digits break out of loop
            if (read == EMPTY) {
                break;
            }

            if (read == MINUS) {
                result = -result;
                break;
            }

            // Check for dot sign
            if (!reverseNumMap.containsKey(read)) {
                read = (short) ((read ^ DOT) | 0x100);
                dot = i;

                // In case of unrecognised digit throw exception
                if (!reverseNumMap.containsKey(read)) {
                    throw new NumberFormatException();
                }
            }
            result += (Math.pow(10, i)) * reverseNumMap.get(read);
        }
        // Place decimal point and return value
        return result / Math.pow(10, dot);
    }
}
