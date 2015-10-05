package flaregradle.myapp.com.Flare.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import com.parse.ParseCloud;

import java.util.HashMap;

import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class NotificationDeclineService extends IntentService {

    public NotificationDeclineService() {
        super("NotificationDeclineService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String phone = extras.getString("phone");
        String text = extras.getString("action");
        int id = extras.getInt("mID");

        /*SmsManager manager = SmsManager.getDefault();
        if(text == null || text.isEmpty())
            text = "Sorry, i can't make it";
        manager.sendTextMessage(phone,null,text,null,null);*/

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(phone,id);

        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        if(text == null || text.isEmpty())
            text = "Sorry, i can't make it";
        try {
            HashMap<String, String> pushParams = new HashMap<>();
            pushParams.put("text", text);
            pushParams.put("to", phone);
            pushParams.put("from",DataStorageHandler.getPhoneNumber());
            ParseCloud.callFunction("DeclineFlare", pushParams);
        } catch (Exception ex) {
            Log.e("Send_Flare_Decline",ex.getMessage());
        }
    }
}
