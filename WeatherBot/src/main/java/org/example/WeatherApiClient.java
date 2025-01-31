//WeatherApiClient.java

package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import java.io.IOException;

public class WeatherApiClient {
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&lang=ru&appid=%s";
    private static final Gson gson = new Gson();
    private static final OkHttpClient client = new OkHttpClient();

    public static WeatherData fetchWeatherData(String city, String apiKey) {
        String url = String.format(API_URL, city, apiKey);
        String jsonResponse = makeHttpRequest(url);

        if (jsonResponse != null) {
            return parseWeatherData(jsonResponse);
        }
        return null;
    }

    private static String makeHttpRequest(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static WeatherData parseWeatherData(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        WeatherData weatherData = new WeatherData();
        weatherData.setTemperature(jsonObject.getAsJsonObject("main").get("temp").getAsInt());
        weatherData.setFeelsLike(jsonObject.getAsJsonObject("main").get("feels_like").getAsInt());
        weatherData.setDescription(jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString());
        weatherData.setWindSpeed(jsonObject.getAsJsonObject("wind").get("speed").getAsInt());
        weatherData.setHumidity(jsonObject.getAsJsonObject("main").get("humidity").getAsInt());
        weatherData.setPressure(jsonObject.getAsJsonObject("main").get("pressure").getAsInt());
        weatherData.setMinTemp(jsonObject.getAsJsonObject("main").get("temp_min").getAsInt());
        weatherData.setMaxTemp(jsonObject.getAsJsonObject("main").get("temp_max").getAsInt());
        weatherData.setIconCode(jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").getAsString());

        return weatherData;
    }
}