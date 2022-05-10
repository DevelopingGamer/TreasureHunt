package com.example.treasurehunt;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 101;

    LocationTrack locationTrack;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    List<String> huntIDs = new ArrayList<>();

    List<String> huntNamesCreator = new ArrayList<>();
    List<String> huntCluesCreator = new ArrayList<>();

    List<String> huntNamesUser = new ArrayList<>();
    List<String> huntCluesUser = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db.collection("Hunts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
           @Override
           public void onComplete(@NonNull Task<QuerySnapshot> task) {
               if (task.isSuccessful()) {
                   for (QueryDocumentSnapshot document : task.getResult()) {
                       //Log.d("TAG", document.getId() + " => " + document.getData());
                       huntIDs.add(document.getString("HuntID"));
                       huntNamesUser.add(document.getString("Hunt Name"));
                       huntCluesUser.add(document.getString("Initial Clue"));
                       if (document.get("Creator").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                           huntNamesCreator.add(document.getString("Hunt Name"));
                           huntCluesCreator.add(document.getString("Initial Clue"));
                       }
                   }

                   String [] IDs = huntIDs.toArray(new String[huntIDs.size()]);

                   String [] namesCreator = huntNamesCreator.toArray(new String[huntNamesCreator.size()]);
                   String [] initCluesCreator = huntCluesCreator.toArray(new String[huntCluesCreator.size()]);

                   String [] namesUser = huntNamesUser.toArray(new String[huntNamesUser.size()]);
                   String [] initCluesUser = huntCluesUser.toArray(new String[huntCluesUser.size()]);


                   Bundle bTop = new Bundle();
                   bTop.putStringArray("Top IDs", IDs);
                   bTop.putStringArray("Top Names", namesCreator);
                   bTop.putStringArray("Top Clues", initCluesCreator);

                   Bundle bBottom = new Bundle();
                   bBottom.putStringArray("Bottom IDs", IDs);
                   bBottom.putStringArray("Bottom Names", namesUser);
                   bBottom.putStringArray("Bottom Clues", initCluesUser);

                   FragmentManager fm = getSupportFragmentManager();

                   Fragment fragmentMainTop = new FragmentMainTop();
                   Fragment fragmentMainBottom = new FragmentMainBottom();

                   fragmentMainTop.setArguments(bTop);
                   fragmentMainBottom.setArguments(bBottom);

                   FragmentTransaction fragmentTransaction = fm.beginTransaction();
                   fragmentTransaction.add(R.id.frameLayoutTop, fragmentMainTop);
                   fragmentTransaction.add(R.id.frameLayoutBottom, fragmentMainBottom);
                   fragmentTransaction.commit();

               } else {
                   Log.w("TAG", "Error getting documents.", task.getException());
               }
           }
       });

        Button signOut = (Button) findViewById(R.id.signOutButton);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LandingPageActivity.class);
                startActivity(intent);
            }
        });

        Button create = (Button) findViewById(R.id.createHuntButton);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateHunt.class);
                startActivity(intent);
            }
        });

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }


    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }
}