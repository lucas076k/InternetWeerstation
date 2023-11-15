import guiboard.*;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Main {
    private static LocalDate begin;
    private static LocalDate end;
    private static Period period;
    private static Measurement currentMeasurement;
    private static LocalDateTime currentTime;

    private static LocalDate lucasBegin;
    private static LocalDate lucasEnd;
    private static Period lucasPeriod;

    private static DotMatrixDisplay display;
    private static SegmentDisplay top;
    private static SegmentDisplay right;
    private static SegmentDisplay left;

    private static Button buttonDown;
    private static Button buttonUp;
    private static Button buttonSelect;

    public static final int LOOPDELAY = 50;


    public static void main(String[] args) {

        IO.init();

        display = new DotMatrixDisplay((short) 0x42, (short) 0x40, 32, 128);
        display.clear();

        buttonDown = new Button(0x90);
        buttonUp = new Button(0x100);
        buttonSelect = new Button(0x80);

        top = new SegmentDisplay(new short[]{0x10, 0x12, 0x14, 0x16, 0x18});
        left = new SegmentDisplay(new short[]{0x20, 0x22, 0x24});
        right = new SegmentDisplay(new short[]{0x30, 0x32, 0x34});


        MenuItem current;
        ArrayList<MenuItem> rootChildren = new ArrayList<>();
        ArrayList<MenuItem> periodChildren = new ArrayList<>();
        ArrayList<MenuItem> statsChildren = new ArrayList<>();
        MenuItem root = new MenuItem("root", display, rootChildren, null);
        MenuItem periodMenu = new MenuItem("Periode", display, periodChildren, root);
        periodMenu.setAction(new MenuItem.Action(periodMenu) {
            @Override
            public MenuItem invoke() {
                return item;
            }
        });
        MenuItem statistics = new MenuItem("Statistieken", display, statsChildren, root);
        statistics.setAction(new MenuItem.Action(statistics) {
            @Override
            public MenuItem invoke() {
                return item;
            }
        });

        MenuItem quit = new MenuItem("Afsluiten", display, null, root);
        quit.setAction(new MenuItem.Action(quit) {
            @Override
            public MenuItem invoke() {
                top.clear();
                left.clear();
                right.clear();
                display.clear();
                System.exit(0);
                return null;
            }
        });
        rootChildren.add(periodMenu);
        rootChildren.add(statistics);


        begin = LocalDate.now();
        end = LocalDate.now();
        period = new Period(begin, end);

        RawMeasurement rawMeasurement = DatabaseConnection.getMostRecentMeasurement();
        if (rawMeasurement == null) {
            System.err.println("Couldn't get most recent measurement!");
        } else {
            currentMeasurement = new Measurement(DatabaseConnection.getMostRecentMeasurement());
            currentTime = LocalDateTime.now().plusMinutes(1);
        }

        MenuItem year = new MenuItem("Jaar", display, null, periodMenu);
        MenuItem month = new MenuItem("Maand", display, null, periodMenu);
        MenuItem day = new MenuItem("Dag", display, null, periodMenu);
        MenuItem periodBack = new MenuItem("Ga terug", display, null, periodMenu);
        periodBack.setAction(new MenuItem.Action(periodMenu) {
            @Override
            public MenuItem invoke() {
                if (!period.getBegin().equals(begin) || !period.getEnd().equals(end)) {
                    display.clear();
                    if (!begin.isAfter(end)) {
                        period = new Period(begin, end);

                        display.clear();
                        display.writeLine("");
                        display.writeLine("      Loading...");
                        period.getMeasurements();
                        display.clear();
                    } else {
                        display.writeLine("     Foutmelding");
                        display.writeLine(" Einde is voor begin");
                        for (; ; ) {
                            if (buttonSelect.changed()) {
                                break;
                            }
                            IO.delay(LOOPDELAY);
                        }
                    }
                }


                return item.getParent();
            }
        });


        year.setAction(new MenuItem.TimeAction(year, begin.getYear(), end.getYear(), 2000, LocalDate.now().getYear(), display, buttonDown, buttonUp, buttonSelect) {
            @Override
            protected void save() {
                begin = LocalDate.of(values[0], begin.getMonth(), begin.getDayOfMonth());
                end = LocalDate.of(values[1], end.getMonth(), end.getDayOfMonth());
            }
        });
        month.setAction(new MenuItem.TimeAction(month, begin.getMonthValue(), end.getMonthValue(), 1, 12, display, buttonDown, buttonUp, buttonSelect) {
            @Override
            protected void save() {
                begin = LocalDate.of(begin.getYear(), values[0], begin.getDayOfMonth());
                end = LocalDate.of(end.getYear(), values[1], end.getDayOfMonth());
            }
        });
        day.setAction(new MenuItem.TimeAction(day, begin.getDayOfMonth(), end.getDayOfMonth(), 1, 31, display, buttonDown, buttonUp, buttonSelect) {
            @Override
            protected void save() {
                try {
                    begin = LocalDate.of(begin.getYear(), begin.getMonth(), values[0]);
                    end = LocalDate.of(end.getYear(), end.getMonth(), values[1]);
                } catch (DateTimeException e) {
                    display.clear();
                    display.writeLine("     Foutmelding");
                    display.writeLine("Dag zit niet in maand");
                    for (; ; ) {
                        if (buttonSelect.changed()) {
                            break;
                        }
                        IO.delay(LOOPDELAY);
                    }
                }
            }
        });
        periodChildren.add(year);
        periodChildren.add(month);
        periodChildren.add(day);
        periodChildren.add(periodBack);

        MenuItem outsideTemp = new MenuItem("Buitentemperatuur", display, null, statistics);
        outsideTemp.setAction(new MenuItem.StatsAction(outsideTemp, display, buttonDown, buttonUp, buttonSelect, " C", "%.1f") {
            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getOutsideTemp();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getOutsideTemp());
                }
            }
        });

        MenuItem insideTemp = new MenuItem("Binnentemperatuur", display, null, statistics);
        insideTemp.setAction(new MenuItem.StatsAction(insideTemp, display, buttonDown, buttonUp, buttonSelect, " C", "%.1f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getInsideTemp();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getInsideTemp());
                }
            }
        });

        MenuItem insideHum = new MenuItem("Bi. luchtvochtigheid", display, null, statistics);
        insideHum.setAction(new MenuItem.StatsAction(insideHum, display, buttonDown, buttonUp, buttonSelect, "%%", "%.0f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getInsideHum();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getInsideHum());
                }
            }
        });

        MenuItem outsideHum = new MenuItem("Bu. luchtvochtigheid", display, null, statistics);
        outsideHum.setAction(new MenuItem.StatsAction(outsideHum, display, buttonDown, buttonUp, buttonSelect, "%%", "%.0f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getOutsideHum();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getOutsideHum());
                }
            }
        });

        MenuItem airPressure = new MenuItem("Luchtdruk", display, null, statistics);
        airPressure.setAction(new MenuItem.StatsAction(airPressure, display, buttonDown, buttonUp, buttonSelect, " hPa", "%.1f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getBarometer();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getBarometer());
                }
            }
        });

        MenuItem windSpeed = new MenuItem("Windsnelheid", display, null, statistics);
        windSpeed.setAction(new MenuItem.StatsAction(windSpeed, display, buttonDown, buttonUp, buttonSelect, " m/s", "%.1f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getWindSpeed();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getWindSpeed());
                }
            }
        });

        MenuItem avgWindSpeed = new MenuItem("Gem windsnelheid", display, null, statistics);
        avgWindSpeed.setAction(new MenuItem.StatsAction(avgWindSpeed, display, buttonDown, buttonUp, buttonSelect, " m/s", "%.1f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getAvgWindSpeed();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getAvgWindSpeed());
                }
            }
        });

        MenuItem windDirection = new MenuItem("Windrichting", display, null, statistics);
        windDirection.setAction(new MenuItem.StatsAction(windDirection, display, buttonDown, buttonUp, buttonSelect, " grad.", "%.0f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getWindDir();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getWindDir());
                }
            }
        });

        MenuItem rainFall = new MenuItem("Neerslag", display, null, statistics);
        rainFall.setAction(new MenuItem.StatsAction(rainFall, display, buttonDown, buttonUp, buttonSelect, " mm", "%.1f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getRainRate();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getRainRate());
                }
            }
        });

        MenuItem uvLevel = new MenuItem("UV-niveau", display, null, statistics);
        uvLevel.setAction(new MenuItem.StatsAction(uvLevel, display, buttonDown, buttonUp, buttonSelect, "", "%.1f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getUVLevel();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getUVLevel());
                }
            }
        });

        MenuItem solarRadiation = new MenuItem("Zonnestraling", display, null, statistics);
        solarRadiation.setAction(new MenuItem.StatsAction(solarRadiation, display, buttonDown, buttonUp, buttonSelect, "W/m2", "%.1f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getSolarRad();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getSolarRad());
                }
            }
        });

        MenuItem xMittBat = new MenuItem("Xmit Batterij", display, null, statistics);
        xMittBat.setAction(new MenuItem.StatsAction(xMittBat, display, buttonDown, buttonUp, buttonSelect) {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getXmitBatt();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getXmitBatt());
                }
            }
        });

        MenuItem battLevel = new MenuItem("Batterij niveau", display, null, statistics);
        battLevel.setAction(new MenuItem.StatsAction(battLevel, display, buttonDown, buttonUp, buttonSelect, "%%", "%.1f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getBattLevel();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getBattLevel());
                }
            }
        });

        MenuItem sunrise = new MenuItem("Zonsopkomst", display, null, statistics);
        sunrise.setAction(new MenuItem.StatsAction(sunrise, display, buttonDown, buttonUp, buttonSelect) {

            @Override
            public void getValues() {

            }

            @Override
            protected void calculateStatistics() {
                Period p = getCurrentPeriod();
                calculated = new ArrayList<>();
                Period.DataObject earliest = p.earliestSunrise();
                if (earliest == null) {
                    calculated.add(new Entry<>("Geen metingen", "", "%s"));
                } else {
                    calculated.add(new Entry<>("Eerste: ", earliest.getTime(), "%s"));
                    calculated.add(new Entry<>("Datum: ", earliest.getDate(), "%s"));
                }
                Period.DataObject latest = p.latestSunrise();
                if (latest == null) {
                    calculated.add(new Entry<>("Geen metingen", "", "%s"));
                } else {
                    calculated.add(new Entry<>("Laatste: ", latest.getTime(), "%s"));
                    calculated.add(new Entry<>("Datum: ", latest.getDate(), "%s"));
                }

            }
        });


        MenuItem sunset = new MenuItem("Zonsondergang", display, null, statistics);
        sunset.setAction(new MenuItem.StatsAction(sunset, display, buttonDown, buttonUp, buttonSelect) {

            @Override
            public void getValues() {

            }

            @Override
            protected void calculateStatistics() {
                Period p = getCurrentPeriod();
                calculated = new ArrayList<>();
                Period.DataObject earliest = p.earliestSunset();
                if (earliest == null) {
                    calculated.add(new Entry<>("Geen metingen", "", "%s"));
                } else {
                    calculated.add(new Entry<>("Eerste: ", earliest.getTime(), "%s"));
                    calculated.add(new Entry<>("Datum: ", earliest.getDate(), "%s"));
                }
                Period.DataObject latest = p.latestSunset();
                if (latest == null) {
                    calculated.add(new Entry<>("Geen metingen", "", "%s"));
                } else {
                    calculated.add(new Entry<>("Laatste: ", latest.getTime(), "%s"));
                    calculated.add(new Entry<>("Datum: ", latest.getDate(), "%s"));
                }

            }
        });

        MenuItem dewPoint = new MenuItem("Dauwpunt", display, null, statistics);
        dewPoint.setAction(new MenuItem.StatsAction(dewPoint, display, buttonDown, buttonUp, buttonSelect, " C", "%.1f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getDewPoint();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getDewPoint());
                }
            }
        });

        MenuItem heatIndex = new MenuItem("Hitte Index", display, null, statistics);
        heatIndex.setAction(new MenuItem.StatsAction(heatIndex, display, buttonDown, buttonUp, buttonSelect, "", "%.0f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getHeatIndex();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getHeatIndex());
                }
            }
        });

        MenuItem windChill = new MenuItem("Gevoelstemperatuur", display, null, statistics);
        windChill.setAction(new MenuItem.StatsAction(windChill, display, buttonDown, buttonUp, buttonSelect, " C", "%.1f") {

            @Override
            public void getValues() {
                Period p = getCurrentPeriod();
                currentValue = getCurrentMeasurement().getWindChill();
                values = new ArrayList<>();
                for (Measurement measurement : p.getMeasurements()) {
                    values.add(measurement.getWindChill());
                }
            }
        });

        MenuItem miscStats = new MenuItem("Persoonlijke delen", display, null, root);
        miscStats.setAction(new MenuItem.StatsAction(miscStats, display, buttonDown, buttonUp, buttonSelect) {
            @Override
            public void getValues() {
            }

            @Override
            protected void calculateStatistics() {
                Period p = getCurrentPeriod();
                calculated = new ArrayList<>();
                // Individual exercises
                calculated.add(new Entry<>("Graaddagen: ", p.degreeDay(), "%.0f"));                                     // Meindert Kempe
                String heatWaveText = p.hasHeatWave() ? "Ja" : "Nee";
                calculated.add(new Entry<>("Hittegolf: ", heatWaveText, "%s"));                                        // Ruben Claassen
                Period.DataObject tristan = p.maxDiff();
                tristan = tristan == null ? new Period.DataObject(LocalDate.of(1,1,1), 0) : tristan;
                calculated.add(new Entry<>("Gevoelstemp verschil:", "", "%s"));
                calculated.add(new Entry<>("-> Datum: ", tristan.getDate(), "%s"));                                     // Tristan van der Put
                calculated.add(new Entry<>("-> Verschil: ", tristan.getValue(), "%.1f", " C"));                   // Tristan van der Put
                calculated.add(new Entry<>("Dagen goed weer: ", p.niceWeather(), "%s"));                                    // Serkan Aydin
                Period.DataObject koen = p.tempDifference();
                koen = koen == null ? new Period.DataObject(LocalDateTime.of(1, 1, 1, 1, 1, 1), 0, 0, 0) : koen;
                calculated.add(new Entry<>("Temperatuur verschil:", "", "%s"));
                calculated.add(new Entry<>("-> Datum: ", koen.getDateTime().toLocalDate(), "%s"));           // Koen Pothof
                calculated.add(new Entry<>("-> Tijd: ", koen.getDateTime().toLocalTime(), "%s"));                      // Koen Pothof
                calculated.add(new Entry<>("-> Buiten temp: ", koen.getOutsideTemp(), "%.1f", "C"));                 // Koen Pothof
                calculated.add(new Entry<>("-> Binnen temp: ", koen.getInsideTemp(), "%.1f", "C"));                   // Koen Pothof
                calculated.add(new Entry<>("-> Verschil: ", koen.getValue(), "%.1f", "C"));                      // Koen Pothof
            }
        });


        lucasBegin = LocalDate.now();
        lucasEnd = LocalDate.now();
        lucasPeriod = new Period(lucasBegin, lucasEnd);

        ArrayList<MenuItem> lucasChildren = new ArrayList<>();
        MenuItem lucasMenu = new MenuItem("Pers. deel Lucas", display, lucasChildren, root);
        lucasMenu.setAction(new MenuItem.Action(lucasMenu) {
            @Override
            public MenuItem invoke() {
                return item;
            }
        });
        MenuItem lucasYear = new MenuItem("Jaar", display, null, lucasMenu);
        lucasYear.setAction(new MenuItem.SingleTimeAction(lucasYear, lucasBegin.getYear(), 2000, LocalDate.now().getYear(), display, buttonDown, buttonUp, buttonSelect) {
            @Override
            protected void save() {
                lucasBegin = LocalDate.of(value, 1, 1);
                lucasEnd = LocalDate.of(value, 12, 31);
            }
        });

        MenuItem lucasResult = new MenuItem("Resultaat", display, null, lucasMenu);
        lucasResult.setAction(new MenuItem.StatsAction(lucasResult, display, buttonDown, buttonUp, buttonSelect) {
            @Override
            public void getValues() {
            }

            @Override
            protected void calculateStatistics() {
                String m;
                boolean error = false;
                if (lucasBegin.equals(lucasEnd)) {
                    m = " Geen jaar gekozen!";
                    error = true;
                } else {
                    display.clear();
                    if (!lucasPeriod.getBegin().equals(lucasBegin) || !lucasPeriod.getEnd().equals(lucasEnd)) {
                        lucasPeriod = new Period(lucasBegin, lucasEnd);
                        display.writeLine("");
                        display.writeLine("      Loading...");
                        lucasPeriod.getMeasurements();
                    }
                    Period p = getLucasPeriod();
                    m = p.monthWithMostRain();
                    display.clear();
                }

                calculated = new ArrayList<>();
                if (m.isEmpty()) {
                    m = "Periode zonder data!";
                    error = true;
                }
                if (error) {
                    calculated.add(new Entry<>("", m, "%s"));
                } else {
                    calculated.add(new Entry<>("Maand: ", m, "%s"));
                }
            }
        });

        MenuItem lucasBack = new MenuItem("Ga terug", display, null, lucasMenu);
        lucasBack.setAction(new MenuItem.Action(lucasBack) {
            @Override
            public MenuItem invoke() {
                if (item.getParent() == null) return null;

                return item.getParent().getParent();
            }
        });

        lucasChildren.add(lucasYear);
        lucasChildren.add(lucasResult);
        lucasChildren.add(lucasBack);

        MenuItem statsBack = new MenuItem("Ga terug", display, null, statistics);
        statsBack.setAction(new MenuItem.Action(statsBack) {
            @Override
            public MenuItem invoke() {
                if (item.getParent() == null) return null;

                return item.getParent().getParent();
            }
        });


        statsChildren.add(outsideTemp);
        statsChildren.add(insideTemp);
        statsChildren.add(insideHum);
        statsChildren.add(outsideHum);
        statsChildren.add(airPressure);
        statsChildren.add(windSpeed);
        statsChildren.add(avgWindSpeed);
        statsChildren.add(windDirection);
        statsChildren.add(rainFall);
        statsChildren.add(uvLevel);
        statsChildren.add(solarRadiation);
        statsChildren.add(xMittBat);
        statsChildren.add(battLevel);
        statsChildren.add(sunrise);
        statsChildren.add(sunset);
        statsChildren.add(dewPoint);
        statsChildren.add(heatIndex);
        statsChildren.add(windChill);
        statsChildren.add(statsBack);

        rootChildren.add(miscStats);
        rootChildren.add(lucasMenu);
        rootChildren.add(quit);

        drawCurrent(currentMeasurement);

        current = root;
        current.draw();


        for (; ; ) {
            if (buttonDown.changed()) {
                current.down();
                current.draw();
            }
            if (buttonUp.changed()) {
                current.up();
                current.draw();
            }
            if (buttonSelect.changed()) {
                MenuItem newItem = current.getChildren().get(current.getSelected()).select();
                if (newItem != null) {
                    current = newItem;
                }
                current.draw();
            }

            if (currentTime.isBefore(LocalDateTime.now())) {
                rawMeasurement = DatabaseConnection.getMostRecentMeasurement();
                if (rawMeasurement == null) {
                    System.err.println("Couldn't get most recent measurement!");
                } else {
                    currentMeasurement = new Measurement(rawMeasurement);
                    currentTime = LocalDateTime.now().plusMinutes(1);
                    drawCurrent(currentMeasurement);
                }

            }
            IO.delay(LOOPDELAY);
        }
    }

    public static void drawCurrent(Measurement measurement) {
        LocalDateTime time = measurement.getDateStamp();
        top.writeNumber(time.getHour() * 100 + time.getMinute());
        left.writeNumber(measurement.getOutsideTemp());
        right.writeNumber(measurement.getInsideTemp());
    }

    public static Period getCurrentPeriod() {
        return period;
    }

    public static Measurement getCurrentMeasurement() {
        return currentMeasurement;
    }

    public static Period getLucasPeriod() {
        return lucasPeriod;
    }
}
