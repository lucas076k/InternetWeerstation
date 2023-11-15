import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PeriodTestMain {
    public static void main(String[] args) {
        Period period = new Period(LocalDate.of(2024, 7, 1), LocalDate.of(2024, 10, 1));
        System.out.println(period.tempDifference());
////        System.out.println(measurements.get(0));
////        System.out.println(period.numberOfDays());
////        System.out.println(Period.maxTempAllDay(measurements));




    }

    public static void degreeDayTest(Period period) {                            //Meindert Kempe
        //https://www.knmi.nl/over-het-knmi/nieuws/graaddagen-in-gasjaar-2021
        //https://energiebespareninfo.nl/kennisbank/energiebegrippen/wat-zijn-graaddagen/
        System.out.println(period.degreeDay());

        HashMap<LocalDate, ArrayList<Double>> map = new HashMap<>();
        for (Measurement measurement : period.getMeasurements()) {
            LocalDate key = measurement.getDateStamp().truncatedTo(ChronoUnit.DAYS).toLocalDate();
            map.put(key, map.getOrDefault(key, new ArrayList<>()));

            map.get(key).add(measurement.getOutsideTemp());
        }

        double degreeDays, average;
        double total = 0;
        for (Map.Entry<LocalDate, ArrayList<Double>> entry : map.entrySet()) {
            average = Period.mean(entry.getValue());
            degreeDays = Math.max(18 - average, 0); //degree days can't be lower than 0
            total += degreeDays;
            System.out.printf("avg temp: %.1f, deg days: %.1f\n", average, degreeDays);
        }
        System.out.println(total);
    }


}
