package com.example.seanh.direction_demo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import module.DirectionFinder;
import module.DirectionFinderListener;
import module.GpsUpdateService;
import module.Route;
import android.app.ProgressDialog;
import module.sms_call_Receiver;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DirectionFinderListener, sms_call_Receiver.OnSmsReceivedListener{

    private ProgressDialog progressDialog;
    static String origin;
    static String end;
    private sms_call_Receiver receiver;
    private GpsUpdateService gpsUpdate;
    private int distanceValue=-1;
    private String ipAddress;

    Messenger msgService;
    boolean isBound;
    private static final int MESSAGE=1;
    private String currentLocation;

    public String go_duration;
    public String go_distance;
    public int go_distance_value;
    public String go_instruction;
    public String go_polyline;
    public String go_smsName;
    public String go_callerName;
    public int go_stepDistance;
    public int go_smsNo;
    public int go_callNo;
    public int stepCounter=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiver= new sms_call_Receiver();
        receiver.setSmsReceiver(this);



        Button btnNavigate=(Button) findViewById(R.id.button);
        Button btnShowMap=(Button) findViewById(R.id.button2);
        Button btnGo=(Button) findViewById(R.id.button3);
        Button btnChangeIPAddress=(Button) findViewById(R.id.btnChange);

        Spinner spinner=(Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.IP_address, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                    ipAddress=parent.getItemAtPosition(pos).toString();
                Toast.makeText(MainActivity.this, "The current IP address is set to "+ipAddress, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });


        btnChangeIPAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText etIP=(EditText) findViewById(R.id.edTxtIPAddress);
                ipAddress="http://"+etIP.getText()+":4000/data?";
                Toast.makeText(MainActivity.this, "The IP address is changed to "+ ipAddress, Toast.LENGTH_SHORT).show();
            }
        });

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

                if(origin==null || end==null) {
                    Toast.makeText(MainActivity.this, "The entered address is not valid", Toast.LENGTH_SHORT).show();
                }else {
                   if(distanceValue==-1){
                       Toast.makeText(MainActivity.this, "Please enter valid start and end point", Toast.LENGTH_SHORT).show();
                   }else if(distanceValue<200&&distanceValue>0){
                       Toast.makeText(MainActivity.this, "You have arrived at destination", Toast.LENGTH_SHORT).show();
                   }else{
                       startLoop();

                   }

                }

              // Log.d("test", "test" );

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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
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
                //"Finding direction..!", true);
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

            origin=route.points.get(1).toString().replaceAll("[lat/ng: ()]","");
            //end=route.points.get(route.points.size()-1).toString().replaceAll("[\\[\\](){}]","");
            //origin=route.step_startLocation.toString().replaceAll("[lat/ng: ()]","");
            end=route.endLocation.toString().replaceAll("[lat/ng: ()]","");

            distanceValue=route.distance.value;
            Log.d("test", String.valueOf(distanceValue) );

            ((TextView) findViewById(R.id.txtInst)).setText(inst);


            go_duration=route.duration.text;
            go_distance=route.distance.text;
            go_distance_value=route.distance.value;
            go_stepDistance=route.step_distance.value;
            if (route.instructions.size()>1){
                go_instruction=route.instructions.get(0)+"--"+route.instructions.get(1);
            }else {
                go_instruction=route.instructions.get(0);
            }

            go_polyline=route.polyline;

        }
    }

    @Override
    public void onReceived(int msgNo,String msgName,int callNo,String callerName) {
        Toast.makeText(this, "New message No is " + msgNo+ " miss call No is " + callNo, Toast.LENGTH_SHORT).show();
        go_smsNo=msgNo;
        go_callNo=callNo;
        go_smsName=msgName;
        go_callerName=callerName;

    }

    //@Override
    public void onUpdate(Location location){
        Log.d("test", "ldsfalfj" );
    }



    private void startLoop(){
/*
        Resources res=getResources();
        String[] lat=res.getStringArray(R.array.lat);
        String[] lng=res.getStringArray(R.array.lng);

        end=lat[lat.length-1]+","+lng[lng.length-1];
        origin=lat[stepCounter]+","+lng[stepCounter];
*/

        Log.d("geocord origin", origin );
        Log.d("geocord end", end );

        JSONObject data_all=new JSONObject();

            try{
                new DirectionFinder(this, origin, end).execute();
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }

            try {
                data_all=packJSon();//data_all is the final data package to the rpi
                sendToServer(data_all);
                //new sendDataToServer().execute(data_all);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        Log.d("JSON info "+stepCounter, data_all.toString() );

           // if (stepCounter<lat.length-1)
             //   stepCounter++;

    }

    private JSONObject packJSon() throws JSONException{
        JSONObject data_map=new JSONObject();
        JSONObject data_notif=new JSONObject();
        JSONObject temp=new JSONObject();

        data_map.put("duration", go_duration);
        data_map.put("distance", go_distance);
        data_map.put("distance_value",go_distance_value);
        data_map.put("step_distance", go_stepDistance);
        data_map.put("instruction",go_instruction);
        data_map.put("polyline",go_polyline);

        data_notif.put("sms",go_smsNo);
        data_notif.put("smsNo",go_smsName);
        data_notif.put("call",go_callNo);
        data_notif.put("callNo",go_callerName);

        temp.put("map",data_map);
        temp.put("notification",data_notif);

        return temp;

    }

    private void sendToServer(JSONObject data){

        class task implements Runnable{
            JSONObject data;
            task(JSONObject data){this.data=data;}
            public void run() {
        Uri uri = Uri.parse(ipAddress)
                    .buildUpon()
                    .appendQueryParameter("key",data.toString())
                    .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            int code = urlConnection.getResponseCode();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        }catch (Exception e){
            urlConnection.disconnect();
        }
            }
        }
        Thread newTread = new Thread(new task(data));
        newTread.start();
    }

    private class sendDataToServer extends AsyncTask<JSONObject, Void, Void>{

        @Override
        protected Void doInBackground(JSONObject... params) {
            HttpURLConnection urlConnection = null;
            JSONObject data=params[0];

            try {
                Uri uri = Uri.parse(ipAddress)
                        .buildUpon()
                        .appendQueryParameter("key",data.toString())
                        .build();
                URL url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();

                int code = urlConnection.getResponseCode();
                //InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }finally {
                urlConnection.disconnect();
            }
            return null;
        }

    }


}
