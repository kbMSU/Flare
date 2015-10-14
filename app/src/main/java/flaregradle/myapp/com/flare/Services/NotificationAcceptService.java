package flaregradle.myapp.com.Flare.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseCloud;

import java.util.HashMap;

import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class NotificationAcceptService extends IntentService {

    public NotificationAcceptService() {
        super("NotificationAcceptService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String phone = extras.getString("phone");
        String text = extras.getString("action");
        String location = extras.getString("location");
        int id = extras.getInt("mID");

        //SmsManager manager = SmsManager.getDefault();
        //manager.sendTextMessage(phone,null,text,null,null);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(phone,id);

        if(location != null)
        {
            Uri geoLocation = Uri.parse(location);
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW);
            mapsIntent.setData(geoLocation);
            mapsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mapsIntent);
            sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }

        if(text == null || text.isEmpty())
            text = "I will be there ASAP";
        try {
            HashMap<String, String> pushParams = new HashMap<>();
            pushParams.put("text", text);
            pushParams.put("to", phone);
            pushParams.put("from",DataStorageHandler.getPhoneNumber());
            ParseCloud.callFunction("AcceptFlare", pushParams);
        } catch (Exception ex) {
            Log.e("Send_Flare_Accept",ex.getMessage());
        }
    }

}
