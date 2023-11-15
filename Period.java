import java.time.*;
import java.time.temporal.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to contain a period of time
 *
 * @author Johan Talboom
 * @version 2.0
 */
public class Period {
    private LocalDate beginPeriod;
    private LocalDate endPeriod;
    public static final int DAYS = 0;
    public static final int YEARS = 1;
    private ArrayList<Measurement> measurements;

    /**
     * default constructor, sets the period to today
     */
    public Period() {
        beginPeriod = LocalDate.now();
        endPeriod = LocalDate.now();
    }

    public Period(LocalDate beginPeriod, LocalDate endPeriod) {
        this.beginPeriod = beginPeriod;
        this.endPeriod = endPeriod;
    }

    public Period(LocalDate beginPeriod) {
        this.beginPeriod = beginPeriod;
        this.endPeriod = LocalDate.now();
    }

    public Period(int days) {
        this.beginPeriod = LocalDate.now().minus(java.time.Period.ofDays(days));
        this.endPeriod = LocalDate.now();
    }

    public Period(int time, int type) {
        if (type == DAYS) {
            this.beginPeriod = LocalDate.now().minus(java.time.Period.ofDays(time));
            this.endPeriod = LocalDate.now();
        } else if (type == YEARS) {
            this.beginPeriod = LocalDate.of(time, 1, 1);
            this.endPeriod = LocalDate.of(time, 12, 31);
        }
    }

    /**
     * Simple setter for start of period
     */
    public void setStart(int year, int month, int day) {
        beginPeriod = LocalDate.of(year, month, day);
    }

    /**
     * simple setter for end of period
     */
    public void setEnd(int year, int month, int day) {
        endPeriod = LocalDate.of(year, month, day);
    }

    /**
     * alternative setter for start of period
     *
     * @param beginPeriod
     */
    public void setStart(LocalDate beginPeriod) {
        this.beginPeriod = beginPeriod;
    }

    /**
     * alternative setter for end of period
     *
     * @param endPeriod
     */
    public void setEnd(LocalDate endPeriod) {
        this.endPeriod = endPeriod;
    }

    /**
     * calculates the number of days in the period
     */
    public long numberOfDays() {
        return ChronoUnit.DAYS.between(beginPeriod, endPeriod);
    }


    /**
     * gets all raw measurements of this period from the database
     *
     * @return a list of raw measurements
     */
    public ArrayList<RawMeasurement> getRawMeasurements() {
        return DatabaseConnection.getMeasurementsBetween(LocalDateTime.of(beginPeriod, LocalTime.of(0, 1)), LocalDateTime.of(endPeriod, LocalTime.of(23, 59)));
    }

    /**
     * Builds an ArrayList of measurements. This method also filters out any 'bad' measurements
     *
     * @return a filtered list of measurements
     */
    public ArrayList<Measurement> getMeasurements() {
        // Cache measurements, that saves a lot of loading time
        if (this.measurements != null) return this.measurements;


        ArrayList<Measurement> measurements = new ArrayList<>();
        ArrayList<RawMeasurement> rawMeasurements = getRawMeasurements();
        for (RawMeasurement rawMeasurement : rawMeasurements) {
            Measurement measurement = new Measurement(rawMeasurement);
            if (measurement.isValid()) {
                measurements.add(measurement);
            }
        }
        this.measurements = measurements;
        return measurements;
    }


    public static double mean(ArrayList<Double> numbers) {        //Serkan Aydin
        // Check if there are actually numbers
        if (numbers == null || numbers.size() < 1) return 0;

        double sum = 0;
        for (Double number : numbers) {
            sum += number;
        }
        return sum / numbers.size();
    }

    public static double highest(ArrayList<Double> numbers) {    //Lucas Kuijsters
        // Check if there are actually numbers
        if (numbers == null || numbers.size() < 1) return 0;

        double highest = numbers.get(0);
        for (Double number : numbers) {
            if (highest < number) {
                highest = number;
            }
        }
        return highest;
    }

    public static double lowest(ArrayList<Double> numbers) {    //Tristan van der Put
        // Check if there are actually numbers
        if (numbers == null || numbers.size() < 1) return 0;

        double minimum = numbers.get(0);

        for (Double number : numbers) {
            if (number < minimum) {
                minimum = number;
            }
        }

        return minimum;
    }

    public static double mode(ArrayList<Double> numbers) {    //Koen Pothof
        // Check if there are actually numbers
        if (numbers == null || numbers.size() < 1) return 0;

        HashMap<Double, Integer> map = new HashMap<>();
        for (Double number : numbers) {
            map.put(number, map.getOrDefault(number, 0) + 1);
        }
        int highest = 0;
        double mode = numbers.get(0);
        for (Map.Entry<Double, Integer> entry : map.entrySet()) {
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                mode = entry.getKey();
            }
        }
        return mode;
    }

    public static double median(ArrayList<Double> numbers) {        //Ruben Claassen
        // Check if there are actually numbers
        if (numbers == null || numbers.size() < 1) return 0;

        double median;
        Collections.sort(numbers);
        int length = numbers.size();
        int mid = length / 2;

        if (length % 2 == 0) {
            median = (numbers.get(mid) + numbers.get(mid - 1)) / 2;
        } else {
            median = numbers.get(mid);
        }
        return median;

    }

    public static double standardDeviation(ArrayList<Double> numbers) {    //Meindert Kempe
        // Check if there are actually numbers
        if (numbers == null || numbers.size() < 1) return 0;

        double mean = mean(numbers);
        double sum = 0;
        double tmp;
        for (Double number : numbers) {
            tmp = number - mean;
            sum += (tmp * tmp);
        }
        return Math.sqrt(sum / numbers.size());
    }


    public static ArrayList<Double> maxTempAllDay(ArrayList<Measurement> measurements) {     //Ruben Claassen
        //gets the highest temperature value of each day and adds them to a list
        ArrayList<Double> maxTemps = new ArrayList<>();
        double maxTemp = 0;
        LocalDate dayCheck = LocalDate.now();
        if (measurements.size() > 0) {
            dayCheck = measurements.get(0).getDateStamp().toLocalDate();
        }


        for (Measurement measurement : measurements) {
            if (measurement.getDateStamp().toLocalDate().equals(dayCheck)) {
                if (measurement.getOutsideTemp() >= maxTemp) {
                    maxTemp = measurement.getOutsideTemp();
                }
            } else {
                maxTemps.add(maxTemp);
                maxTemp = measurement.getOutsideTemp();
                dayCheck = measurement.getDateStamp().toLocalDate();

            }
        }
        //adds last day since the loop skips it
        maxTemps.add(maxTemp);
        return maxTemps;
    }


    public boolean hasHeatWave() {                                        // Ruben Claassen
        // A heatwave occurs when the maximum temperature of at least
        // 5 days in a row are 25 C or higher and at least 3 of those are 30 C or higher
        ArrayList<Measurement> measurements = getMeasurements();
        ArrayList<Double> maxTemps = maxTempAllDay(measurements);

        boolean heatwave = false;
        int amountOfDays = maxTemps.size();
        if (amountOfDays > 0) {
            for (int i = 0; i < amountOfDays; i++) {
                if (maxTemps.get(i) >= 25) {
                    int nextCheck = 0;
                    int summerDay = 0;
                    int tropicalDay = 0;
                    while (i + nextCheck < amountOfDays) {
                        if (maxTemps.get(i + nextCheck) >= 25) {
                            summerDay++;
                        } else {
                            break;
                        }
                        if (maxTemps.get(i + nextCheck) >= 30) {
                            tropicalDay++;
                        }
                        nextCheck++;
                    }
                    if (summerDay >= 5 && tropicalDay >= 3) {
                        heatwave = true;
                    }
                }
            }
        }
        return heatwave;
    }


    public DataObject tempDifference() {                    //Koen Pothof
        ArrayList<Measurement> measures = getMeasurements();
        // Check if there are actually measurements
        if (measurements == null || measurements.size() < 1) return null;

        ArrayList<Double> numbers = new ArrayList<>();

        for (int i = 0; i < measures.size(); i++) {
            numbers.add(measures.get(i).getOutsideTemp());
            numbers.add(measures.get(i).getInsideTemp());
        }

        LocalDateTime day = measures.get(0).getDateStamp();
        double outsideTemp = numbers.get(0);
        double insideTemp = numbers.get(0);
        double maxDiff = 0;

        for (int j = 1; j < measures.size(); j++) {
            if (measures.get(j).getDateStamp().getDayOfMonth() != measures.get(j - 1).getDateStamp().getDayOfMonth()) {
                if (numbers.get(j - 1) < outsideTemp) {
                    outsideTemp = numbers.get(j - 1);
                }
                if (numbers.get(j - 1) > insideTemp) {
                    insideTemp = numbers.get(j - 1);
                }
                if (maxDiff < insideTemp - outsideTemp) {
                    maxDiff = Math.abs(insideTemp - outsideTemp);
                    day = measures.get(j - 1).getDateStamp();
                }
            }
        }
        return new DataObject(day, maxDiff, outsideTemp, insideTemp);
    }

    public DataObject maxDiff() {                                    //Tristan van der Put
        ArrayList<Measurement> measurements = getMeasurements();
        // Check if there are actually measurements
        if (measurements == null || measurements.size() < 1) return null;

        double highestDiff = 0;
        LocalDate day = measurements.get(0).getDateStamp().toLocalDate();

        for (Measurement measurement : measurements) {
            double diff = Math.abs(measurement.getOutsideTemp() - measurement.getWindChill());
            if (diff > highestDiff) {
                highestDiff = diff;
                day = measurement.getDateStamp().toLocalDate();
            }
        }

        return new DataObject(day, highestDiff);

    }


    public double degreeDay() {                        //Meindert Kempe
        double degreeDays = 0;
        // reference temperature to calculate degree days against
        final double referenceTemp = 18.0;

        ArrayList<Measurement> measurements = getMeasurements();

        // Check if there are actually measurements
        if (measurements == null || measurements.size() < 1) return 0;

        LocalDate lastday = measurements.get(0).getDateStamp().toLocalDate();
        double sum = 0;
        int count = 0;

        for (Measurement measurement : measurements) {
            // When we get to the next day, calculate the degree days for that day
            if (!measurement.getDateStamp().toLocalDate().equals(lastday)) {
                double avg = sum / count;
                degreeDays += Math.max(referenceTemp - avg, 0);
                lastday = measurement.getDateStamp().toLocalDate();
                sum = 0;
                count = 0;
            }
            sum += measurement.getOutsideTemp();
            count++;
        }

        // Add last day
        double avg = sum / count;
        degreeDays += Math.max(referenceTemp - avg, 0);

        return degreeDays;
    }


    public String monthWithMostRain() {                                 //Lucas Kuijsters
        ArrayList<Measurement> measurements = getMeasurements();
        // Check if there are actually measurements
        if (measurements == null || measurements.size() < 1) return "";

        double[] months = new double[12];

        for (Measurement measurement : measurements) {
            if (measurement.getRainRate() != 0) {
                int m = measurement.getDateStamp().get(ChronoField.MONTH_OF_YEAR);
                months[m - 1] += measurement.getRainRate();
            }
        }

        double highestRain = months[0];
        int highestMonth = 0;
        if (measurements.size() < 1) return "Invalid value.";

        for (int i = 0; i < months.length; i++) {
            if (highestRain < months[i]) {
                highestRain = months[i];
                highestMonth = i;
            }
        }
        String month = Month.of(highestMonth + 1).name().toLowerCase();
        return month.replaceFirst(".", (month.charAt(0) + "").toUpperCase());
    }

    //AvgOutside temp >=15 && <=30,                                    //Serkan Aydin
    //Rainrate <5
    //avgWindspeed <=5
    //outsideHum <80
    public int niceWeather() {
        ArrayList<Measurement> measurements = getMeasurements();
        // Check if there are actually measurements
        if (measurements == null || measurements.size() < 1) return 0;

        int daysNiceWeather = 0;
        double totalOutsidetemp = 0;
        double totalRainrate = 0;
        double totalOutsideHum = 0;
        int amountMeasurements = 0;

        //First day of all measurements are put into lastday
        LocalDate lastday = measurements.get(0).getDateStamp().toLocalDate();

        //Goes through all measurements
        for (Measurement measurement : measurements) {
            //Check if current measurement is equal to lastday
            if (measurement.getDateStamp().toLocalDate().equals(lastday)) {

                totalOutsidetemp += measurement.getOutsideTemp();
                totalRainrate += measurement.getRainRate();
                totalOutsideHum += measurement.getOutsideHum();
                amountMeasurements++;
            } else {
                double averageOutsidetemp = totalOutsidetemp / amountMeasurements;
                double avgRainrate = totalRainrate / amountMeasurements;
                double avgOutsideHum = totalOutsideHum / amountMeasurements;


                if (averageOutsidetemp >= 15 && averageOutsidetemp <= 30 && avgRainrate < 5
                        && measurement.getAvgWindSpeed() <= 5 && avgOutsideHum < 80) {

                    daysNiceWeather++;

                }

                lastday = measurement.getDateStamp().toLocalDate();
                //newday of measurement is put in lastday

                amountMeasurements = 0;
                totalOutsideHum = 0;
                totalOutsidetemp = 0;
                totalRainrate = 0;
            }


        }
        return daysNiceWeather;
    }

    public DataObject earliestSunset() {
        ArrayList<Measurement> measurements = getMeasurements();

        if (measurements == null || measurements.size() < 1) return null;


        LocalTime earliest = measurements.get(0).getSunset();
        LocalDate date = measurements.get(0).getDateStamp().toLocalDate();
        for (Measurement measurement : measurements) {
            if (measurement.getSunset().isBefore(earliest)) {
                earliest = measurement.getSunset();
                date = measurement.getDateStamp().toLocalDate();
            }
        }
        return new DataObject(date, earliest);
    }

    public DataObject latestSunset() {
        ArrayList<Measurement> measurements = getMeasurements();

        if (measurements == null || measurements.size() < 1) return null;


        LocalTime latest = measurements.get(0).getSunset();
        LocalDate date = measurements.get(0).getDateStamp().toLocalDate();
        for (Measurement measurement : measurements) {
            if (measurement.getSunset().isAfter(latest)) {
                latest = measurement.getSunset();
                date = measurement.getDateStamp().toLocalDate();
            }
        }
        return new DataObject(date, latest);
    }

    public DataObject earliestSunrise() {
        ArrayList<Measurement> measurements = getMeasurements();

        if (measurements == null || measurements.size() < 1) return null;


        LocalTime earliest = measurements.get(0).getSunrise();
        LocalDate date = measurements.get(0).getDateStamp().toLocalDate();
        for (Measurement measurement : measurements) {
            if (measurement.getSunrise().isBefore(earliest)) {
                earliest = measurement.getSunrise();
                date = measurement.getDateStamp().toLocalDate();
            }
        }
        return new DataObject(date, earliest);
    }

    public DataObject latestSunrise() {
        ArrayList<Measurement> measurements = getMeasurements();

        if (measurements == null || measurements.size() < 1) return null;


        LocalTime latest = measurements.get(0).getSunrise();
        LocalDate date = measurements.get(0).getDateStamp().toLocalDate();
        for (Measurement measurement : measurements) {
            if (measurement.getSunrise().isAfter(latest)) {
                latest = measurement.getSunrise();
                date = measurement.getDateStamp().toLocalDate();
            }
        }
        return new DataObject(date, latest);
    }


    public LocalDate getBegin() {
        return beginPeriod;
    }

    public LocalDate getEnd() {
        return endPeriod;
    }


    public static class DataObject {
        private LocalDate date;
        private LocalDateTime dateTime;
        private LocalTime time;
        private double value;
        private double outsideTemp;
        private double insideTemp;


        public DataObject(LocalDate date, double value) {
            this.date = date;
            this.value = value;
        }

        public DataObject(LocalDateTime dateTime, double value, double outsideTemp, double insideTemp) {
            this.dateTime = dateTime;
            this.value = value;
            this.outsideTemp = outsideTemp;
            this.insideTemp = insideTemp;
        }

        public DataObject(LocalDate date, LocalTime time) {
            this.date = date;
            this.time = time;
        }


        public double getValue() {
            return value;
        }


        public LocalDate getDate() {
            return date;
        }

        public LocalTime getTime() {
            return time;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public double getOutsideTemp() {
            return outsideTemp;
        }

        public double getInsideTemp() {
            return insideTemp;
        }
    }


}
