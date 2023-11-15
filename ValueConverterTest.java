public class ValueConverterTest {
    public static void main(String[] args) {

        System.out.print("Air pressure: ");
        short givenAirPressure = 29863;
        System.out.printf("%.1f hPa\n", ValueConverter.airPressure(givenAirPressure));

        System.out.print("Sunrise: ");
        short givenSunRise = 706;
        System.out.println(ValueConverter.sunSetRise(givenSunRise));

        System.out.print("Sunset: ");
        short givenSunSet = 2015;
        System.out.println(ValueConverter.sunSetRise(givenSunSet));

        System.out.print("Wind direction: ");
        short givenWindDir = 211;
        System.out.printf("%.0f° \n", ValueConverter.windDirection(givenWindDir));

        System.out.print("Inside humidity: ");
        short givenInsideHumidity = 51;
        System.out.printf("%.0f%% \n", ValueConverter.humidity(givenInsideHumidity));

        System.out.print("Outside humidity: ");
        short givenOutsideHumidity = 83;
        System.out.printf("%.0f%% \n", ValueConverter.humidity(givenOutsideHumidity));

        System.out.print("Rain fall: ");
        short givenRainMeter = 106;
        System.out.printf("%.0f mm/h \n", ValueConverter.rainMeter(givenRainMeter));

        System.out.print("Wind speed: ");
        short givenWindSpeed = 6;
        System.out.printf("%.1f m/s \n", ValueConverter.windSpeed(givenWindSpeed));

        System.out.print("UV index: ");
        short givenUvIndex = 10;
        System.out.printf("%.1f\n", ValueConverter.uvLevel(givenUvIndex));

        System.out.print("Battery level: ");
        short givenBattLevel = 165;
        System.out.printf("%.0f%%\n", ValueConverter.batteryLevel(givenBattLevel));

        System.out.print("Inside temperature: ");
        short givenInsideTemperature = 750;
        System.out.printf("%.1f °C \n", ValueConverter.temperature(givenInsideTemperature));

        System.out.print("Outside temperature: ");
        short givenOutsideTemperature = 1310;
        System.out.printf("%.1f °C \n", ValueConverter.temperature(givenOutsideTemperature));

        double outsideTemp = ValueConverter.temperature((short) 683);
        double outsideHumidity = ValueConverter.humidity((short) 76);
        System.out.print("Dewpoint: ");
        System.out.printf("%.1f °C \n", ValueConverter.dewPoint(outsideTemp, outsideHumidity));

        short outsideTempChill = 547;
        short windSpeed = 2;
        System.out.print("Wind chill: ");
        System.out.printf("%.1f °C \n", ValueConverter.windChill(outsideTempChill, windSpeed));

        System.out.print("Heat index: ");
        short outsideTempIndex = 1040;
        short outsideHumIndex = 50;
        System.out.printf("%.1f °C \n", ValueConverter.heatIndex(outsideTempIndex, outsideHumIndex));

        System.out.print("Average wind speed: ");
        short givenAvgWindSpeed = 10;
        System.out.printf("%.1f m/s \n", ValueConverter.windSpeed(givenAvgWindSpeed));
    }
}