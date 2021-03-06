package com.hansung.android.week11_1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    GoogleMap mGoogleMap = null;
    final private int REQUEST_PERMISSIONS_FOR_LAST_KNOWN_LOCATION = 100;
    final private String TAG = "LocationService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!checkLocationPermissions()) {
            requestLocationPermissions(REQUEST_PERMISSIONS_FOR_LAST_KNOWN_LOCATION);
        } else
            getLastLocation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button findbtn = (Button) findViewById(R.id.find);
        findbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                getAddress();
            }
        });


    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        Task task = mFusedLocationClient.getLastLocation();
        task.addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    mLastLocation = location;
                    LatLng location2= new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location2,15));
                } else
                    Toast.makeText(getApplicationContext(), getString(R.string.no_location_detected), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    private boolean checkLocationPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions(int requestCode) {
        ActivityCompat.requestPermissions(
                MainActivity.this,            // MainActivity 액티비티의 객체 인스턴스를 나타냄
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},        // 요청할 권한 목록을 설정한 String 배열
                requestCode    // 사용자 정의 int 상수. 권한 요청 결과를 받을 때
        );
    }

    private void getAddress() {
        TextView addressTextView = (TextView) findViewById(R.id.result);
        try {
            Geocoder geocoder = new Geocoder(this, Locale.KOREA);

            EditText place = (EditText)findViewById(R.id.place);
            TextView result = (TextView)findViewById(R.id.result);
            List<Address> addresses = geocoder.getFromLocationName(place.getText().toString(),1);
            if(addresses.size()>0){
                Address bestResult = (Address) addresses.get(0);

                result.setText(String.format("[%s, %s]",
                        bestResult.getLatitude(),
                        bestResult.getLongitude()));
            }

            LatLng location2= new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location2,15));

            mGoogleMap.addMarker(
                    new MarkerOptions().
                            position(location2).
                            title(place.getText().toString()).
                            alpha(0.8f).
                            icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
            );
        } catch (IOException e) {
            Log.e(TAG, "Failed in using Geocoder",e);
            return;
        }

    }
}
