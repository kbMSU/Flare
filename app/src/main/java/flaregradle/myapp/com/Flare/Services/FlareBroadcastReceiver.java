package flaregradle.myapp.com.Flare.Services;

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
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONObject;

import java.util.Set;

import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.Utilities.ContactsHandler;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class FlareBroadcastReceiver extends ParsePushBroadcastReceiver {

    private Context _context;

    @Override
    public void onPushReceive(Context context, Intent intent) {
        //super.onPushReceive(context,intent);

        _context = context;
        Bundle extras = intent.getExtras();

        if (extras != null && !extras.isEmpty()) {
            try {
                Set<String> keys = extras.keySet();
                //String data = extras.getString("data");
                String data = extras.getString("com.parse.Data");
                JSONObject json = new JSONObject(data);
                String type = json.getString("pushType");
                if (type.equals("unknown")) {
                    super.onPushReceive(context,intent);
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
                super.onPushReceive(context,intent);
                Log.e("Flare_Bad_Data","Unable to parse data");
            }
        }
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        return super.getNotification(context,intent);

        /*_context = context;
        Notification notification = new Notification();
        try {

            try {
                Bundle extras = intent.getExtras();
                if(extras == null) {
                    Log.e("flare_data","intent has no extras");
                } else {
                    Set<String> keys = extras.keySet();
                    for(String key : keys) {
                        Log.i("key",key);
                    }
                }
            } catch (Exception ex) {
                Log.e("flare_data",ex.getMessage());
                Log.e("flare_data","Has no extras");
            }

            String str = intent.getStringExtra("com.parse.Data");

            Log.i("flare_data","got data");
            Log.i("flare_data",str);

            JSONObject json = new JSONObject(str);

            Log.i("flare_data","created json");

            String type = json.getString("pushType");
            if (type.equals("unknown")) {
                Log.i("flare_data",type);
                notification = super.getNotification(context,intent);
            }

            if (type.equals("flare")) {
                Log.i("flare_data",type);
                String phoneNumber = json.getString("phone");
                String text = json.getString("text");
                String latitude = json.getString("latitude");
                String longitude = json.getString("longitude");
                Log.i("flare_data","got info");

                notification = getFlareNotification(phoneNumber, text, latitude, longitude);
            } else if (type.equals("response")) {
                Log.i("flare_data",type);
                String from = json.getString("phone");
                String text = json.getString("text");
                Boolean accepted = json.getBoolean("accepted");
                Log.i("flare_data","got info");

                notification = getResponseNotification(from, text, accepted);
            }
        } catch (Exception ex) {
            Log.e("exception",ex.getMessage());
            Log.e("Flare_Bad_Data","Unable to parse data");
        }
        return notification;*/
    }

    private Notification getFlareNotification(String phoneNumber,String text,String latitude,String longitude) {
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

        return notification;
    }

    private Notification getResponseNotification(String from,String text,Boolean accepted) {
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

        return notification;
    }

    private void handleFlare(String phoneNumber,String text,String latitude,String longitude) {
        DataStorageHandler.getInstance();
        if(DataStorageHandler.AllContacts.isEmpty()) {
            DataStorageHandler.Preferences =  _context.getSharedPreferences("flaregradle.myapp.com.Flare_preferences", Context.MODE_PRIVATE);
            DataStorageHandler.setupPreferences();

            ContactsHandler _contactsHandler = new ContactsHandler(_context.getContentResolver());
            int contactId = _context.getResources().getIdentifier("person", "drawable", _context.getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), contactId);
            _contactsHandler.setDefaultImage(bitmap);
            DataStorageHandler.AllContacts = _contactsHandler.getContacts();
        }

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
        DataStorageHandler.getInstance();
        if(DataStorageHandler.AllContacts.isEmpty()) {
            DataStorageHandler.Preferences =  _context.getSharedPreferences("flaregradle.myapp.com.Flare_preferences", Context.MODE_PRIVATE);
            DataStorageHandler.setupPreferences();

            ContactsHandler _contactsHandler = new ContactsHandler(_context.getContentResolver());
            int contactId = _context.getResources().getIdentifier("person", "drawable", _context.getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), contactId);
            _contactsHandler.setDefaultImage(bitmap);
            DataStorageHandler.AllContacts = _contactsHandler.getContacts();
        }

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