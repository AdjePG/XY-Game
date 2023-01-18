package com.example.xyserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout constrainUp;
    ConstraintLayout constrainDown;

    TextView GroupName1;
    TextView GroupName2;

    Button buttonStart;

    TextView Code;
    TextView TextCode;
    //ListView IterationList;

    FirebaseDatabase database;

    String codi;

    int nGrups = 0;
    boolean hasStarted = false;
    boolean voteStarted = false;

    int votacions = -1;
    int ronda = 0;

    String votacioGrup1 = "";
    String votacioGrup2 = "";

    int puntuacioGrup1 = 0;
    int puntuacioGrup2 = 0;

    Map<String, Object> updates = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();

        setContentView(R.layout.activity_main);

        constrainDown = (ConstraintLayout) findViewById(R.id.constraintLayoutDown);
        constrainUp = (ConstraintLayout) findViewById(R.id.constraintLayoutUp);
        GroupName1 = (TextView) findViewById(R.id.nomGrup1);
        GroupName2 = (TextView) findViewById(R.id.nomGrup2);
        buttonStart= (Button) findViewById(R.id.buttonStart);
        Code = (TextView) findViewById(R.id.code);
        TextCode = (TextView) findViewById(R.id.textcodi);
        //IterationList = (ListView) findViewById(R.id.llistaIteracions);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbReference = database.getReference("");

        Random rand = new Random(); //instance of random class
        int int_random = rand.nextInt(99999);
        codi = String.valueOf(int_random);
        codi = padLeftZeros(codi, 5);
        Code.setText(codi);


        updates.put(codi+"/nGrups", nGrups);
        updates.put(codi+"/hasStarted", false);
        updates.put(codi+"/voteStarted", false);
        dbReference.updateChildren(updates);


        dbReference.child(codi).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("MiTag", String.valueOf(dataSnapshot.getValue()));
                for (DataSnapshot keyNode : dataSnapshot.getChildren()){
                    dbReference.child(codi).child("nGrups").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                if(keyNode.getKey().equals("nGrups")){
                                    nGrups = Integer.parseInt(task.getResult().getValue().toString());
                                }

                                if(!keyNode.getKey().equals("nGrups") && !keyNode.getKey().equals("hasStarted") && !keyNode.getKey().equals("voteStarted")){
                                    if(nGrups == 1){
                                        GroupName1.setText(keyNode.getKey());
                                    }else if(nGrups == 2 && !keyNode.getKey().equals(GroupName1.getText())){
                                        GroupName2.setText(keyNode.getKey());
                                    }
                                }
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });


        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("MiTag", String.valueOf(dataSnapshot.getValue()));
                if (hasStarted && voteStarted) {
                    if (nGrups == 2) {
                        votacions += 1;
                    }
                    if(votacions == 2){
                        votacioGrup1 = "";
                        votacioGrup2 = "";
                        for (DataSnapshot keyNode : dataSnapshot.child(codi).getChildren()) {
                            dbReference.child(codi).child(keyNode.getKey()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    String keyName = task.getResult().getKey();
                                    if (!task.isSuccessful()) {
                                        Log.e("firebase", "Error getting data", task.getException());
                                    } else if(!keyName.equals("nGrups") && !keyName.equals("hasStarted")  && !keyName.equals("voteStarted")){
                                        Log.d("lilil", "Ronda: "+ronda + "      "+codi+"/" + GroupName1.getText().toString() + "/Puntuacio");

                                        if(GroupName1.getText().toString().equals(keyName)){
                                            votacioGrup1 = task.getResult().child("Iteracio" + ronda).getValue().toString();
                                        }else if(GroupName2.getText().toString().equals(keyName)){
                                            votacioGrup2 = task.getResult().child("Iteracio" + ronda).getValue().toString();
                                        }


                                        if(!votacioGrup1.equals("") && !votacioGrup2.equals("")){
                                            if(votacioGrup1.equals("X") && votacioGrup2.equals("X")){
                                                puntuacioGrup1 = +5;
                                                puntuacioGrup2 = +5;
                                            }else if(votacioGrup1.equals("Y") && votacioGrup2.equals("Y")){
                                                puntuacioGrup1 = -5;
                                                puntuacioGrup2 = -5;
                                            }else if(votacioGrup1.equals("X") && votacioGrup2.equals("Y")){
                                                puntuacioGrup1 = -5;
                                                puntuacioGrup2 = +5;
                                            }else if(votacioGrup1.equals("Y") && votacioGrup2.equals("X")){
                                                puntuacioGrup1 = +5;
                                                puntuacioGrup2 = -5;
                                            }
                                        }
                                        updates.put(codi+"/" + GroupName1.getText().toString() + "/Puntuacio", ServerValue.increment(puntuacioGrup1));
                                        updates.put(codi+"/" + GroupName2.getText().toString() + "/Puntuacio", ServerValue.increment(puntuacioGrup2));
                                    }
                                }
                            });
                        }
                        try {
                            Thread.sleep(2000);
                        }catch(Exception e){

                        }
                        votacions = -1;
                        voteStarted = false;
                        NovaRonda();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    public void Stop(){

    }

    public void NovaRonda(){
        DatabaseReference dbReference = database.getReference();

        buttonStart.setText("VOTAR");

        updates.put(codi+"/voteStarted", false);
        dbReference.updateChildren(updates);

        constrainUp.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00B7FF")));
        constrainDown.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B5B4B4")));
        buttonStart.setEnabled(true);
        buttonStart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#006ADC")));
        TextCode.setEnabled(true);
        Code.setEnabled(true);
        GroupName1.setEnabled(true);
        GroupName2.setEnabled(true);
        ronda += 1;
    }

    public void StartPartida(){
        DatabaseReference dbReference = database.getReference("");

        if (nGrups == 2){

            constrainUp.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B5B4B4")));
            constrainDown.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00B7FF")));

            buttonStart.setEnabled(false);
            buttonStart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6E6E6E")));
            TextCode.setEnabled(false);
            Code.setEnabled(false);
            GroupName1.setEnabled(false);
            GroupName2.setEnabled(false);
            //IterationList.setEnabled(true);

            hasStarted = true;
            voteStarted = true;

            updates.put(codi+"/hasStarted", true);
            updates.put(codi+"/voteStarted", true);
            updates.put(codi+"/nGrups", nGrups);
            dbReference.updateChildren(updates);
        }
    }

    public void Start(View view){
        StartPartida();
    }

    public String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) { return inputString; }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) { sb.append('0'); }
        sb.append(inputString);
        return sb.toString();
    }
}