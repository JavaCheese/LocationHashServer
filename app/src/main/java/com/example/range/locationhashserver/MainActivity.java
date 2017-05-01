package com.example.range.locationhashserver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.codec.DecoderException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tVLatitude;
    TextView tVLongitude;
    TextView tvGson;
    private LocationManager locationManager;
    TempInfo tempInfo;

    String url = "http://openlab.hopto.org:3000/coords";
    RequestQueue queue;
    List<LatLng> routePoints;
    private final static String TAG = "COORDS - DEVICE_ID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(getApplicationContext());
        routePoints = new ArrayList<>();

        tVLatitude = (TextView) findViewById(R.id.textViewLatitude);
        tVLongitude = (TextView) findViewById(R.id.textViewLongitude);
        tvGson = (TextView) findViewById(R.id.textViewGson);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tVLatitude.setText("Status: " + String.valueOf(status));
            }
        }
    };


    /*
        Get location, shows on the screen, write to JSON, store in file
     */
    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            tVLatitude.setText("Latitude: "+location.getLatitude());
            tVLongitude.setText("Longitude: "+location.getLongitude());
            String deviceId = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            tempInfo = new TempInfo(deviceId, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            String tempGson = gson.toJson(tempInfo);
            writeToFile(tempGson);
            try{
                String hash = MyCryptography.getHash("locInfo.txt");
                tvGson.setText("Json information: " + tempGson + "Hash: " + hash);
                makeRequest(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),hash);
            }
            catch (DecoderException e){
                e.printStackTrace();
            }
        }
    }

    private void makeRequest(final String latitude, final String longitude,String hash) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("device_id", Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID));
            jsonObject.put("coords", latitude + "," + longitude);
            jsonObject.put("is_hash", "true");
            jsonObject.put("hashing_algorithm", "stb-77");
            jsonObject.put("hash", hash);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        final String requestBody = jsonObject.toString();

        StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Upload OK");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error occurred");
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Log.d(TAG, uee.getMessage());
                    return null;
                }
            }
        };
        queue.add(sr);
    }

    private void writeToFile(String tempGson){
        try{
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(openFileOutput("locInfo.txt",MODE_PRIVATE)));
            bw.write(tempGson);
            bw.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
