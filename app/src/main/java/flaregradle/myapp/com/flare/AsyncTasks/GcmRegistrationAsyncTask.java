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

import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
import flaregradle.myapp.com.Flare.Events.RegistrationError;
import flaregradle.myapp.com.Flare.Events.RegistrationSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.AzureNotificationsHandler;
import flaregradle.myapp.com.Flare.BackendItems.DeviceItem;
import flaregradle.myapp.com.Flare.Activities.LoadScreen;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class GcmRegistrationAsyncTask extends AsyncTask<Context, Void, String> {
    private Context _context;
    private Exception _exception;

    public GcmRegistrationAsyncTask(){
    }

    @Override
    protected String doInBackground(Context... params) {
        _context = params[0];

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
            } else {
                ParseObject device = savedPhones.get(0);
                if(!device.getString("DeviceType").equals("Android")) {
                    device.put("DeviceType","Android");
                }
                device.saveInBackground();
            }
        } catch (Exception ex) {
            _exception = ex;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String msg) {
        if(_exception == null) {
            DataStorageHandler.SetRegistered();

            String code = DataStorageHandler.getCountryCode();
            String number = DataStorageHandler.getPhoneNumber();
            String fullPhone = code+number;
            for(Contact contact : DataStorageHandler.AllContacts.values()) {
                for(PhoneNumber num : contact.allPhoneNumbers) {
                    if(num.number.equals(number) || num.number.equals(fullPhone)) {
                        markContactAsHavingFlare(contact);
                        break;
                    }
                }
            }

            EventsModule.Post(new RegistrationSuccess());
        } else {
            EventsModule.Post(new RegistrationError(_exception));
        }
    }

    private void markContactAsHavingFlare(Contact contact) {
        contact.hasFlare = true;
        contact.phoneNumber.hasFlare = true;
        for(PhoneNumber phone : contact.allPhoneNumbers) {
            phone.hasFlare = true;
        }
    }
}
