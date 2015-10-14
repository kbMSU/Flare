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
import com.parse.ParseCloud;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.List;

import flaregradle.myapp.com.Flare.Utilities.AzureNotificationsHandler;
import flaregradle.myapp.com.Flare.BackendItems.DeviceItem;
import flaregradle.myapp.com.Flare.Activities.LoadScreen;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class GcmRegistrationAsyncTask extends AsyncTask<Context, Void, String> {
    private Context _context;

    public GcmRegistrationAsyncTask(){
    }

    @Override
    protected String doInBackground(Context... params) {
        _context = params[0];

        String msg = "Device Registered";
        try {
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

            if(savedPhones == null || savedPhones.size() == 0) {
                ParseObject newDevice = new ParseObject("Device");
                newDevice.put("FullPhone", fullPhone);
                newDevice.put("CountryCode", code);
                newDevice.put("Number", phone);
                newDevice.put("DeviceType","Android");
                newDevice.saveInBackground();
            }
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
