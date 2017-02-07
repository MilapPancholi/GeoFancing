package com.example.dev.test;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    LocationListener locationListener;
    LocationManager manager;
    Marker marker;

    double TargetLat=21.190256;
    double TargetLon=72.815416;
    double area=100.00;

    @Bind(R.id.txtarea)
    TextView txtarea;

    @Bind(R.id.txtcity)
    TextView txtcity;

    @Bind(R.id.button)
    Button btnAccept;

    private GoogleMap map;
    private SupportMapFragment mapView;

    @Bind(R.id.actualDistance)
    TextView actualDistance;

    @Bind(R.id.distance)
    EditText distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        manager=(LocationManager)getSystemService(LOCATION_SERVICE);
        InitMap();
        distance.setText(area+"");
        distance.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                try {
                    area = Double.parseDouble(distance.getText().toString());
                }
                catch (Exception ex)
                {
                    area=0;
                }
                return false;
            }
        });

        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("UPDATE","LOCATION CHANGED");
                double lat=location.getLatitude();
                double lon=location.getLongitude();
                double alt=location.getAltitude();
                double distance=distance(TargetLat,TargetLon,lat,lon);
                actualDistance.setText(distance+" Meter");
                if(distance<=area)
                {
                    btnAccept.setEnabled(true);
                }
                else
                {
                    btnAccept.setEnabled(false);
                }
                Log.e("LOCATION_DISTANCE",distance+"");
                Geocoder geocoder=new Geocoder(MainActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                    Address obj = addresses.get(0);
                    String add = obj.getAddressLine(0);
                    txtarea.setText(add + "," + obj.getAdminArea());
                    txtcity.setText(obj.getLocality());
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("LOCATION","CHANGE");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("LOCATION","ENABLE");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("LOCATION","DISABLE");
            }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission Granted","NO");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
        }
        else
        {
            Log.d("Permission Granted","YES");
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,0,locationListener);
        }
    }

    private void InitMap()
    {
        mapView = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        mapView.getMapAsync(this);
    }

    public boolean checkMobileData()
    {
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            mobileDataEnabled = (Boolean)method.invoke(cm);
            return mobileDataEnabled;
        } catch (Exception e) {
            return mobileDataEnabled;
        }
    }

    public void Accept_click(View view)
    {
        Toast.makeText(MainActivity.this,"You are in coverage area.",Toast.LENGTH_LONG).show();
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1000;
        return dist;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    Location locationcur;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
        }
        else
        {
            map.setMyLocationEnabled(true);
            try {
                Location locationtmp = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationcur=locationtmp;
                LatLng currentLocation = new LatLng(locationtmp.getLatitude(), locationtmp.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));
            }
            catch(Exception ex)
            {
                Toast.makeText(MainActivity.this,"Location service is not enabled.",Toast.LENGTH_SHORT).show();
            }
        }
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng arg0) {
                if (marker != null) {
                    marker.remove();
                }
                marker = map.addMarker(new MarkerOptions().position(new LatLng(arg0.latitude, arg0.longitude)).visible(true));
                TargetLat=arg0.latitude;
                TargetLon=arg0.longitude;
                btnAccept.setEnabled(false);
                if(locationcur!=null) {
                    double distance = distance(TargetLat, TargetLon, locationcur.getLatitude(), locationcur.getLongitude());
                    actualDistance.setText(distance + " Meter");
                    if(distance<=area)
                    {
                        btnAccept.setEnabled(true);
                    }
                }
            }
        });
        Toast.makeText(MainActivity.this,"Map is ready",Toast.LENGTH_SHORT).show();
    }
}
