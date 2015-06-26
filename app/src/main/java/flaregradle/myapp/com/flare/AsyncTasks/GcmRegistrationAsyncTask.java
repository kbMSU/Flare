package flaregradle.myapp.com.Flare.AsyncTasks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import flaregradle.myapp.com.Flare.AzureNotificationsHandler;
import flaregradle.myapp.com.Flare.BackendItems.DeviceItem;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;
import flaregradle.myapp.com.Flare.LoadScreen;

public class GcmRegistrationAsyncTask extends AsyncTask<Context, Void, String> {

    private GoogleCloudMessaging gcm;
    private Context context;

    // Google Developers Console project number
    private static final String SENDER_ID = "97762557726";

    // Azure connection string
    private static final String connection_string = "Endpoint=sb://flares.servicebus.windows.net/;SharedAccessKeyName=DefaultFullSharedAccessSignature;SharedAccessKey=+ofrbn1EiYh6j+bgD8z0w20G8oa781CctkemzeQITXU=";

    private LoadScreen _parent;
    private String phoneNumber;

    public GcmRegistrationAsyncTask(LoadScreen parent, String number){
        phoneNumber = number;
        _parent = parent;
    }

    @Override
    protected String doInBackground(Context... params) {

        context = params[0];

        String msg = "Getting gcm";
        boolean registered = false;
        //while(!registered) {
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                msg = "Getting regId";
                String regId = gcm.register(SENDER_ID);

                DataStorageHandler dataStore = DataStorageHandler.getInstance();
                dataStore.registrationId = regId;

                msg = "Parsing phone number";
                // Split the phone number
                String phone = "";
                String code = "";

                for (int i= phoneNumber.length()-1 ; i >= 0; i--) {
                    if(phone.length() < 10) {
                        phone += phoneNumber.charAt(i);
                    } else {
                        code += phoneNumber.charAt(i);
                    }
                }

                code = new StringBuilder(code).reverse().toString();
                phone = new StringBuilder(phone).reverse().toString();

                msg = "Instantiating mobile service client";
                // Save phone data in backend
                MobileServiceClient client = new MobileServiceClient(
                        "https://flareservice.azure-mobile.net/",
                        "vAymygcCyvnOQrDzLOEjyOQGIxIJMm78",
                        context);

                msg = "Getting devices table";
                MobileServiceTable<DeviceItem> devices = client.getTable(DeviceItem.class);

                msg = "Getting phone and reg Items";
                MobileServiceList<DeviceItem> phoneItems = devices.where().field("FullPhone").eq(phoneNumber).execute().get();
                MobileServiceList<DeviceItem> regItems = devices.where().field("RegId").eq(regId).execute().get();

                msg = "Saving device in SQL database";
                if(phoneItems.size() == 0){
                    // This phone has not been registered before, how about this device ?
                    if(regItems.size() > 0) {
                        // This device has been registered before , lets update it
                        DeviceItem item = regItems.get(0);
                        item.regId = regId;
                        item.fullPhone = phoneNumber;
                        item.countryCode = code;
                        item.number = phone;
                        item.deviceType = "android";
                        devices.update(item);
                    }
                    else {
                        // This phone number has not been registered , nor has this device
                        DeviceItem item = new DeviceItem();
                        item.regId = regId;
                        item.fullPhone = phoneNumber;
                        item.countryCode = code;
                        item.number = phone;
                        item.deviceType = "android";
                        devices.insert(item);
                    }
                } else {
                    // This phone number has been registered before
                    DeviceItem item = phoneItems.get(0);
                    if(item.regId == null || !item.regId.equals(regId)) {
                        // It was registered to a different device so lets update it
                        item.regId = regId;
                        item.deviceType = "android";
                        item.fullPhone = phoneNumber;
                        item.countryCode = code;
                        item.number = phone;
                        devices.update(item);
                    }
                }

                msg = "Registering with notification hubs";
                // Register with notification hubs
                NotificationsManager.handleNotifications(context, SENDER_ID, AzureNotificationsHandler.class);
                NotificationHub hub = new NotificationHub("flarenotifications",connection_string,context);
                hub.register(regId,phoneNumber);

                msg = "Device Registered";
                registered = true;

            } catch (Exception ex) {
                msg += " : " + ex.getMessage();
            }

            /*try {
                if(!msg.equals("Device Registered"))
                    wait(5000);
            } catch (InterruptedException e) {
                msg = e.getMessage();
            }*/
        //}

        return msg;
    }

    @Override
    protected void onPostExecute(String msg) {
        /*if(!DataStorageHandler.getInstance().loadedHomeScreen) {
            DataStorageHandler.getInstance().loadedHomeScreen = true;
            if(!msg.startsWith("Device Registered")) {
                showErrorOverlay(msg);
            } else {
                onDeviceRegister();
            }
        } else {
            if(!msg.startsWith("Device Registered")) {
                Log.e("Registration",msg);
                onFailedToRegister();
            }
        }*/

        if(msg.equals("Device Registered"))
            DataStorageHandler.getInstance().Registered = true;
    }

    /*private void onDeviceRegister() {
        _parent.continueToHomeScreen();
    }

    private void onFailedToRegister() {
        GcmRegistrationAsyncTask task = new GcmRegistrationAsyncTask(_parent,phoneNumber);
        task.execute(_parent);

        _parent.retryRegister();
    }
    private void showErrorOverlay(String msg) {
        final String message = msg;

        AlertDialog.Builder alert = new AlertDialog.Builder(_parent);
        alert.setTitle("Error");
        alert.setMessage("Could not connect, We'll keep trying. Would you like to send the error to the Flare team ?");
        alert.setPositiveButton("Send",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _parent.sendError(message);
            }
        });
        alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                proceed();
                dialog.dismiss();
            }
        });
        alert.show();
    }
    public void proceed(){
        onFailedToRegister();
        onDeviceRegister();
    }*/
}
