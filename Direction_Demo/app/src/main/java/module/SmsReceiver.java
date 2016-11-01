package module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


/**
 * Created by seanh on 2/11/2016.
 */


    public class SmsReceiver extends BroadcastReceiver {
        private SharedPreferences preferences;
    private int counter=0;

    public interface OnSmsReceivedListener {
        void onSmsReceived(int msgNo);
    }

    private OnSmsReceivedListener listener = null;

    public void setSmsReceiver(Context context) {
        this.listener = (OnSmsReceivedListener) context;
    }


    @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
                SmsMessage[] msgs = null;
                String msg_from;
                if (bundle != null) {
                    //---retrieve the SMS message received---
                    try {

                        counter++;
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        msgs = new SmsMessage[pdus.length];
                        if (listener != null)
                            listener.onSmsReceived(counter);

                    } catch (Exception e) {
                        Log.d("Exception caught", e.getMessage());
                    }
                }
            }
        }


    }

