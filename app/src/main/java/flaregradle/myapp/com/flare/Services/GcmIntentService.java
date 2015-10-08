package flaregradle.myapp.com.Flare.Services;

import android.app.IntentService;
import android.app.Notification;
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

import org.json.JSONObject;

import java.util.Set;

import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class GcmIntentService extends IntentService {

    private Context _context;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        _context = getApplicationContext();

        Bundle extras = intent.getExtras();

        if (extras != null && !extras.isEmpty()) {
            Set<String> keys = extras.keySet();
            String data = extras.getString("data");
            try {
                JSONObject json = new JSONObject(data);
                String type = json.getString("pushType");
                if (type.equals("unknown")) {
                    return;
                }

                if (type.equals("flare")) {
                    String phoneNumber = json.getString("phone");
                    String text = json.getString("text");
                    String latitude = json.getString("latitude");
                    String longitude = json.getString("longitude");

                    handleFlare(phoneNumber,text,latitude,longitude);
                } else if (type.equals("response")) {
                    String from = json.getString("phone");
                    String text = json.getString("text");
                    Boolean accepted = json.getBoolean("accepted");

                    handleResponse(from,text,accepted);
                }
            } catch (Exception ex) {
                Log.e("Flare_Bad_Data",data);
            }
        }
    }

    private void handleFlare(String phoneNumber,String text,String latitude,String longitude) {
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
                int contactId = _context.getResources().getIdentifier("contactdefaultimage","drawable",_context.getPackageName());
                Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), contactId);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap,50,50,false);
                wearableExtender.setBackground(scaled);
            } catch(Exception ex) {
                Log.e("NOTIFICATION", "Unable to load the default contact image for the wearable notification");
            }
        }

        // Make the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(_context)
                .setSmallIcon(R.drawable.flare_notification)
                .setContentTitle(contentTitle)
                .setContentText(text)
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

        // Create intents for actions
        int id = DataStorageHandler.getNotificationId();

        // Decline Action
        Intent declineService = new Intent(_context,NotificationDeclineService.class);
        declineService.putExtra("phone",phoneNumber);
        declineService.putExtra("action", DataStorageHandler.GetDefaultDeclineResponse());
        declineService.putExtra("mID",id);
        PendingIntent declineIntent = PendingIntent.getService(_context,id,declineService,0);
        NotificationCompat.Action declineAction =
                new NotificationCompat.Action.Builder(R.drawable.abc_ic_clear_mtrl_alpha,"Decline",declineIntent).build();
        mBuilder.addAction(declineAction);

        // Accept Action
        Intent acceptService = new Intent(_context,NotificationAcceptService.class);
        acceptService.putExtra("phone",phoneNumber);
        acceptService.putExtra("action", DataStorageHandler.GetDefaultAcceptResponse());
        acceptService.putExtra("location","geo:0,0?q="+latitude+","+longitude+" (" + "Flare from "+contentTitle + ")");
        acceptService.putExtra("mID",id);
        PendingIntent acceptIntent = PendingIntent.getService(_context,id,acceptService,0);
        NotificationCompat.Action acceptAction =
                new NotificationCompat.Action.Builder(R.drawable.send_reg_icon_transparent, "Accept", acceptIntent).build();
        mBuilder.addAction(acceptAction);

        // Build the notification
        Notification notification = mBuilder.build();

        // mId allows you to update the notification later on.
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(_context);
        mNotificationManager.notify(phoneNumber, id, notification);
    }

    private void handleResponse(String from, String text, Boolean accepted) {
        Contact contact = DataStorageHandler.findContact(from);

        String contentTitle = from;
        if(contact != null)
            contentTitle = contact.name;

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Make the wearable extender
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
        if(contact != null)
            wearableExtender.setBackground(contact.photo);
        else {
            try {
                int contactId = _context.getResources().getIdentifier("contactdefaultimage","drawable",_context.getPackageName());
                Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), contactId);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap,50,50,false);
                wearableExtender.setBackground(scaled);
            } catch(Exception ex) {
                Log.e("NOTIFICATION", "Unable to load the default contact image for the wearable notification");
            }
        }

        // Make the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(_context)
                .setContentTitle(contentTitle)
                .setContentText(text)
                .setSound(alarmSound)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setOngoing(false)
                .extend(wearableExtender);

        if(accepted) {
            mBuilder.setSmallIcon(R.drawable.checkmark_icon);
        } else {
            mBuilder.setSmallIcon(R.drawable.clear_icon_2);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mBuilder.setPriority(Notification.PRIORITY_HIGH);

        // Set the sound to play
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if(sound != null)
            mBuilder.setSound(sound);

        // Create intents for actions
        int id = DataStorageHandler.getNotificationId();

        // Build the notification
        Notification notification = mBuilder.build();

        // mId allows you to update the notification later on.
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(_context);
        mNotificationManager.notify(from, id, notification);
    }
}
