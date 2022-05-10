package com.example.treasurehunt;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapsActivityHunt extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {
    private GoogleMap mMap;
    protected LocationManager locMan;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //List<String> listLocations = new ArrayList<>();
    List<String> listClues = new ArrayList<>();
    List<LatLng> listLocs = new ArrayList<>();

    int locNum;
    double userLat = 53;
    double userLong = -1.19;
    LatLng position = new LatLng(userLat, userLong);
    LatLng newPos;
    double latitude;
    double longitude;
    String ID;
    String huntName;
    String initialClue;
    String newClue;

    Marker marker;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(this);

        Intent intent = getIntent();
        //ID = intent.getStringExtra("Hunt ID");

        marker = mMap.addMarker(new MarkerOptions().position(position).title("Marker").draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 13));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_hunt);

        locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationTrack locationTrack = new LocationTrack(MapsActivityHunt.this);

        Intent intent = getIntent();

        ID = intent.getStringExtra("Hunt ID");
        huntName = intent.getStringExtra("Hunt Name");
        initialClue = intent.getStringExtra("Initial Clue");

        if (locationTrack.canGetLocation()) {
            db.collection("Hunts").document(ID).collection("Locations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    locNum = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        locNum++;
                        String s = String.valueOf(locNum);
                        listClues.add(document.getString("Clue"));

                        GeoPoint gp = document.getGeoPoint("Coords");

                        latitude = gp.getLatitude();
                        longitude = gp.getLongitude();

                        LatLng location = new LatLng(latitude, longitude);

                        listLocs.add(location);
                    }
                }
            });

        } else {

            locationTrack.showSettingsAlert();
        }

        TextView name = (TextView) findViewById(R.id.huntMapName);
        name.setText(huntName);

        TextView clue = (TextView) findViewById(R.id.huntMapClueText);
        clue.setText(initialClue);

        Button back = (Button) findViewById(R.id.huntMapBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivityHunt.this, MainActivity.class);
                startActivity(intent);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapHunt);
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
        Intent i = getIntent();
        locNum = i.getIntExtra("Position", 0);

        newPos = marker.getPosition();
        boolean nextLoc  = false;
        LatLng loc = listLocs.get(locNum);

        Location userPin = new Location(LocationManager.GPS_PROVIDER);
        userPin.setLatitude(newPos.latitude);
        userPin.setLongitude(newPos.longitude);

        Location clueLoc = new Location(LocationManager.GPS_PROVIDER);
        clueLoc.setLatitude(loc.latitude);
        clueLoc.setLongitude(loc.longitude);


//        float[] results = new float[1];
        double distanceInMeters = clueLoc.distanceTo(userPin);
        boolean isWithin300m = false; //= distanceInMeters < 200;

        if (distanceInMeters < 300) {isWithin300m = true;}


        if (nextLoc == false && isWithin300m) {
                locNum++;

            if (listClues.get(locNum) != null) {
                Intent intent = new Intent(MapsActivityHunt.this, MapsActivityHunt.class);
                intent.putExtra("Hunt ID", ID);
                intent.putExtra("Hunt Name", huntName);
                intent.putExtra("Initial Clue", listClues.get(locNum));
                intent.putExtra("Position", locNum);

                startActivity(intent);
            } else {
                Intent intent = new Intent(MapsActivityHunt.this, WonActivity.class);
                startActivity(intent);
            }
        }
    }
}