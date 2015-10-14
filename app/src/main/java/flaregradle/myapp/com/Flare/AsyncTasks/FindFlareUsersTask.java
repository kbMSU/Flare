package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.ExecutableQuery;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import flaregradle.myapp.com.Flare.BackendItems.DeviceItem;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;


public class FindFlareUsersTask extends AsyncTask<Context,Void,Void> {

    @Override
    protected Void doInBackground(Context... params) {
        try {
            List<String> numbers = new ArrayList<>();
            for(Contact c : DataStorageHandler.AllContacts.values()) {
                for(PhoneNumber phone : c.allPhoneNumbers) {
                    String number = phone.number;
                    numbers.add(number);
                }
            }
            ParseQuery<ParseObject> numberQuery = ParseQuery.getQuery("Device").whereContainedIn("Number",numbers);
            ParseQuery<ParseObject> fullPhoneQuery = ParseQuery.getQuery("Device").whereContainedIn("FullPhone",numbers);
            List<ParseQuery<ParseObject>> queries = new ArrayList<>();
            queries.add(numberQuery);
            queries.add(fullPhoneQuery);
            List<ParseObject> devices = ParseQuery.or(queries).find();

            for(Contact c : DataStorageHandler.AllContacts.values()) {
                boolean hasFlare = false;
                for (PhoneNumber phone : c.allPhoneNumbers) {
                    boolean isSavedAsHasFlare = DataStorageHandler.doesNumberHaveFlare(phone.number);
                    boolean result = false;
                    for (ParseObject item : devices) {
                        if (item.getString("FullPhone").contains(phone.number)) {
                            result = true;
                            break;
                        }
                    }
                    if (result) {
                        hasFlare = true;
                        phone.hasFlare = true;
                        if (!isSavedAsHasFlare) {
                            DataStorageHandler.saveContactNumbersWithFlare(phone);
                        }
                    } else {
                        phone.hasFlare = false;
                        if (isSavedAsHasFlare) {
                            DataStorageHandler.deleteContactNumbersWithFlare(phone);
                        }
                    }
                }
                c.hasFlare = hasFlare;
            }

        } catch (Exception ex) {
            Log.e("Find Flare Users",ex.getMessage());
        }

        return null;
    }
}
