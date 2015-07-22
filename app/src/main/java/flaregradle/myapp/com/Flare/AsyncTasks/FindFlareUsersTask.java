package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.ExecutableQuery;
import com.microsoft.windowsazure.mobileservices.table.query.Query;

import flaregradle.myapp.com.Flare.BackendItems.DeviceItem;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;


public class FindFlareUsersTask extends AsyncTask<Context,Void,Void> {

    @Override
    protected Void doInBackground(Context... params) {

        Context context = params[0];

        try {
            MobileServiceClient client = new MobileServiceClient(
                    "https://flareservice.azure-mobile.net/",
                    "vAymygcCyvnOQrDzLOEjyOQGIxIJMm78",
                    context);

            MobileServiceTable<DeviceItem> devicesTable = client.getTable(DeviceItem.class);

            for(Contact c : DataStorageHandler.getInstance().AllContacts.values()) {
                boolean hasFound = false;
                for(String phone : c.allPhoneNumbers) {
                    if(hasFound)
                        continue;

                    MobileServiceList<DeviceItem> phoneItems = devicesTable.where().indexOf("FullPhone",phone).ne(-1).execute().get();

                    if(phoneItems.size() > 0) {
                        c.hasFlare = true;
                        hasFound = true;
                    }
                }
            }

        } catch (Exception ex) {
            Log.e("Find Flare Users",ex.getMessage());
        }


        return null;
    }
}
