package flaregradle.myapp.com.Flare.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

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
        //GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        //String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            // Since we're not using two way messaging, this is all we really to check for
        //    if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String type = extras.getString("type");
                if(type == null) {
                    GcmBroadcastReceiver.completeWakefulIntent(intent);
                    return;
                }

                //if(type.equals("flare")) {
                    handleFlare(extras);
                //} else if(type.equals("response")) {
                //    handleResponse(extras);
                //}
            }
        //}
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handleFlare(Bundle extras) {
        String phoneNumber = extras.getString("phone");
        Contact contact = DataStorageHandler.findContact(phoneNumber);

        String contentTitle = phoneNumber;
        if(contact != null)
            contentTitle = contact.name;

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Make the wearable extender
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
        if(contact != null)
            wearableExtender.setBackground(contact.photo);
        else {
            try {
                int contactId = getResources().getIdentifier("contactdefaultimage","drawable",getPackageName());
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), contactId);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap,50,50,false);
                wearableExtender.setBackground(scaled);
            } catch(Exception ex) {
                Log.e("NOTIFICATION","Unable to load the default contact image for the wearable notification");
            }
        }

        // Make the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.flare_notification)
                .setContentTitle(contentTitle)
                .setContentText(extras.getString("text"))
                .setSound(alarmSound)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setOngoing(false)
                .extend(wearableExtender);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mBuilder.setPriority(Notification.PRIORITY_HIGH);

        // Set the sound to play
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if(sound != null)
            mBuilder.setSound(sound);

        String latitude = extras.getString("latitude");
        String longitude = extras.getString("longitude");

        // Create intents for actions
        int id = DataStorageHandler.getNotificationId();

        // Decline Action
        Intent declineService = new Intent(getApplicationContext(),NotificationDeclineService.class);
        declineService.putExtra("phone",phoneNumber);
        declineService.putExtra("action", DataStorageHandler.GetDefaultDeclineResponse());
        declineService.putExtra("mID",id);
        PendingIntent declineIntent = PendingIntent.getService(this,id,declineService,0);
        NotificationCompat.Action declineAction =
                new NotificationCompat.Action.Builder(R.drawable.abc_ic_clear_mtrl_alpha,"Decline",declineIntent).build();
        mBuilder.addAction(declineAction);

        // Accept Action
        Intent acceptService = new Intent(getApplicationContext(),NotificationAcceptService.class);
        acceptService.putExtra("phone",phoneNumber);
        acceptService.putExtra("action", DataStorageHandler.GetDefaultAcceptResponse());
        acceptService.putExtra("location","geo:0,0?q="+latitude+","+longitude+" (" + "Flare from "+contentTitle + ")");
        acceptService.putExtra("mID",id);
        PendingIntent acceptIntent = PendingIntent.getService(this,id,acceptService,0);
        NotificationCompat.Action acceptAction =
                new NotificationCompat.Action.Builder(R.drawable.send_reg_icon_transparent, "Accept", acceptIntent).build();
        mBuilder.addAction(acceptAction);

        // Build the notification
        Notification notification = mBuilder.build();

        // mId allows you to update the notification later on.
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        mNotificationManager.notify(phoneNumber, id, notification);
    }
}
