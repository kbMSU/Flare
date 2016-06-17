package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
import flaregradle.myapp.com.Flare.Events.FindFlareError;
import flaregradle.myapp.com.Flare.Events.FindFlareSuccess;
import flaregradle.myapp.com.Flare.Interfaces.ICallBack;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;


public class FindFlareUsersTask extends AsyncTask<Context,Void,Void> {
    private Exception _exception;
    private Context _context;

    @Override
    protected Void doInBackground(Context... params) {
        _context = params[0];
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
                    } else {
                        phone.hasFlare = false;
                    }
                }
                c.hasFlare = hasFlare;
            }

        } catch (Exception ex) {
            Log.e("Find Flare Users",ex.getMessage());
            _exception = ex;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(_exception == null) {
            EventsModule.Post(new FindFlareSuccess());
        } else {
            EventsModule.Post(new FindFlareError(_exception));
        }
    }
}
