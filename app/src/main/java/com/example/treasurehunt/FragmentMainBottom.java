package com.example.treasurehunt;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

public class FragmentMainBottom extends Fragment {

    View view;

    GridView gView;
    GridAdapterBottomHome gAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String [] huntNames = getArguments().getStringArray("Bottom Names");
        String [] huntIDs = getArguments().getStringArray("Bottom IDs");
        String [] initClues = getArguments().getStringArray("Bottom Clues");

        view = inflater.inflate(R.layout.fragment_main_top, container, false);
        gView = (GridView) view.findViewById(R.id.huntsGridTop);
        gAdapter = new GridAdapterBottomHome(getContext(), huntNames);
        gView.setAdapter(gAdapter);

        gView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), MapsActivityHunt.class);

                String ID = huntIDs[position].trim();
                String huntName = huntNames[position].trim();
                String initClue = initClues[position].trim();

                intent.putExtra("Hunt ID", ID);
                intent.putExtra("Hunt Name", huntName);
                intent.putExtra("Initial Clue", initClue);

                startActivity(intent);
            }

        });

        return view;
    }

}