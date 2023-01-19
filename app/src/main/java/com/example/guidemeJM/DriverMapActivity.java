package com.example.guidemeJM;

import androidx.annotation.NonNull;
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
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
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
import com.example.guidemeJM.databinding.ActivityDriverMapBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment  mapFragment ;
    Location nLastLocation;
    LocationRequest nLocationRequest;
    private Button nLogout , mSettings;
    private String customerId = "";
    private Boolean isLoggingOut = false;
    private ActivityDriverMapBinding binding;
    private LinearLayout mCustomerInfo;
    private ImageView mCustomerProfileImage;
    private TextView mCustomerName , mCustomerPhone ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);








        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);

        mCustomerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);
        mCustomerName = (TextView) findViewById(R.id.customerName);
        mCustomerPhone = (TextView) findViewById(R.id.customerPhone);
        mSettings = (Button)findViewById(R.id.settings);
        nLogout = (Button) findViewById(R.id.logout);
        nLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoggingOut =true;
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapActivity.this , MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (DriverMapActivity.this , DriverSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });
        getAssignedCustomer();

    }
    private void getAssignedCustomer() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerInfo();
                }else{
                    customerId = "";
                if(pickupMarker!= null ){
                    pickupMarker.remove();
                }
                if(AssignedCustomerPickupLocationRefListener!=null){
                    AssignedCustomerPickupLocationRef.removeEventListener(AssignedCustomerPickupLocationRefListener);

                }
                    mCustomerInfo.setVisibility(View.GONE);
                    mCustomerName.setText("");
                    mCustomerPhone.setText("");
                    mCustomerProfileImage.setImageResource(R.mipmap.icon);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

            Marker pickupMarker;
            DatabaseReference AssignedCustomerPickupLocationRef;
            private ValueEventListener AssignedCustomerPickupLocationRefListener ;
            private void getAssignedCustomerPickupLocation ()
            {
                AssignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("Users").child("customerRequest").child("customerId").child("l");
                AssignedCustomerPickupLocationRefListener = AssignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
                    @Override

                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()&&customerId.equals("")){
                            List<Object> map =(List<Object>) dataSnapshot.getValue();
                            double locationLat = 0;
                            double locationLng = 0;

                                if(map.get(0) != null) {
                                locationLat = Double.parseDouble(map.get(0).toString());
                                if (map.get(0) != null) {
                                    locationLng = Double.parseDouble(map.get(1).toString());

                                }

                                LatLng driverLatLan = new LatLng(locationLat, locationLng);
                                pickupMarker = mMap.addMarker(new MarkerOptions().position(driverLatLan).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.tour_guide)));
                            }

                        }
                    }
                        @Override
                    public void onCancelled(DatabaseError databaseError){
                    }

                });

            }

    private void getAssignedCustomerInfo(){
                    mCustomerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);

        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String , Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("name")!= null){

                        mCustomerName.setText(map.get("name").toString());
                    }

                    if(map.get("phone")!= null){

                        mCustomerPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("phone")!= null){

                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mCustomerProfileImage);

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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED);
        }else{
            checkLocationpermission();
        }
    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                nLastLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversavailable");
                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
                GeoFire geoFireAvailable = new GeoFire(refAvailable);
                GeoFire geoFireWorking = new GeoFire(refWorking);


                switch (customerId){
                    case "":
                        geoFireWorking.removeLocation(userId);
                        geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

                        break;
                    default:
                        geoFireAvailable.removeLocation(userId);
                        geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

                        break;

                }

            }

        }
    };

private void checkLocationpermission(){
    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED);{
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Please Allow Us To Use Location")
                    .setMessage("Allow Us To Use Location")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(DriverMapActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                        }
                    })
                    .create()
                    .show();
        }
        else{
            ActivityCompat.requestPermissions(DriverMapActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,1);
        }
    }
}
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(nLocationRequest , mLocationCallback , Looper.myLooper());
                        mMap.setMyLocationEnabled((true));
                    }
                }
                else{
                    Toast.makeText(getApplicationContext() , "Please Provide Permission" , Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }



        private void disconnectDriver(){
            super.onStop();
            if(mFusedLocationClient != null){
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversavailable");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(userId);
        }


    @Override
    protected void onStop() {
        super.onStop();
                if(!isLoggingOut){
                    disconnectDriver();
                }

    }
}