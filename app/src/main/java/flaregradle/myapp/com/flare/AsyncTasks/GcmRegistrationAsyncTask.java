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
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import flaregradle.myapp.com.Flare.Utilities.AzureNotificationsHandler;
import flaregradle.myapp.com.Flare.BackendItems.DeviceItem;
import flaregradle.myapp.com.Flare.Activities.LoadScreen;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class GcmRegistrationAsyncTask extends AsyncTask<Context, Void, String> {

    /*private GoogleCloudMessaging gcm;

    // Google Developers Console project number
    private static final String SENDER_ID = "97762557726";

    // Azure connection string
    private static final String connection_string = "Endpoint=sb://flares.servicebus.windows.net/;SharedAccessKeyName=DefaultFullSharedAccessSignature;SharedAccessKey=+ofrbn1EiYh6j+bgD8z0w20G8oa781CctkemzeQITXU=";
    */

    private Context _context;

    public GcmRegistrationAsyncTask(){
    }

    @Override
    protected String doInBackground(Context... params) {

        _context = params[0];

        String msg = "Device Registered";
        try {
            /*if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            String regId = gcm.register(SENDER_ID);*/
            String code = DataStorageHandler.getCountryCode();
            String phone = DataStorageHandler.getPhoneNumber();
            String fullPhone = code+phone;

            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("FullPhone",fullPhone);
            installation.put("Number",phone);
            installation.put("CountryCode",code);
            installation.save();

            // Lets see if this phone number or this device has been saved
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Device").whereEqualTo("FullPhone",fullPhone);
            List<ParseObject> savedPhones = query.find();
            //ParseQuery<ParseObject> deviceQuery = ParseQuery.getQuery("Device").whereEqualTo("RegId", regId);
            //List<ParseObject> savedDevices = deviceQuery.find();

            if(savedPhones == null || savedPhones.size() == 0) {
                // This phone number has not been saved before, how about this device ?
                /*if(savedDevices != null && savedDevices.size() > 0) {
                    // This device has been saved before , lets update it
                    ParseObject savedDevice = savedDevices.get(0);
                    savedDevice.put("FullPhone",fullPhone);
                    savedDevice.put("CountryCode",code);
                    savedDevice.put("Number", phone);
                    savedDevice.saveInBackground();

                } else {*/
                    // This phone number has not been saved , neither has this device
                    ParseObject newDevice = new ParseObject("Device");
                    //newDevice.put("RegId", regId);
                    newDevice.put("FullPhone", fullPhone);
                    newDevice.put("CountryCode", code);
                    newDevice.put("Number", phone);
                    newDevice.put("DeviceType","Android");
                    newDevice.saveInBackground();
                }
            /*} else {
                ParseObject savedPhone = savedPhones.get(0);
                // This phone has been registered before
                if(savedDevices != null && savedDevices.size() > 0) {
                    ParseObject savedDevice = savedDevices.get(0);
                    if(!savedPhone.getString("RegId").equals(regId)) {
                        // This phone number was registered against a different device
                        savedDevice.put("RegId",regId);
                        savedDevice.put("DeviceType","Android");
                        savedDevice.saveInBackground();
                    }
                }
            }*/

            /*MobileServiceClient client = new MobileServiceClient(
                    "https://flareservice.azure-mobile.net/",
                    "vAymygcCyvnOQrDzLOEjyOQGIxIJMm78",
                    context);

            MobileServiceTable<DeviceItem> devices = client.getTable(DeviceItem.class);
            MobileServiceList<DeviceItem> phoneItems = devices.where().field("FullPhone").eq(fullPhone).execute().get();
            MobileServiceList<DeviceItem> regItems = devices.where().field("RegId").eq(regId).execute().get();

            if(phoneItems.size() == 0){
                // This phone has not been registered before, how about this device ?
                if(regItems.size() > 0) {
                    // This device has been registered before , lets update it
                    DeviceItem item = regItems.get(0);
                    item.regId = regId;
                    item.fullPhone = fullPhone;
                    item.countryCode = code;
                    item.number = phone;
                    item.deviceType = "android";
                    devices.update(item);
                }
                else {
                    // This phone number has not been registered , nor has this device
                    DeviceItem item = new DeviceItem();
                    item.regId = regId;
                    item.fullPhone = fullPhone;
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
                    item.fullPhone = fullPhone;
                    item.countryCode = code;
                    item.number = phone;
                    devices.update(item);
                }
            }*/

            // Register with notification hubs
            /*NotificationsManager.handleNotifications(context, SENDER_ID, AzureNotificationsHandler.class);
            NotificationHub hub = new NotificationHub("flarenotifications",connection_string, context);
            hub.register(regId,fullPhone);*/

        } catch (Exception ex) {
            msg += " : " + ex.getMessage();
        }

        return msg;
    }

    @Override
    protected void onPostExecute(String msg) {
        if(msg.equals("Device Registered"))
            DataStorageHandler.SetRegistered();
        else {
            Toast.makeText(_context,msg,Toast.LENGTH_LONG).show();
        }
    }
}
