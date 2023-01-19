package com.example.guidemeJM;

import
        androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.guidemeJM.databinding.ActivityDriverMapBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location nLastLocation;
    private FusedLocationProviderClient mFusedLocationClient ;
    LocationRequest nLocationRequest;
    private Button nLogout,nRequest , mSettings , nDone ;
    private LatLng pickupLocation ;
    private Boolean requestBol = false;
    private Marker pickupMarker ;
    private SupportMapFragment mapFragment ;


    private ActivityDriverMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        nDone = (Button) findViewById(R.id.done);
        nLogout = (Button) findViewById(R.id.logout);
        nRequest = (Button) findViewById(R.id.request);
        mSettings = (Button) findViewById(R.id.settings);

        nDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this , MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        nLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this , MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }

        });
        nRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestBol){
                    requestBol =false ;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverLocationRefListener);
                    if(driverFoundID != null ){
                        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("DriverFoundID");
                        driverRef.setValue(true);
                        driverFoundID = null ;
                    }
                    driverFound = false;
                    radius = 1 ;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId);

                    if(pickupMarker!=null){
                        pickupMarker.remove();
                    }
                    nRequest.setText("Find A Guide");
                }
                else {
                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(nLastLocation.getLatitude() , nLastLocation.getLongitude()));
                    pickupLocation = new LatLng(nLastLocation.getLatitude() ,nLastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Meeting Spot").icon(BitmapDescriptorFactory.fromResource(R.mipmap.luggage)));
                    nRequest.setText("Finding Your Guide");
                    getClosestDriver();

                }



            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (CustomerMapActivity.this , CustomerSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });
    }
    private int radius = 1;
    private Boolean driverFound = false ;
    private String driverFoundID;
    GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversavailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude ,pickupLocation.longitude), radius );
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound&& requestBol){
                    driverFound = true ;
                    driverFoundID = key ;

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("DriverFoundID");
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId",customerId);
                    driverRef.updateChildren(map);

                    getDriverLocation();
                    nRequest.setText("Looking For Driver Location");

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }

        });

    }
    private Marker nDriverMarker;
    private     DatabaseReference driverLocationRef;
    private ValueEventListener    driverLocationRefListener ;
    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Users").child("driversWorking").child("driverFoundId").child("l");
        driverLocationRefListener =driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&& requestBol) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    nRequest.setText("Driver Found !");

                    if(map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                        if (map.get(0) != null) {
                            locationLng = Double.parseDouble(map.get(0).toString());

                        }
                        LatLng driverLatLan = new LatLng(locationLat, locationLng);
                        if (nDriverMarker != null) {
                            nDriverMarker.remove();

                        }
                        Location loc1 = new Location("");
                        loc1.setLatitude(pickupLocation.latitude);
                        loc1.setLongitude(pickupLocation.longitude);
                        Location loc2 = new Location("");
                        loc2.setLatitude(driverLatLan.latitude);
                        loc2.setLongitude(driverLatLan.longitude);

                        float distance = loc1.distanceTo(loc2);
                        if (distance < 100) {
                            nRequest.setText("Your Guide Is Here : ");

                            nDone.setVisibility(View.VISIBLE);


                        } else {
                            nRequest.setText("The Distance Between You & Your Guide " + String.valueOf(distance));
                        }

                        nDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLan).title("Your Driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.tour_guide)));

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        nLocationRequest = new LocationRequest();
        nLocationRequest.setInterval(1000);
        nLocationRequest.setFastestInterval(1000);
        nLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }
    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                nLastLocation = location ;
                LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

            }

        }
    };
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Give Location Permission")
                        .setMessage("Give Location Permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

                            }
                        })
                        .create()
                        .show();
            }else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION )== PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(nLocationRequest , mLocationCallback , Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext() , "Please Provide Permission" , Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }




    @Override
    protected void onStop() {
        super.onStop();

    }
}