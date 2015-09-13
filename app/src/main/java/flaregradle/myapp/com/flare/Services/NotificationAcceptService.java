package flaregradle.myapp.com.Flare.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

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
        //if(text == null || text.isEmpty())
        //    text = "I will be there ASAP";
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

    }

}
