package module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * Created by seanh on 2/11/2016.
 */


public class SmsReceiver extends BroadcastReceiver {
        private SharedPreferences preferences;
    private int smscounter=0;
    private int callcounter=0;

    static boolean ring=false;
    static boolean callReceived=false;

    public interface OnSmsReceivedListener {
        void onReceived(int msgNo,int callNo);
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
            String from=null;
            String msg= null;
            String str = "";
            if (bundle != null) {
                //---retrieve the SMS message received---
                try {

                    smscounter++;
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    for (int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        str += "SMS from " + msgs[i].getOriginatingAddress();
                        from = msgs[i].getOriginatingAddress();
                        str += " :";
                        str += msgs[i].getMessageBody().toString();
                        msg = msgs[i].getMessageBody().toString();
                        str += "\n";
                    }


                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (state!=null) {

            // If phone state "Rininging"
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                ring = true;
                // Get the Caller's Phone Number
                Bundle bundle = intent.getExtras();
                String callerPhoneNumber = bundle.getString("incoming_number");
                callcounter++;
            }

            // If incoming call is received
            if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                callReceived = true;
            }


            // If phone is Idle
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                // If phone was ringing(ring=true) and not received(callReceived=false) , then it is a missed call
                if (ring == true && callReceived == false) {
                    //callcounter++;
                }
            }
        }

        if (listener != null)
            listener.onReceived(smscounter,callcounter/2);


    }

    }

