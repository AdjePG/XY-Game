package com.example.xyclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout registerLayout;
    private ConstraintLayout playAreaLayout;

    private EditText groupNameET;
    private EditText gameCodeET;

    private TextView roundNumberTV;
    private TextView scoreTV;

    private Button startBTN;
    private Button xBTN;
    private Button yBTN;

    private FirebaseDatabase database;
    private DatabaseReference dbReference;

    private Map<String, Object> updates = new HashMap<>();
    private String code = "";
    private String group = "";
    private String score = "";

    private boolean canStart;
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupVariables();

        database = FirebaseDatabase.getInstance();
        dbReference = database.getReference("");
    }

    private void setupVariables () {
        registerLayout = (ConstraintLayout) findViewById(R.id.registerLayout);
        playAreaLayout = (ConstraintLayout) findViewById(R.id.playAreaLayout);

        groupNameET = (EditText) findViewById(R.id.groupNameET);
        gameCodeET = (EditText) findViewById(R.id.gameCodeET);

        roundNumberTV = (TextView) findViewById(R.id.roundNumberTV);
        scoreTV = (TextView) findViewById(R.id.scoreTV);

        startBTN = (Button) findViewById(R.id.startBTN);
        xBTN = (Button) findViewById(R.id.xBTN);
        yBTN = (Button) findViewById(R.id.yBTN);

        counter = 0;
        score = "";
    }

    public void startGame(View view) {
        if (gameCodeET.getText().toString().equals("")) {
            Toast.makeText(MainActivity.this, "Has d'afegir un codi de sala", Toast.LENGTH_SHORT).show();
            return;
        }

        if (groupNameET.getText().toString().equals("")) {
            Toast.makeText(MainActivity.this, "Has d'afegir un nom de grup", Toast.LENGTH_SHORT).show();
            return;
        }

        code = gameCodeET.getText().toString();
        group = groupNameET.getText().toString();

        canStart = true;

        dbReference.child(code).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    if (!task.getResult().exists()) {
                        Toast.makeText(MainActivity.this, "No existeix el codi de la sala", Toast.LENGTH_SHORT).show();
                        canStart = false;
                    } else {
                        dbReference.child(code).child("hasStarted").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    if (task.getResult().getValue().toString().equals("true")) {
                                        Toast.makeText(MainActivity.this, "La partida ja ha començat", Toast.LENGTH_SHORT).show();
                                        canStart = false;
                                    } else  {
                                        setGame();
                                        changeMode(false,false);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });

        if (!canStart) {
            return;
        }

        dbReference.child(code).child("voteStarted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("true")) {
                    if (counter < 10) {
                        changeMode(false, true);
                        counter++;
                        roundNumberTV.setText("- - " + counter + " - -");
                        scoreTV.setText("Selecciona una de les opcions");
                    }
                } else {
                    if (counter == 10) {
                        changeMode(true, false);
                        counter = 0;
                        roundNumberTV.setText("");

                        dbReference.child(code).child(group).child("Puntuacio").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    score = task.getResult().getValue().toString();
                                }
                            }
                        });

                        scoreTV.setText("Puntuacio: " + score);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //
            }
        });
    }

    public void xButtonSelected(View view) { sendSelectedOption("X"); }

    public void yButtonSelected(View view) { sendSelectedOption("Y"); }

    private void sendSelectedOption(String buttonValue) {
        dbReference = database.getReference(code +  "/" + group + "/Iteracio" + counter);
        dbReference.setValue(buttonValue);
        changeMode(false,false);
    }

    private void setGame() {
        updates.put(code +  "/nGrups", ServerValue.increment(1));
        dbReference = database.getReference("");
        dbReference.updateChildren(updates);

        dbReference = database.getReference(code + "/" + groupNameET.getText().toString() + "/Puntuacio");
        dbReference.setValue(0);
    }

    private void changeMode(boolean enableTop, boolean enableBottom) {
        if (enableTop) {
            registerLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00B7FF")));
            startBTN.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#006ADC")));

            gameCodeET.setEnabled(true);
            groupNameET.setEnabled(true);
            startBTN.setEnabled(true);
        } else {
            registerLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B5B4B4")));
            startBTN.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6E6E6E")));

            gameCodeET.setEnabled(false);
            groupNameET.setEnabled(false);
            startBTN.setEnabled(false);
        }

        if (enableBottom) {
            playAreaLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00B7FF")));
            xBTN.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#006ADC")));
            yBTN.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#006ADC")));

            xBTN.setEnabled(true);
            yBTN.setEnabled(true);
        } else {
            playAreaLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B5B4B4")));
            xBTN.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6E6E6E")));
            yBTN.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6E6E6E")));

            xBTN.setEnabled(false);
            yBTN.setEnabled(false);
        }
    }
}