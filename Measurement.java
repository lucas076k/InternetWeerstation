import java.time.LocalDateTime;
import java.time.LocalTime;

public class Measurement {
    private String stationId;
    private LocalDateTime dateStamp;
    private Double barometer;
    private Double insideTemp;
    private Double outsideTemp;
    private Double insideHum;
    private Double outsideHum;
    private Double windSpeed;
    private Double avgWindSpeed;
    private Double windDir;
    private Double rainRate;
    private Double UVLevel;
    private Double solarRad;
    private Double xmitBatt;
    private Double battLevel;
    private Short foreIcon;
    private LocalTime sunrise;
    private LocalTime sunset;

    private Double dewPoint;
    private Double heatIndex;
    private Double windChill;

    private RawMeasurement rawMeasurement;

    public Measurement(RawMeasurement rawMeasurement) {
        this.rawMeasurement = rawMeasurement;

        this.stationId = rawMeasurement.getStationId();
        this.dateStamp = rawMeasurement.getDateStamp();
        this.foreIcon = rawMeasurement.getForeIcon();
    }

    public String getStationId() {
        return stationId;
    }

    public LocalDateTime getDateStamp() {
        return dateStamp;
    }

    public double getBarometer() {
        if (barometer == null) {
            barometer = ValueConverter.airPressure(rawMeasurement.getBarometer());
        }
        return barometer;
    }

    public double getInsideTemp() {
        if (insideTemp == null) {
            insideTemp = ValueConverter.temperature(rawMeasurement.getInsideTemp());
        }
        return insideTemp;
    }

    public double getOutsideTemp() {
        if (outsideTemp == null) {
            outsideTemp = ValueConverter.temperature(rawMeasurement.getOutsideTemp());
        }
        return outsideTemp;
    }

    public double getInsideHum() {
        if (insideHum == null) {
            insideHum = ValueConverter.humidity(rawMeasurement.getInsideHum());
        }
        return insideHum;
    }

    public double getOutsideHum() {
        if (outsideHum == null) {
            outsideHum = ValueConverter.humidity(rawMeasurement.getOutsideHum());
        }
        return outsideHum;
    }

    public double getWindSpeed() {
        if (windSpeed == null) {
            windSpeed = ValueConverter.windSpeed(rawMeasurement.getWindSpeed());
        }
        return windSpeed;
    }

    public double getAvgWindSpeed() {
        if (avgWindSpeed == null) {
            avgWindSpeed = ValueConverter.windSpeed(rawMeasurement.getAvgWindSpeed());
        }
        return avgWindSpeed;
    }

    public double getWindDir() {
        if (windDir == null) {
            windDir = ValueConverter.windDirection(rawMeasurement.getWindDir());
        }
        return windDir;
    }

    public double getRainRate() {
        if (rainRate == null) {
            rainRate = ValueConverter.rainMeter(rawMeasurement.getRainRate());
        }
        return rainRate;
    }

    public double getUVLevel() {
        if (UVLevel == null) {
            UVLevel = ValueConverter.uvLevel(rawMeasurement.getUVLevel());
        }
        return UVLevel;
    }

    public double getSolarRad() {
        if (solarRad == null) {
            solarRad = ValueConverter.solarRadiation(rawMeasurement.getSolarRad());
        }
        return solarRad;
    }

    public double getXmitBatt() {
        if (xmitBatt == null) {
            xmitBatt = ValueConverter.batteryLevel(rawMeasurement.getXmitBatt());
        }
        return xmitBatt;
    }

    public double getBattLevel() {
        if (battLevel == null) {
            battLevel = ValueConverter.batteryLevel(rawMeasurement.getBattLevel());
        }
        return battLevel;
    }

    public short getForeIcon() {
        return foreIcon;
    }

    public LocalTime getSunrise() {
        if (sunrise == null) {
            sunrise = ValueConverter.sunSetRise(rawMeasurement.getSunrise());
        }
        return sunrise;
    }


    public LocalTime getSunset() {
        if (sunset == null) {
            sunset = ValueConverter.sunSetRise(rawMeasurement.getSunset());
        }
        return sunset;
    }

    public Double getDewPoint() {
        if (dewPoint == null) {
            dewPoint = ValueConverter.dewPoint(getOutsideTemp(), getOutsideHum());
        }
        return dewPoint;
    }

    public Double getHeatIndex() {
        if (heatIndex == null) {
            heatIndex = ValueConverter.heatIndex(rawMeasurement.getOutsideTemp(), rawMeasurement.getOutsideHum());
        }
        return heatIndex;
    }

    public Double getWindChill() {
        if (windChill == null) {
            windChill = ValueConverter.windChill(rawMeasurement.getOutsideTemp(), rawMeasurement.getWindSpeed());
        }
        return windChill;
    }

    public boolean isValid() {
        if (rawMeasurement.getBarometer() == 0) {
            return false;
        }
        if (rawMeasurement.getInsideTemp() == 32767) {
            return false;
        }
        if (rawMeasurement.getOutsideTemp() == 32767) {
            return false;
        }
        if (rawMeasurement.getInsideHum() == 255) {
            return false;
        }
        if (rawMeasurement.getOutsideHum() == 255) {
            return false;
        }
        if (rawMeasurement.getWindSpeed() == 255) {
            return false;
        }
        if (rawMeasurement.getAvgWindSpeed() == 255) {
            return false;
        }
        if (rawMeasurement.getWindDir() == 32767) {
            return false;
        }
        if (rawMeasurement.getRainRate() == 32767) {
            return false;
        }
        if (rawMeasurement.getUVLevel() == 255) {
            return false;
        }
        if (rawMeasurement.getSolarRad() == 32767) {
            return false;
        }
        if (rawMeasurement.getSunrise() == 32767) {
            return false;
        }
        if (rawMeasurement.getSunset() == 32767) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "Station ID: %s\n" +
                        "Date: %s\n" +
                        "Air pressure: %.1f hPa\n" +
                        "Inside temperature: %.1f°C\n" +
                        "Outside temperature: %.1f°C\n" +
                        "Inside humidity: %.0f%%\n" +
                        "Outside humidity: %.0f%%\n" +
                        "Wind speed: %.1f m/s\n" +
                        "Average wind speed: %.1f m/s\n" +
                        "Wind direction: %.0f°\n" +
                        "Rain fall: %.1f mm\n" +
                        "UV level: %.0f\n" +
                        "Solar radiation: %.1f W/m²\n" +
                        "xMitt battery: %.0f%%\n" +
                        "Battery level: %.0f%%\n" +
                        "Sunrise: %s\n" +
                        "Sunset: %s\n" +
                        "Dew point: %.1f°C\n" +
                        "Heat index: %.0f\n" +
                        "Wind chill: %.1f°C"
                , getStationId(), getDateStamp().toString(), getBarometer(), getInsideTemp(), getOutsideTemp(),
                getInsideHum(), getOutsideHum(), getWindSpeed(), getAvgWindSpeed(), getWindDir(), getRainRate(), getUVLevel(),
                getSolarRad(), getXmitBatt(), getBattLevel(), getSunrise(), getSunset(), getDewPoint(), getHeatIndex(), getWindChill()
        );
    }
}

