package com.example.seanh.dip_mapdirection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;


/**
 * Created by seanh on 26/9/2016.
 */

public class LaunchActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }



        Button clickButton=(Button) findViewById(R.id.button);
        clickButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

    }

}
