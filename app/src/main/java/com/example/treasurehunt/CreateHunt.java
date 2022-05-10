package com.example.treasurehunt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateHunt extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

//    String huntName;
//    String initialClue;

    List<String> listLocations = new ArrayList<>();
    List<String> listClues = new ArrayList<>();

    String ID;

    ListView lView;
    ListAdapterClues lAdapter;

    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_hunt);

        EditText name = (EditText) findViewById(R.id.hNameEdit);
        EditText clue = (EditText) findViewById(R.id.iClueEdit);

        Intent intent = getIntent();

        ID = intent.getStringExtra("Hunt ID");
        String huntName = intent.getStringExtra("Hunt Name");
        String initialClue = intent.getStringExtra("Initial Clue");

        name.setText(huntName);
        clue.setText(initialClue);

        if (ID != null) {
            db.collection("Hunts").document(ID).collection("Locations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        int i = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            i++;
                            Log.d("TAG", document.getId() + " => " + document.getData());
                            String s = String.valueOf(i);
                            listLocations.add("Location " + s);
                            listClues.add(document.getString("Clue"));
                        }

//                        String[] arrLocations = listLocations.toArray(new String[listLocations.size()]);
//                        String[] arrClues = listClues.toArray(new String[listClues.size()]);

//                        createHunt(ID , listLocations, listClues);
                        createHunt(ID, listClues);

                    } else {
                        Log.d("Error", "DB Error");
                    }
                }
            });
        } else {
            Map<String, Object> hunt = new HashMap<>();
            //hunt.put("Creator", FirebaseAuth.getInstance().getCurrentUser().getUid());

            db.collection("Hunts").add(hunt).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    listLocations.add("Location 1");
                    listClues.add("New Clue");

                    Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());

//                    String[] arrLocations = listLocations.toArray(new String[listLocations.size()]);
//                    String[] arrClues = listClues.toArray(new String[listClues.size()]);

                    ID = documentReference.getId();

//                    createHunt(ID, listLocations, listClues);
                    createHunt(ID, listClues);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("TAG", "Error adding document", e);
                }
            });
        }
    }

//    private void createHunt(String huntID, List<String> locations, List<String> clues) {
    private void createHunt(String huntID, List<String> clues) {

        lView = (ListView) findViewById(R.id.clueList);

        Button discard = (Button) findViewById(R.id.discardChanges);
        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateHunt.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button save = (Button) findViewById(R.id.saveHunt);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText huntName = (EditText)findViewById(R.id.hNameEdit);
                String editName = huntName.getText().toString();

                EditText initialClue = (EditText)findViewById(R.id.iClueEdit);
                String initClue = initialClue.getText().toString();

                Map<String, Object> details = new HashMap<>();

                details.put("Creator", FirebaseAuth.getInstance().getCurrentUser().getUid());
                details.put("Hunt Name", editName);
                details.put("HuntID", huntID);
                details.put("Initial Clue", initClue);

                db.collection("Hunts").document(huntID).update(details);

                for (int i = 0; i < clues.size(); i++)  {
                    Map<String, Object> data = new HashMap<>();

                    String s = String.valueOf(i+1);

                    data.put("Name", "Location " + s);
                    data.put("Clue", lAdapter.clues.get(i));

                    db.collection("Hunts").document(huntID).collection("Locations").document("Location " + s).update(data).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            db.collection("Hunts").document(huntID).collection("Locations").document("Location " + s).set(data);
                        }
                    });
                }
            }
        });

        Button delete = (Button) findViewById(R.id.deleteHunt);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Hunts").document(huntID).delete();

                Intent intent = new Intent(CreateHunt.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button newLocation = (Button) findViewById(R.id.addLocation);
        newLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = listLocations.size() + 1;

                String s = String.valueOf(i);

                listLocations.add("Location " + i);
                listClues.add("New Clue");

                lView.setAdapter(lAdapter);

            }
        });

        lAdapter = new ListAdapterClues(CreateHunt.this, listLocations, clues, ID);
        lView.setAdapter(lAdapter);
    }


}