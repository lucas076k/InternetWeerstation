import java.time.LocalTime;

public class ValueConverter {

    /**
     * airPressure
     *
     * @param inchOfMercuryRaw Air pressure in inch of mercury.
     * @return in hecto Pascal / hPa
     */
    public static double airPressure(short inchOfMercuryRaw) {
        return (inchOfMercuryRaw / 1000.0) / 0.029530;
    }

    /**
     * sunSetRise
     *
     * @param sunRiseRaw Raw measurement value from vp2pro weather station
     * @return Sunset in hh:mm format
     */
    public static LocalTime sunSetRise(short sunRiseRaw) {
        return formatTime(sunRiseRaw);
    }

    /**
     * formatTime
     *
     * @param value time as minutes + (100 * hours)
     * @return hh:mm formatted string
     */
    private static LocalTime formatTime(short value) {
        int hours = value / 100;
        int minutes = value % 100;
        return LocalTime.of(hours, minutes, 0);
    }

    /**
     * windDirection
     *
     * @param windDirection Raw measurement value from vp2pro weather station in degrees
     * @return The wind direction in degrees
     */
    public static double windDirection(short windDirection) {
        return windDirection;
    }

    /**
     * humidity
     *
     * @param humidity in percentages
     * @return
     */
    public static double humidity(short humidity) {

        return humidity;
    }

    /**
     * rainMeter
     *
     * @param rainInches Raw measurement value from vp2pro weather station in inches per hour
     * @return The amount of rain in mm
     */
    public static double rainMeter(short rainInches) {
        return (rainInches / 100.0) * 25.4;
    }

    /**
     * windSpeed
     *
     * @param windMiles Windspeed in miles per hour/mph
     * @return Windspeed in meters per second
     */
    public static double windSpeed(short windMiles) {
        return windMiles * 0.44704;
    }

    /**
     * uvLevel
     *
     * @param uv UV level
     * @return UV level
     */
    public static double uvLevel(short uv) {

        return uv / 10.0;
    }

    /**
     * solarRadiation
     *
     * @param rad solar radiation in W/m^2
     * @return solar radiation in W/m^2
     */
    public static double solarRadiation(short rad) {
        return rad;
    }

    /**
     * batteryLevel
     *
     * @param rawValue input in Volt
     * @return output in percentages
     */
    public static double batteryLevel(short rawValue) {
        return ((rawValue * 300.0) / 512);
    }

    /**
     * temperature
     *
     * @param fahrenheit Temperature in Fahrenheit × 10
     * @return Temperature in Celcius
     */
    public static double temperature(short fahrenheit) {

        return ((fahrenheit / 10.0) - 32) / 1.8;
    }

    /**
     * dewpoint
     *
     * @param outsideTemperature outside temperature in °C
     * @param outsideHumidity    outside relative humidity
     * @return dewpoint in °C
     */
    public static double dewPoint(double outsideTemperature, double outsideHumidity) {
        /*
         * Formula:
         * The following method is used to calculate dewpoint:
         * v = RH*0.01*6.112 * exp [(17.62*T)/(T + 243.12)],
         * this equation will provide the vapor pressure value (in pressure units) where T is the air
         * temperature in C and RH is the relative humidity.
         * Now dewpoint, Td, can be found:
         * Numerator = 243.12*(ln v) – 440.1
         * Denominator = 19.43 – ln v
         * Td = Numerator/Denominator
         */
        double v = outsideHumidity * 0.01 * 6.112 * Math.exp((17.62 * outsideTemperature) / (243.12 + outsideTemperature));
        return (243.12 * Math.log(v) - 440.1) / (19.43 - Math.log(v));
    }


    /**
     * windChill
     *
     * @param temperature is temperature in °F * 10
     * @param windMiles   is windSpeed in mph
     * @return windChill in °C
     */
    public static double windChill(short temperature, short windMiles) {
        if (windMiles > 55) {
            windMiles = 55;
        }
        int fahrenheit = (temperature / 10);
        double windcool = ((0.0817 * (3.71 * (Math.pow(windMiles, 0.5)) + 5.81 - (0.25 * windMiles)) * (fahrenheit - 91.4) + 91.4) - 32) / 1.8;
        if (windcool > temperature(temperature)) {
            return temperature(temperature);
        } else {
            return windcool;
        }
    }

    /**
     * Heat index
     *
     * @param temperature is temperature in °F * 10
     * @param humidity    is  outside relative humidity
     * @return heatIndex in °C
     */
    public static double heatIndex(short temperature, short humidity) {

        double C1 = -42.379;
        double C2 = 2.04901523;
        double C3 = 10.14333127;
        double C4 = -0.22475541;
        double C5 = -.00683783;
        double C6 = -5.481717E-2;
        double C7 = 1.22874E-3;
        double C8 = 8.5282E-4;
        double C9 = -1.99E-6;
        int fahrenheit = (temperature / 10);
        double heat = C1 + (C2 * fahrenheit) + (C3 * humidity) + (C4 * fahrenheit * humidity) + (C5 * Math.pow(fahrenheit, 2)) + (C6 * Math.pow(humidity, 2)) + (C7 * Math.pow(fahrenheit, 2) * humidity) + (C8 * fahrenheit * Math.pow(humidity, 2)) + (C9 * Math.pow(fahrenheit, 2) * Math.pow(humidity, 2));

        double heatIndex = (heat - 32) / 1.8;
        return heatIndex;
    }
}
