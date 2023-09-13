package com.example.weather_forecast;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Spinner cboCountry;
    private Spinner cboCity;
    private EditText txtLocation;
    private Button btnForecast;
    private TextView txtCity, txtCountry, txtLat, txtLong, txtId;
    private ListView lvwForecast;
    private ArrayAdapter<String> forecastAdapter;

    // Map to store countries and their respective city arrays
    private Map<String, String[]> cities = new HashMap<>();

    private static final String API_KEY = "38bee84be9ecc1e0a08df0245901ce9e";
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private static final String ForecastUrl = BASE_URL + "forecast?q=@LOC@&units=imperial&APPID=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        // Initialize the cities map
        cities.put("Vietnam", new String[]{"Hanoi", "Ho Chi Minh", "Da Nang"});
        cities.put("Japan", new String[]{"Tokyo", "Osaka", "Yokohama"});
        cities.put("United States", new String[]{"New York", "Los Angeles", "Chicago"});
        cities.put("France", new String[]{"Paris", "Marseille", "Lyon"});
        cities.put("United Kingdom", new String[]{"London", "Manchester", "Birmingham"});
        cities.put("Australia", new String[]{"Sydney", "Melbourne", "Brisbane"});
        cities.put("China", new String[]{"Beijing", "Shanghai", "Guangzhou"});
        cities.put("Russia", new String[]{"Moscow", "Saint Petersburg", "Novosibirsk"});
        cities.put("Brazil", new String[]{"São Paulo", "Rio de Janeiro", "Brasília"});
        cities.put("Egypt", new String[]{"Cairo", "Alexandria", "Giza"});
        // ... Add city arrays for other countries

        cboCountry = findViewById(R.id.cboCountry);
        cboCity = findViewById(R.id.cboCity);

        btnForecast = findViewById(R.id.btnForecast);
        txtCity = findViewById(R.id.txtCity);
        txtCountry = findViewById(R.id.txtCountry);
        txtLat = findViewById(R.id.txtLat);
        txtLong = findViewById(R.id.txtLong);
        txtId = findViewById(R.id.txtId);
        lvwForecast = findViewById(R.id.lvwForecast);

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities.keySet().toArray(new String[0]));
        cboCountry.setAdapter(countryAdapter);
        cboCountry.setSelection(0);

        cboCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCountry = (String) parentView.getItemAtPosition(position);
                updateCitySpinner(selectedCountry);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        forecastAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lvwForecast.setAdapter(forecastAdapter);

        btnForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String country = (String) cboCountry.getSelectedItem();
                String city;

                if (cboCity.isEnabled()) {
                    city = (String) cboCity.getSelectedItem();
                } else {
                    city = txtLocation.getText().toString();
                }

                if (city.isEmpty()) {
                    // Hiển thị thông báo lỗi nếu không nhập tên thành phố
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = createUrl(city);

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String jsonData = response.body().string();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        displayForecast(jsonData);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    private void updateCitySpinner(String country) {
        String[] cityArray = cities.get(country);
        if (cityArray != null) {
            cboCity.setEnabled(true);
            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityArray);
            cboCity.setAdapter(cityAdapter);
        } else {
            cboCity.setEnabled(false);
        }
    }



    private String createUrl(String city) {
        return ForecastUrl.replace("@LOC@", city);
    }
    private void displayForecast(String jsonData) throws JSONException {
        // Phân tích dữ liệu JSON từ phản hồi API
        JSONObject json = new JSONObject(jsonData);

        // Lấy thông tin vị trí
        JSONObject locNode = json.getJSONObject("city");
        txtCity.setText(locNode.getString("name"));
        txtCountry.setText(locNode.getString("country"));
        JSONObject geoNode = locNode.getJSONObject("coord");
        txtLat.setText(geoNode.getString("lat"));
        txtLong.setText(geoNode.getString("lon"));
        txtId.setText(locNode.getString("id"));

        // Xóa dữ liệu cũ và hiển thị dữ liệu mới lên ListView
        forecastAdapter.clear();
        JSONArray timeNodes = json.getJSONArray("list");
        for (int i = 0; i < timeNodes.length(); i++) {
            JSONObject timeNode = timeNodes.getJSONObject(i);
            long timestamp = timeNode.getLong("dt") * 1000; // Chuyển đổi thành mili giây
            Date date = new Date(timestamp);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
            String dayOfWeek = dayFormat.format(date);
            String time = timeFormat.format(date);

            JSONObject tempNode = timeNode.getJSONObject("main");
            double tempValue = tempNode.getDouble("temp");
            String temp = String.format("%.1f", tempValue) + "°F";

            forecastAdapter.add(dayOfWeek + " - " + time + " - " + temp);
        }

        // Thông báo cho adapter là dữ liệu đã thay đổi
        forecastAdapter.notifyDataSetChanged();
    }
}
