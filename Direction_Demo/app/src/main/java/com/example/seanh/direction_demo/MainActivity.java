package com.example.seanh.direction_demo;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.StrictMode;
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


import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RunnableFuture;
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
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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
                data_all=packJSon();//data_all is the final data package to the rpi
                sendData();
                sendToServer(data_all);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        Log.d("JSON info "+stepCounter, data_all.toString() );

            if (stepCounter<lat.length-1)
                stepCounter++;

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
    private void sendToServer(JSONObject data){

        class task implements Runnable{
            JSONObject data;
            task(JSONObject data){this.data=data;}
            public void run() {
        Uri uri = Uri.parse("http://192.168.42.94:4000/data?")
                    .buildUpon()
                    .appendQueryParameter("key",data.toString())
                    .build();
            Log.d("uri",uri.toString());
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.d("url",url.toString());
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Start to send",data.toString());
        try{

            Log.d("Conneter",urlConnection.toString());
            int code = urlConnection.getResponseCode();
            Log.d("code",code+"");
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());



        }catch (Exception e){
            urlConnection.disconnect();
            String msg = (e.getMessage()==null)?"Login failed!":e.getMessage();
            Log.i("Login Error1",msg);
        }
            }
        };
        Thread newTread = new Thread(new task(data));
        newTread.start();
    }
    public void sendData(){
        Runnable a= new Runnable() {
            @Override
            public void run() {
                try {
                    String path = "http://www.baidu.com";
                    // 1.声明访问的路径， url 网络资源 http ftp rtsp
                    URL url = new URL(path);
                    // 2.通过路径得到一个连接 http的连接
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    // 3.判断服务器给我们返回的状态信息。
                    // 200 成功 302 从定向 404资源没找到 5xx 服务器内部错误
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        // 4.利用链接成功的 conn 得到输入流
                        InputStream is = conn.getInputStream();// png的图片

                        // 5. ImageView设置Bitmap,用BitMap工厂解析输入流
                        ;
                    } else {
                        // 请求失败
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        };
        Thread b= new Thread(a);
        b.start();
    }




}
