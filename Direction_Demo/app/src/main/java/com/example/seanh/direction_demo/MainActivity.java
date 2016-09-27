package com.example.seanh.direction_demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import module.DirectionFinder;
import module.DirectionFinderListener;
import module.Route;
import android.app.ProgressDialog;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DirectionFinderListener {

    private ProgressDialog progressDialog;
    public List<Route> uniRoutes =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnNavigate=(Button) findViewById(R.id.button);
        Button btnShowMap=(Button) findViewById(R.id.button2);

       btnNavigate.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               sendRequest();
           }
       });

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

    }


    private void sendRequest(){
        EditText et1=(EditText) findViewById(R.id.edTxtStart);
        EditText et2=(EditText) findViewById(R.id.edTxtEnd);

        String origin=et1.getText().toString();
        String end=et2.getText().toString();
        if (origin.isEmpty()){
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (end.isEmpty()){
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            new DirectionFinder(this, origin, end).execute();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        uniRoutes=routes;
        progressDialog.dismiss();
        String inst="";

        for (Route route : routes) {
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            for(int i=0;i<route.instructions.size();i++){
                inst=inst+route.instructions.get(i)+"--";

            }

            ((TextView) findViewById(R.id.txtInst)).setText(inst);

        }
    }



}
