package flaregradle.myapp.com.Flare.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.MyApp.Flare.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            // Since we're not using two way messaging, this is all we really to check for
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String phoneNumber = extras.getString("phone");
                Contact contact = DataStorageHandler.findContact(phoneNumber);

                String contentTitle = phoneNumber;
                if(contact != null)
                    contentTitle = contact.name;

                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                // Make the notification
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                                                        .setSmallIcon(R.drawable.flare_notification)
                                                        .setContentTitle(contentTitle)
                                                        .setContentText(extras.getString("text"))
                                                        .setSound(alarmSound)
                                                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                                                        .setOngoing(true);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    mBuilder.setPriority(Notification.PRIORITY_HIGH);

                // Set the sound to play
                Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if(sound != null)
                    mBuilder.setSound(sound);

                String latitude = extras.getString("latitude");
                String longitude = extras.getString("longitude");

                // Create intents for actions
                int id = DataStorageHandler.getInstance().getNotificationId();

                // Decline Action
                Intent declineService = new Intent(getApplicationContext(),NotificationDeclineService.class);
                declineService.putExtra("phone",phoneNumber);
                declineService.putExtra("action",DataStorageHandler.getInstance().GetDefaultDeclineResponse());
                declineService.putExtra("mID",id);
                PendingIntent declineIntent = PendingIntent.getService(this,id,declineService,0);
                mBuilder.addAction(R.drawable.abc_ic_clear_mtrl_alpha,"Decline",declineIntent);

                // Accept Action
                Intent acceptService = new Intent(getApplicationContext(),NotificationAcceptService.class);
                acceptService.putExtra("phone",phoneNumber);
                acceptService.putExtra("action",DataStorageHandler.getInstance().GetDefaultAcceptResponse());
                acceptService.putExtra("location","geo:0,0?q="+latitude+","+longitude+" (" + "Flare from "+contentTitle + ")");
                acceptService.putExtra("mID",id);
                PendingIntent acceptIntent = PendingIntent.getService(this,id,acceptService,0);
                mBuilder.addAction(R.drawable.send_reg_icon_transparent, "Accept", acceptIntent);

                // Build the notification
                Notification notification = mBuilder.build();

                // mId allows you to update the notification later on.
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(phoneNumber,id, notification);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
