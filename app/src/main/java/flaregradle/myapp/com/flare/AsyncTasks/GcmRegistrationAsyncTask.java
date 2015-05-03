package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
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

        String msg = "";
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            String regId = gcm.register(SENDER_ID);

            DataStorageHandler dataStore = DataStorageHandler.getInstance();
            dataStore.registrationId = regId;

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

            // Save phone data in backend
            MobileServiceClient client = new MobileServiceClient(
                    "https://flareservice.azure-mobile.net/",
                    "vAymygcCyvnOQrDzLOEjyOQGIxIJMm78",
                    context);

            MobileServiceTable<DeviceItem> devices = client.getTable(DeviceItem.class);

            MobileServiceList<DeviceItem> phoneItems = devices.where().field("FullPhone").eq(phoneNumber).execute().get();
            MobileServiceList<DeviceItem> regItems = devices.where().field("RegId").eq(regId).execute().get();
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

            // Register with notification hubs
            NotificationsManager.handleNotifications(context,SENDER_ID,AzureNotificationsHandler.class);
            NotificationHub hub = new NotificationHub("flarenotifications",connection_string,context);
            hub.register(regId,phoneNumber);

            msg = "Device Registered ";

        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "Error: " + ex.getMessage();
        }
        return msg;
    }

    @Override
    protected void onPostExecute(String msg) {
        if(msg.startsWith("Device Registered")) {
            onDeviceRegister();
            return;
        }

        Toast.makeText(context, "Retrying, Failed to connect : "+msg, Toast.LENGTH_SHORT).show();
        onFailedToRegister();

        Logger.getLogger("REGISTRATION").log(Level.INFO, msg);
    }

    private void onDeviceRegister() {
        _parent.continueToHomeScreen();
    }
    private void onFailedToRegister() {
        _parent.retryRegister();
    }
}
