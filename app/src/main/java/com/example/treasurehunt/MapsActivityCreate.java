package com.example.treasurehunt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class MapsActivityCreate extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    LocationTrack locationTrack;

    private GoogleMap mMap;
    //private ActivityMapsBinding binding;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String huntID;
    int pos;
    int posPlus;
    LatLng location;
    double latitude;
    double longitude;
    String s;
    LatLng newPos;

    Marker marker;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Log.d("TAG", "No Perms");
        }

        locationTrack = new LocationTrack(MapsActivityCreate.this);

        Intent intent = getIntent();
        huntID = intent.getStringExtra("Hunt ID");
        pos = intent.getIntExtra("Location", 0);
        posPlus = pos + 1;

        s = String.valueOf(posPlus);

        if (locationTrack.canGetLocation()) {
            db.collection("Hunts").document(huntID).collection("Locations").document("Location " + posPlus).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();

                    if (document.get("Coords") != null) {
                        GeoPoint gp = document.getGeoPoint("Coords");

                        latitude = gp.getLatitude();
                        longitude = gp.getLongitude();

                        location = new LatLng(latitude, longitude);
                    } else {
                        latitude = locationTrack.getLatitude();
                        longitude = locationTrack.getLongitude();

                        location = new LatLng(latitude, longitude);
                    }
                    marker = mMap.addMarker(new MarkerOptions().position(location).title("Marker").draggable(true));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                }

            });

        } else {

            locationTrack.showSettingsAlert();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_maps_create);

        Button back = (Button) findViewById(R.id.createMapBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng newPos = marker.getPosition();
                Map<String, Object> loc = new HashMap<>();

                double lat = newPos.latitude;
                double lon = newPos.longitude;

                GeoPoint geoPoint = new GeoPoint(lat, lon);

                loc.put("Coords", geoPoint);

                db.collection("Hunts").document(huntID).collection("Locations").document("Location " + s).update(loc);

                onBackPressed();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapCreate);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        newPos = marker.getPosition();
    }
}
