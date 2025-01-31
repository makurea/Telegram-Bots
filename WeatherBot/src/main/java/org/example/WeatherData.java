//WeatherData.java
package org.example;

public class WeatherData {
    private int temperature;
    private int feelsLike;
    private String description;
    private int windSpeed;
    private int humidity;
    private int pressure;
    private int minTemp;
    private int maxTemp;
    private String iconCode;

    // Конструктор (опционально)
    public WeatherData() {
    }

    // Геттеры и сеттеры
    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(int feelsLike) {
        this.feelsLike = feelsLike;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getIconCode() {
        return iconCode;
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }

    // Переопределение метода toString() для удобного вывода
    @Override
    public String toString() {
        return "WeatherData{" +
                "temperature=" + temperature +
                ", feelsLike=" + feelsLike +
                ", description='" + description + '\'' +
                ", windSpeed=" + windSpeed +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                ", minTemp=" + minTemp +
                ", maxTemp=" + maxTemp +
                ", iconCode='" + iconCode + '\'' +
                '}';
    }
}