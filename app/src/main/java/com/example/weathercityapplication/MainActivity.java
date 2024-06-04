package com.example.weathercityapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weathercityapplication.R;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText cityNameEditText;
    private TextView locationTextView;
    private TextView timeTextView;
    private TextView temperatureTextView;
    private TextView humidityTextView;
    private TextView descriptionTextView;
    private Button fetchWeatherButton;

    private SharedPreferences sharedPreferences;

    private static final String TAG = "MainActivity";
    private static final String API_KEY = "e3fb247aa2740b901efa952004dc7553";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityNameEditText = findViewById(R.id.cityNameEditText);
        locationTextView = findViewById(R.id.locationTextView);
        timeTextView = findViewById(R.id.timeTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        fetchWeatherButton = findViewById(R.id.fetchWeatherButton);

        sharedPreferences = getSharedPreferences("WeatherApp", MODE_PRIVATE);

        String lastCity = sharedPreferences.getString("lastCity", "");
        if (!lastCity.isEmpty()) {
            cityNameEditText.setText(lastCity);
            fetchWeatherData(lastCity);
        }

        fetchWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = cityNameEditText.getText().toString();
                if (!cityName.isEmpty()) {
                    fetchWeatherData(cityName);
                    sharedPreferences.edit().putString("lastCity", cityName).apply();
                }
            }
        });

        displayCurrentTime();
    }

    private void fetchWeatherData(String cityName) {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_KEY + "&units=metric";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            parseJsonResponse(response);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Volley error: " + error.getMessage());
                if (error.networkResponse != null) {
                    Log.e(TAG, "Error Response code: " + error.networkResponse.statusCode);
                }
            }
        });

        queue.add(jsonObjectRequest);
    }

    private void parseJsonResponse(JSONObject response) throws JSONException {
        JSONObject main = response.getJSONObject("main");
        double temperature = main.getDouble("temp");
        int humidity = main.getInt("humidity");

        String description = response.getJSONArray("weather").getJSONObject(0).getString("description");

        temperatureTextView.setText("Temperature: " + temperature + "°C");
        humidityTextView.setText("Humidity: " + humidity + "%");
        descriptionTextView.setText("Description: " + description);

        String location = response.getString("name") + ", " + response.getJSONObject("sys").getString("country");
        locationTextView.setText("Location: " + location);

        Log.d(TAG, "Weather data parsed successfully");
    }

    private void displayCurrentTime() {
        String currentTime = java.text.DateFormat.getTimeInstance().format(new java.util.Date());
        timeTextView.setText("Time: " + currentTime);
    }
}
