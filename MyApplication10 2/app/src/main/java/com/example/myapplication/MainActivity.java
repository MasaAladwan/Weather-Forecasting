package com.example.myapplication;

import android.util.Log;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.HorizontalScrollView;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Scanner;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Scanner;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Spinner locationSpinner;
    private EditText dateEditText;
    private TextView avgTempTextView;
    private LinearLayout weatherDetailsLayout; // LinearLayout containing weather details
    private TextView avgPresTextView;
    private TextView avgWdirTextView;
    private TextView avgWspdTextView;
    private ImageView weatherImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationSpinner = findViewById(R.id.locationSpinner);
        dateEditText = findViewById(R.id.dateEditText);
        avgTempTextView = findViewById(R.id.avgTempTextView);
        weatherDetailsLayout = findViewById(R.id.weatherDetailsLayout); // Initialize LinearLayout
        avgPresTextView = findViewById(R.id.avgPresTextView);
        avgWdirTextView = findViewById(R.id.avgWdirTextView);
        avgWspdTextView = findViewById(R.id.avgWspdTextView);
        weatherImageView = findViewById(R.id.weatherImageView);
        Button buttonSendRequest = findViewById(R.id.buttonSendRequest);



        // Hide weather details layout initially
        weatherDetailsLayout.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cities_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle city selection here if needed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        buttonSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = locationSpinner.getSelectedItem().toString();
                String date = dateEditText.getText().toString();
                Log.d(TAG, "Button clicked, starting AsyncTask");
                new PostRequestTask(city, date, avgTempTextView, weatherDetailsLayout, avgPresTextView, avgWdirTextView, avgWspdTextView, weatherImageView).execute();
            }
        });
    }

    private class PostRequestTask extends AsyncTask<Void, Void, JSONObject> {
        private String city;
        private String date;
        private TextView avgTempTextView;
        private LinearLayout weatherDetailsLayout;
        private TextView avgPresTextView;
        private TextView avgWdirTextView;
        private TextView avgWspdTextView;
        private ImageView weatherImageView;



        public PostRequestTask(String city, String date, TextView avgTempTextView, LinearLayout weatherDetailsLayout, TextView avgPresTextView, TextView avgWdirTextView, TextView avgWspdTextView, ImageView weatherImageView) {
            this.city = city;
            this.date = date;
            this.avgTempTextView = avgTempTextView;
            this.weatherDetailsLayout = weatherDetailsLayout;
            this.avgPresTextView = avgPresTextView;
            this.avgWdirTextView = avgWdirTextView;
            this.avgWspdTextView = avgWspdTextView;
            this.weatherImageView = weatherImageView;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                Log.d(TAG, "AsyncTask started, sending request to server");
                URL url = new URL("http://192.168.1.22:8000/predict/");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("city", city);
                jsonObject.put("date", date);
                jsonObject.put("add", "test");

                String jsonInputString = jsonObject.toString();

                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                InputStream inputStream = urlConnection.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                StringBuilder responseBuilder = new StringBuilder();
                while (scanner.hasNext()) {
                    responseBuilder.append(scanner.nextLine());
                }
                String jsonResponse = responseBuilder.toString();
                return new JSONObject(jsonResponse);

            } catch (Exception e) {
                Log.e(TAG, "Error during network request", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    double avgTemp = response.getDouble("avg_temp");
                    avgTempTextView.setText(avgTemp + "°C");

                    // Show weather details layout
                    weatherDetailsLayout.setVisibility(View.VISIBLE);

                    // Show weather image
                    weatherImageView.setVisibility(View.VISIBLE);

                    // Update other weather details
                    double avgPres = response.getDouble("avg_pres");
                    double avgWdir = response.getDouble("avg_wdir");
                    double avgWspd = response.getDouble("avg_wspd");

                    avgPresTextView.setText("Pressure:\n" + avgPres);
                    avgWdirTextView.setText("Wind \nDirection:\n" + avgWdir);
                    avgWspdTextView.setText("Wind \nSpeed:\n" + avgWspd);
                    LinearLayout daycontainer = findViewById(R.id.daycontainer);
                    LinearLayout text = findViewById(R.id.TODAY10DAYS);

                    // Clear any previous weather details for next 10 days
                    daycontainer.removeAllViews();

                    // Add weather details for the next 10 days
                    JSONObject howManyDays = response.getJSONObject("howManyDays");
                    Iterator<String> keys = howManyDays.keys();

                    HorizontalScrollView horizontalScrollView = new HorizontalScrollView(MainActivity.this);
                    LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    horizontalScrollView.setLayoutParams(scrollViewParams);

// Create a new LinearLayout to hold the TextViews horizontally
                    LinearLayout horizontalLinearLayout = new LinearLayout(MainActivity.this);
                    LinearLayout.LayoutParams horizontalLayoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    horizontalLinearLayout.setLayoutParams(horizontalLayoutParams);
                    horizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

// Add TextViews to the horizontal LinearLayout
                    while (keys.hasNext()) {
                        String date = keys.next();
                        JSONArray dayDetails = howManyDays.getJSONArray(date);

                        double dayTemp = dayDetails.getDouble(0);
                        double dayPres = dayDetails.getDouble(1);
                        double dayWdir = dayDetails.getDouble(2);
                        double dayWspd = dayDetails.getDouble(3);

                        // Create a new TextView for date and add it to daycontainer

                        daycontainer.setVisibility(View.VISIBLE);
                        text.setVisibility(View.VISIBLE);


                        LinearLayout verticalLayout = new LinearLayout(MainActivity.this);
                        LinearLayout.LayoutParams verticalLayoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        verticalLayout.setLayoutParams(verticalLayoutParams);
                        verticalLayout.setOrientation(LinearLayout.VERTICAL);
                        verticalLayout.setPadding(16,16,16,16);
                        verticalLayoutParams.setMargins(16, 16, 16, 16);
                        verticalLayout.setBackgroundResource(R.drawable.button); // Replace R.drawable.vertical_layout_background with your background resource



                        // Create a new TextView for weather details and add it to daycontainer
                        TextView dateTextView = new TextView(MainActivity.this);
                        dateTextView.setText(date);
                        dateTextView.setTextColor(getResources().getColor(R.color.text)); // Set text color
                        dateTextView.setTextSize(16); // Set text size
                        dateTextView.setPadding(20, 8, 20, 8); // Set padding
                        verticalLayout.addView(dateTextView);



                      // TEMPERATURE
                        //Layout done
                        LinearLayout tempLayout = new LinearLayout(MainActivity.this);
                        tempLayout.setOrientation(LinearLayout.HORIZONTAL);
                        tempLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                        tempLayout.setGravity(Gravity.CENTER_VERTICAL);
                        tempLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);



                        // Add temperature TextView
                        TextView tempTextView = new TextView(MainActivity.this);
                        tempTextView.setText(dayTemp + "°C");
                        tempTextView.setTextColor(getResources().getColor(R.color.text));
                        tempTextView.setTextSize(16); // Set text size
                        tempTextView.setPadding(20, 8, 20, 8);

                        // Add temperature image
                        ImageView tempIcon = new ImageView(MainActivity.this);
                        tempIcon.setImageResource(R.drawable.temperature_566675);
                        LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(
                                60, // Set width to WRAP_CONTENT or custom width in pixels
                                60// Set height to WRAP_CONTENT or custom height in pixels
                        );

                        tempIcon.setLayoutParams(iconLayoutParams);

                        tempLayout.addView(tempIcon);
                        tempLayout.addView(tempTextView);
                        verticalLayout.addView(tempLayout);

                        // PRESSURE
                        //Layout
                        LinearLayout presLayout = new LinearLayout(MainActivity.this);
                        presLayout.setOrientation(LinearLayout.HORIZONTAL);
                        presLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                        presLayout.setGravity(Gravity.CENTER_VERTICAL);
                        presLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);



                        // Add Pressure TextView
                        TextView presTextView = new TextView(MainActivity.this);
                        presTextView.setText(String.valueOf(dayPres)); // Convert double to String
                        presTextView.setTextColor(getResources().getColor(R.color.text));
                        presTextView.setTextSize(16); // Set text size
                        presTextView.setPadding(20, 8, 20, 8);

                        // Add Pressure image
                        ImageView presIcon = new ImageView(MainActivity.this);
                        presIcon.setImageResource(R.drawable.pressure);
                        LinearLayout.LayoutParams presLayoutParams = new LinearLayout.LayoutParams(
                                60, // Set width to WRAP_CONTENT or custom width in pixels
                                60// Set height to WRAP_CONTENT or custom height in pixels
                        );
                        presIcon.setLayoutParams(presLayoutParams);
                        presLayout.addView(presIcon);
                        presLayout.addView(presTextView);
                        verticalLayout.addView(presLayout);


                        // WIND direction
                        LinearLayout winddirLayout = new LinearLayout(MainActivity.this);
                        winddirLayout.setOrientation(LinearLayout.HORIZONTAL);
                        winddirLayout.setGravity(Gravity.CENTER_VERTICAL);
                        winddirLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                        winddirLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);



                        // Add winddir TextView
                        TextView WiddirTextView = new TextView(MainActivity.this);
                        WiddirTextView.setText(String.valueOf(dayWdir)); // Convert double to String
                        WiddirTextView.setTextColor(getResources().getColor(R.color.text)); // Set text color
                        WiddirTextView.setTextSize(16); // Set text size
                        WiddirTextView.setPadding(20, 8, 20, 8); // Set padding

                        // Add winddir image
                        ImageView winddirIcon = new ImageView(MainActivity.this);
                        winddirIcon.setImageResource(R.drawable.wind);
                        LinearLayout.LayoutParams windLayoutParams = new LinearLayout.LayoutParams(
                                60, // Set width to WRAP_CONTENT or custom width in pixels
                                60// Set height to WRAP_CONTENT or custom height in pixels
                        );

                        winddirIcon.setLayoutParams(windLayoutParams);

                        winddirLayout.addView(winddirIcon);
                        winddirLayout.addView(WiddirTextView);
                        verticalLayout.addView(winddirLayout);



                        // WIND Speed
                        //LAYOUT
                        LinearLayout windspeed = new LinearLayout(MainActivity.this);
                        windspeed.setOrientation(LinearLayout.HORIZONTAL);
                        windspeed.setGravity(Gravity.CENTER_VERTICAL);
                        windspeed.setGravity(Gravity.CENTER_HORIZONTAL);
                        windspeed.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

                        //textview
                        TextView WidspdTextView = new TextView(MainActivity.this);
                        WidspdTextView.setText(String.valueOf(dayWspd)); // Convert double to String
                        WidspdTextView.setTextColor(getResources().getColor(R.color.text)); // Set text color
                        WidspdTextView.setTextSize(16); // Set text size
                        WidspdTextView.setPadding(20, 8, 20, 8);

                        //image
                        ImageView windspeedicon = new ImageView(MainActivity.this);
                        windspeedicon.setImageResource(R.drawable.windy);
                        LinearLayout.LayoutParams windspeedParams = new LinearLayout.LayoutParams(
                                60,
                                60
                        );
                        windspeedParams.gravity = Gravity.START;
                        windspeedicon.setLayoutParams(windspeedParams);
                        windspeed.addView(windspeedicon);
                        windspeed.addView(WidspdTextView);
                        verticalLayout.addView(windspeed);


                        // Add the vertical LinearLayout to the horizontalLinearLayout
                        horizontalLinearLayout.addView(verticalLayout);
                    }

                    horizontalScrollView.addView(horizontalLinearLayout); // Add horizontal LinearLayout to HorizontalScrollView
                    daycontainer.addView(horizontalScrollView);


                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                }
            } else {
                Log.e(TAG, "Response is null");
            }
        }
    }
}
