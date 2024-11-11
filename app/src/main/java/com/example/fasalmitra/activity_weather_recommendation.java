package com.example.fasalmitra;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class activity_weather_recommendation extends AppCompatActivity {

    private TextView tvWeatherInfo;
    private TextView tvCropRecommendations;
    private Button btnFetchWeather;
    private Spinner spinnerLocation;

    private static final String API_KEY = "57322d139523b819bc908933aca327bc";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_recommendation);

        tvWeatherInfo = findViewById(R.id.tvWeatherInfo);
        tvCropRecommendations = findViewById(R.id.tvCropRecommendations);
        btnFetchWeather = findViewById(R.id.tvWeatherbtn);
        spinnerLocation = findViewById(R.id.spinnerLocation);

        List<String> locations = new ArrayList<>();
        locations.add("Rajasthan");
        locations.add("Sikkim");
        locations.add("Punjab");
        locations.add("Mumbai");
        locations.add("Kerala");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(adapter);

        // Fetch weather data when the button is clicked
        btnFetchWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = spinnerLocation.getSelectedItem().toString();
                fetchWeatherData(location);
            }
        });
    }

    private void fetchWeatherData(String location) {
        String url = BASE_URL + location + "&appid=" + API_KEY + "&units=metric";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject main = response.getJSONObject("main");
                            double temp = main.getDouble("temp");
                            double humidity = main.getDouble("humidity");
                            JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
                            String description = weather.getString("description");

                            tvWeatherInfo.setText("Temperature: " + temp + "°C\n" +
                                    "Humidity: " + humidity + "%\n" +
                                    "Condition: " + description);

                            provideCropRecommendations(temp, humidity);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(activity_weather_recommendation.this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("WeatherAPI", "Error fetching weather data", error);
                Toast.makeText(activity_weather_recommendation.this, "Error fetching weather data", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonObjectRequest);
    }

    // Function to provide crop recommendations based on weather
    private void provideCropRecommendations(double temp, double humidity) {
        String recommendations = "Recommended Crops:\n";

        if (temp > 25 && humidity > 60) {
            recommendations += "- Rice\n- Corn\n- Sugarcane";
        } else if (temp < 25 && humidity < 60) {
            recommendations += "- Wheat\n- Barley\n- Oats";
        } else {
            recommendations += "- Tomatoes\n- Peppers\n- Lettuce";
        }

        tvCropRecommendations.setText(recommendations);
    }
}
