package com.example.weatherreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.UiModeManager;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location current_location;
    double temp;
    double temp_feel;
    double temp_max;
    double temp_min;
    String wind;
    FusedLocationProviderClient fusedLocationProviderClient;
    Retrofit retrofit;
    String lat;
    String lon;
    double humid;
    double pressure;
    double t;
    String t_min;
    String t_max;
    String output;
    String t_feel;
    String description;
    ImageButton imageButton;
    TextView t_wind,t_h,t_p,t_temp,t_range,t_l,t_lat,t_d;
    TextToSpeech textToSpeech;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        t_wind=findViewById(R.id.wind);
        t_h=findViewById(R.id.humidity);
        t_p=findViewById(R.id.pressure);
        t_temp=findViewById(R.id.temp);
        t_d=findViewById(R.id.cloud);
        t_range=findViewById(R.id.range);
        imageButton=findViewById(R.id.speak);
        t_l=findViewById(R.id.local);
        t_lat=findViewById(R.id.latlng);
       UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=TextToSpeech.ERROR)
                    textToSpeech.setLanguage(Locale.getDefault());
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(output.length()!=0&&output!=null)
                {
                    textToSpeech.speak(output,TextToSpeech.QUEUE_FLUSH,null);
                }
                else
                {
                    textToSpeech.speak("Your Weather report is incomplete please check your internet connections",TextToSpeech.QUEUE_FLUSH,null);
                }
            }
        });

        findViewById(R.id.dark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);


                findViewById(R.id.dark).setVisibility(View.INVISIBLE);
                findViewById(R.id.light).setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                findViewById(R.id.dark).setVisibility(View.VISIBLE);
                findViewById(R.id.light).setVisibility(View.INVISIBLE);
            }
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        get_currentlocation();
    retrofit=new Retrofit.Builder().baseUrl("http://api.openweathermap.org/data/2.5/").addConverterFactory(GsonConverterFactory.create()).build();

    }

    private void get_currentlocation() {
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
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null)
                {

                    current_location=location;
                    request_location(current_location.getLatitude(),current_location.getLongitude());

                    request_weather(current_location.getLatitude(),current_location.getLongitude());

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }

    private void request_location(double latitude, double longitude) {
        String place;
        Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> addresses=geocoder.getFromLocation(latitude,longitude,1);
            place=addresses.get(0).getLocality();
            Log.d(TAG, "onSuccess: "+place);
            t_l.setText(place);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void request_weather(double latitude, double longitude) {
        JsonHolder jsonHolder=retrofit.create(JsonHolder.class);
        Call<Weather_Class> call=jsonHolder.get_weather(String.valueOf(latitude),String.valueOf(longitude),"c6ecfd655997ea0cd1d16f5b50c89d0f");
        call.enqueue(new Callback<Weather_Class>() {
            @Override
            public void onResponse(Call<Weather_Class> call, Response<Weather_Class> response) {
                Weather_Class weather_class =response.body();
                if(weather_class.getCoord().getLat()!=null)
                {
                    lat=weather_class.getCoord().getLat();
                    output+="Latitude:"+lat;
                    Log.d(TAG, "onResponse: "+lat);
                }

                if(weather_class.getCoord().getLon()!=null)
                {
                    lon=weather_class.getCoord().getLon();
                    output+="Longitude:"+lon;
                    Log.d(TAG, "onResponse: "+lon);
                }
                wind=weather_class.getWind().getSpeed();
                t_wind.setText(wind);
                t_lat.setText(lat+"\t"+"/"+"\t"+lon);
                temp=weather_class.getMain().getTemp()-273;
                output+="Temperature is:"+(int)temp+"degree celsius";
                t_temp.setText((int)(temp)+"\u2103");
                Log.d(TAG, "onResponse: "+temp);
                temp_feel=weather_class.getMain().getFeels_like()-273;
                output+="and it feels like"+(int)temp_feel+"degree celsius";
                Log.d(TAG, "onResponse: "+temp_feel);
                temp_max=weather_class.getMain().getTemp_max()-273;
                output+="Maximum temperature recorded is "+(int)temp_max+"degree celsius";
                Log.d(TAG, "onResponse: "+temp_max);

                temp_min=weather_class.getMain().getTemp_min()-273;
                output+="Minimum temperature recorded is "+ (int)temp_min+"degree celsius";
                Log.d(TAG, "onResponse: "+temp_min);
                t_range.setText((int)temp_max+"\u2103"+"/"+(int)temp_min+"\u2103");

                humid=weather_class.getMain().getHumidity();
                t_h.setText(humid+"%");
                output+="The area is \t"+humid+"humid";
                Log.d(TAG, "onResponse: "+humid);
                description=weather_class.getWeather().get(0).getDescription();
                output+="The sky is covered with"+description;
                t_d.setText(description);
                Log.d(TAG, "onResponse: "+description);

                pressure=weather_class.getMain().getPressure();
                output+="Pressure is recorded upto"+pressure+"now";
                Log.d(TAG, "onResponse: "+pressure);
                t_p.setText(pressure+"hPa");
                Log.d(TAG, "output: "+output);



            }

            @Override
            public void onFailure(Call<Weather_Class> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.getMessage());

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng user_latlng = new LatLng(current_location.getLatitude(), current_location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(user_latlng).title("You").draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user_latlng,10));
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart( Marker marker) {
                Log.d(TAG, "onMarkerDragStart: "+marker.getPosition().latitude);
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                Log.d(TAG, "onMarkerDrag: ");
            }

            @Override
            public void onMarkerDragEnd( Marker marker) {
                Log.d(TAG, "onMarkerDragEnd: "+marker.getPosition().longitude);
                request_weather(marker.getPosition().latitude,marker.getPosition().longitude);
                output="";
                request_location(marker.getPosition().latitude,marker.getPosition().longitude);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MapsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Permission Denied!!Please Access permission manually or Restart your App", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }
}