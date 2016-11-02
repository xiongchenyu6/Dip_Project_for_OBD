package com.example.seanh.direction_demo;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import module.DirectionFinder;
import module.DirectionFinderListener;
import module.Route;
import android.app.ProgressDialog;
import module.SmsReceiver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.Console;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements DirectionFinderListener,SmsReceiver.OnSmsReceivedListener{

    private ProgressDialog progressDialog;
    String origin;
    String end;
    private SmsReceiver receiver;

    public String go_duration;
    public String go_distance;
    public String go_instruction;
    public String go_polyline;
    public int go_smsNo;
    public int go_callNo;
    public int stepCounter=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiver= new SmsReceiver();
        receiver.setSmsReceiver(this);

        Button btnNavigate=(Button) findViewById(R.id.button);
        Button btnShowMap=(Button) findViewById(R.id.button2);
        Button btnGo=(Button) findViewById(R.id.button3);

       btnNavigate.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               sendRequest();
           }
       });

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText et1=(EditText) findViewById(R.id.edTxtStart);
                EditText et2=(EditText) findViewById(R.id.edTxtEnd);
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("VALUE_ORIGIN",et1.getText().toString());
                intent.putExtra("VALUE_END",et2.getText().toString());
                MainActivity.this.startActivity(intent);
            }
        });

        btnGo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startLoop();
            }
        });

        IntentFilter intentFilter= new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        this.registerReceiver(receiver,intentFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        this.unregisterReceiver(this.receiver);
    }

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter intentFilter= new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        this.registerReceiver(this.receiver,intentFilter);
    }

    private void sendRequest(){
        EditText et1=(EditText) findViewById(R.id.edTxtStart);
        EditText et2=(EditText) findViewById(R.id.edTxtEnd);
        origin=et1.getText().toString();
        end=et2.getText().toString();
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
        //progressDialog = ProgressDialog.show(this, "Please wait.",
              //  "Finding direction..!", true);
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        //progressDialog.dismiss();
        String inst="";

        for (Route route : routes) {
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            for(int i=0;i<route.instructions.size();i++){
                inst=inst+route.instructions.get(i)+"--";

            }

            inst=inst+"\n"+route.polyline;

            ((TextView) findViewById(R.id.txtInst)).setText(inst);


            go_duration=route.duration.text;
            go_distance=route.distance.text;
            if (route.instructions.size()>1){
                go_instruction=route.instructions.get(0)+"--"+route.instructions.get(1);
            }else {
                go_instruction=route.instructions.get(0);
            }

            go_polyline=route.polyline;

        }
    }

    @Override
    public void onReceived(int msgNo,int callNo) {
        Toast.makeText(this, "New message No is " + msgNo+ " miss call No is " + callNo, Toast.LENGTH_SHORT).show();
        go_smsNo=msgNo;
        go_callNo=callNo;
    }


    private void startLoop(){
        Resources res=getResources();
        String[] lat=res.getStringArray(R.array.lat);
        String[] lng=res.getStringArray(R.array.lng);

        JSONObject data_all=new JSONObject();
        end=lat[lat.length-1]+","+lng[lng.length-1];
        origin=lat[stepCounter]+","+lng[stepCounter];

            try{
                new DirectionFinder(this, origin, end).execute();
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }

            try {
                data_all=packJSon();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (stepCounter<lat.length-1)
                stepCounter++;

        Log.d("JSON info "+stepCounter, data_all.toString() );




    }



    private JSONObject packJSon() throws JSONException{
        JSONObject data_map=new JSONObject();
        JSONObject data_notif=new JSONObject();
        JSONObject temp=new JSONObject();

        data_map.put("duration", go_duration);
        data_map.put("distance", go_distance);
        data_map.put("instruction",go_instruction);
        data_map.put("polyline",go_polyline);

        data_notif.put("sms",go_smsNo);
        data_notif.put("call",go_callNo);

        temp.put("map",data_map);
        temp.put("notification",data_notif);

        return temp;

    }



}
