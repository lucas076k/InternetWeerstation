import guiboard.Button;
import guiboard.DotMatrixDisplay;
import guiboard.IO;

import java.util.ArrayList;

public class MenuItem {
    private String name;
    private DotMatrixDisplay display;
    private ArrayList<MenuItem> children;
    private MenuItem parent;
    private Action action;
    private int selected;


    public MenuItem(String name, DotMatrixDisplay display, ArrayList<MenuItem> children, MenuItem parent) {
        this.name = name;
        this.display = display;
        this.children = children;
        this.parent = parent;
        this.selected = 0;
    }

    public void draw() {
        display.clear();
        ArrayList<MenuItem> items = getChildren();

        if (items == null || items.size() < 1) return;

        int index = getSelected();

        int start = 0;
        int end = 0;
        if (index > 0 && index < items.size() - 1) {
            start = index - 1;
            end = Math.min(items.size(), index + 2);
        } else if (index == 0) {
            start = 0;
            end = Math.min(items.size(), 3);
        } else if (index == items.size() - 1) {
            start = Math.max(index - 2, 0);
            end = index + 1;
        }


        for (int i = start; i < end; i++) {
            String s = "";
            if (i == index) s += "-";
            else s += " ";

            s += items.get(i).getName();
            display.writeLine(s);
        }
    }

    public ArrayList<MenuItem> getChildren() {
        return children;
    }

    public MenuItem getParent() {
        return parent;
    }

    public int getSelected() {
        return selected;
    }

    public void down() {
        if (selected + 1 < children.size())
            selected++;
        else
            selected = 0;
    }

    public void up() {
        if (selected - 1 >= 0)
            selected--;
        else
            selected = children.size() - 1;
    }

    public String getName() {
        return name;
    }

    public MenuItem select() {
        if (action != null)
            return action.invoke();
        return null;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public static abstract class Action {
        protected final MenuItem item;

        public Action(MenuItem item) {
            this.item = item;
        }

        public abstract MenuItem invoke();
    }

    public static abstract class TimeAction extends Action {
        private final int min;
        private final int max;
        protected int[] values = new int[2];
        private int index = 0;
        private boolean selected = false;
        private final String[] items = new String[]{
                "Begin:  ",
                "Einde:  ",
                "Ga terug"
        };

        private final DotMatrixDisplay display;
        private final Button buttonDown;
        private final Button buttonUp;
        private final Button buttonSelect;

        public TimeAction(MenuItem item, int begin, int end, int min, int max, DotMatrixDisplay display, Button buttonDown, Button buttonUp, Button buttonSelect) {
            super(item);
            this.display = display;
            this.buttonDown = buttonDown;
            this.buttonUp = buttonUp;
            this.buttonSelect = buttonSelect;
            this.min = min;
            this.max = max;
            this.values[0] = begin;
            this.values[1] = end;

        }

        @Override
        public MenuItem invoke() {
            draw(index);
            for (; ; ) {
                if (buttonDown.changed()) {
                    if (!selected && index + 1 <= 2)
                        index++;
                    else if (!selected)
                        index = 0;

                    if (selected && values[index] > min)
                        values[index]--;
                    else if (selected)
                        values[index] = max;

                    draw(index);
                }
                if (buttonUp.changed()) {
                    if (!selected && index - 1 >= 0)
                        index--;
                    else if (!selected)
                        index = 2;

                    if (selected && values[index] < max)
                        values[index]++;
                    else if (selected)
                        values[index] = min;

                    draw(index);
                }
                if (buttonSelect.changed()) {
                    if (index == 0 || index == 1) {
                        selected = !selected;
                    } else {
                        save();
                        display.clear();
                        break;

                    }
                }
                IO.delay(Main.LOOPDELAY);
            }
            return null;
        }

        private void draw(int index) {
            display.clear();

            int start = 0;
            int end = 3;


            for (int i = start; i < end; i++) {
                String s = "";
                if (i == index) s += "-";
                else s += " ";

                s += items[i];
                if (i == 0) s += this.values[i];
                if (i == 1) s += this.values[i];

                display.writeLine(s);
            }
        }

        protected abstract void save();
    }

    public static abstract class SingleTimeAction extends Action {
        private final int min;
        private final int max;
        protected int value;
        private int index = 0;
        private boolean selected = false;
        private final String[] items = new String[]{
                "Waarde: ",
                "Ga terug"
        };

        private final DotMatrixDisplay display;
        private final Button buttonDown;
        private final Button buttonUp;
        private final Button buttonSelect;

        public SingleTimeAction(MenuItem item, int value, int min, int max, DotMatrixDisplay display, Button buttonDown, Button buttonUp, Button buttonSelect) {
            super(item);
            this.display = display;
            this.buttonDown = buttonDown;
            this.buttonUp = buttonUp;
            this.buttonSelect = buttonSelect;
            this.min = min;
            this.max = max;
            this.value = value;
        }

        @Override
        public MenuItem invoke() {
            draw(index);
            for (; ; ) {
                if (buttonDown.changed()) {
                    if (!selected && index + 1 <= 1)
                        index++;
                    else if (!selected)
                        index = 0;

                    if (selected && value > min)
                        value--;
                    else if (selected)
                        value = max;

                    draw(index);
                }
                if (buttonUp.changed()) {
                    if (!selected && index - 1 >= 0)
                        index--;
                    else if (!selected)
                        index = 1;

                    if (selected && value < max)
                        value++;
                    else if (selected)
                        value = min;

                    draw(index);
                }
                if (buttonSelect.changed()) {
                    if (index == 0) {
                        selected = !selected;
                    } else {
                        save();
                        display.clear();
                        break;

                    }
                }
                IO.delay(Main.LOOPDELAY);
            }
            return null;
        }

        private void draw(int index) {
            display.clear();

            int start = 0;
            int end = 2;


            for (int i = start; i < end; i++) {
                String s = "";
                if (i == index) s += "-";
                else s += " ";

                s += items[i];
                if (i == 0) s += this.value;

                display.writeLine(s);
            }
        }

        protected abstract void save();
    }

    public static abstract class StatsAction extends Action {
        private final DotMatrixDisplay display;
        private final Button buttonDown;
        private final Button buttonUp;
        private final Button buttonSelect;
        protected Double currentValue;
        protected ArrayList<Double> values;
        protected ArrayList<Entry> calculated;
        protected String unit;
        protected String formatting;

        private int index;


        public StatsAction(MenuItem item, DotMatrixDisplay display, Button buttonDown, Button buttonUp, Button buttonSelect) {
            this(item, display, buttonDown, buttonUp, buttonSelect, "", "%.2f");
        }

        public StatsAction(MenuItem item, DotMatrixDisplay display, Button buttonDown, Button buttonUp, Button buttonSelect, String unit, String formatting) {
            super(item);
            this.display = display;
            this.buttonDown = buttonDown;
            this.buttonUp = buttonUp;
            this.buttonSelect = buttonSelect;
            this.unit = unit;
            this.formatting = formatting;
        }

        @Override
        public MenuItem invoke() {
            display.clear();
            display.writeLine("");
            display.writeLine("      Loading...");
            getValues();
            calculateStatistics();
            display.clear();

            index = calculated.size() > 1 ? 1 : 0;
            draw();
            for (; ; ) {
                if (buttonDown.changed()) {
                    if (index != 0 && index + 1 < calculated.size() - 1)
                        index++;
                    draw();
                }
                if (buttonUp.changed()) {
                    if (index - 1 > 0)
                        index--;
                    draw();
                }
                if (buttonSelect.changed()) {
                    break;
                }
                IO.delay(Main.LOOPDELAY);
            }
            return item.getParent();
        }

        protected void draw() {
            display.clear();

            if (calculated == null || calculated.size() < 1) return;

            int start = 0;
            int end = 0;
            if (index > 0 && index < calculated.size() - 1) {
                start = index - 1;
                end = Math.min(calculated.size(), index + 2);
            } else if (index >= 0 && index < 3) {
                start = 0;
                end = Math.min(calculated.size(), 3);
            }

            for (int i = start; i < end; i++) {
                String s = String.format(
                        "%s" + calculated.get(i).getFormat() + calculated.get(i).getEndtext(),
                        this.calculated.get(i).getText(),
                        this.calculated.get(i).getValue()
                );

                display.writeLine(s);
            }
        }

        protected void calculateStatistics() {
            calculated = new ArrayList<>();
            calculated.add(new Entry<>("Huidige:   ", currentValue, formatting, unit));
            calculated.add(new Entry<>("Gem.:      ", Period.mean(values), formatting, unit));
            calculated.add(new Entry<>("Mediaan:   ", Period.median(values), formatting, unit));
            calculated.add(new Entry<>("Modus:     ", Period.mode(values), formatting, unit));
            calculated.add(new Entry<>("Hoogste:   ", Period.highest(values), formatting, unit));
            calculated.add(new Entry<>("Laagste:   ", Period.lowest(values), formatting, unit));
            calculated.add(new Entry<>("Std. afw.: ", Period.standardDeviation(values), formatting, unit));
        }

        public abstract void getValues();

        protected static class Entry<T> {
            private final String text;
            private final T value;
            private final String format;
            private String endtext;

            public Entry(String text, T value) {
                this(text, value, "%.2f", "");
            }

            public Entry(String text, T value, String format) {
                this(text, value, format, "");
            }

            public Entry(String text, T value, String format, String endtext) {
                this.text = text;
                this.value = value;
                this.format = format;
                this.endtext = endtext;
            }

            public String getText() {
                return text;
            }

            public T getValue() {
                return value;
            }

            public String getFormat() {
                return format;
            }

            public String getEndtext() {
                return endtext;
            }
        }


    }
}
