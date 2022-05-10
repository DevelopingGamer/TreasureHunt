package com.example.treasurehunt;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ListAdapterClues extends BaseAdapter {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Context context;
    private List<String> locations;
    public List<String> clues;
    String huntID;
    String huntName;
    String initClue;


    public ListAdapterClues(Context context, List<String> locations, List<String> clues, String ID) {
        this.context = context;
        this.locations = locations;
        this.clues = clues;
        this.huntID = ID;
    }

    @Override
    public int getCount() {
        return locations.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {
        ViewHolder viewHolder;
        final View result;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.hunt_list_clues, parent, false);
            viewHolder.locationNumber = (TextView) convertView.findViewById(R.id.locationNo);
            viewHolder.clue = (EditText) convertView.findViewById(R.id.clue);
            viewHolder.mapButton = (Button) convertView.findViewById(R.id.mapButton);
            viewHolder.saveButton = (Button) convertView.findViewById(R.id.saveLocation);
            viewHolder.removeButton = (Button) convertView.findViewById(R.id.removeLocation);
            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        viewHolder.locationNumber.setText(locations.get(position));

        if (huntID != null) {
            viewHolder.clue.setText(clues.get(position));
        }

        viewHolder.mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Context context = CreateHunt.context;
                Intent intent = new Intent(context, MapsActivityCreate.class);
                intent.putExtra("Location", position);
                intent.putExtra("Hunt ID", huntID);

                context.startActivity(intent);
            }
        });

        viewHolder.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clues.set(position, viewHolder.clue.getText().toString());

            }
        });

        viewHolder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //location = position;

                String s = String.valueOf(position + 1);

                db.collection("Hunts").document(huntID).collection("Locations").document("Location " + s).delete();
                db.collection("Hunts").document(huntID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        huntName = document.getString("Hunt Name");
                        initClue = document.getString("Initial Clue");

                        Intent i = new Intent(context, CreateHunt.class);

                        i.putExtra("Hunt ID", huntID);
                        i.putExtra("Hunt Name", huntName);
                        i.putExtra("Initial Clue", initClue);

                        context.startActivity(i);
                    }
                });
            }
        });


        return convertView;
    }

    private static class ViewHolder {
        TextView locationNumber;
        EditText clue;
        Button mapButton;
        Button saveButton;
        Button removeButton;
    }

}
