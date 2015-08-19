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
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
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
            Query query = null;

            for(Contact c : DataStorageHandler.AllContacts.values()) {
                for(PhoneNumber phone : c.allPhoneNumbers) {
                    String number = phone.number;
                    if(query == null) {
                        query = devicesTable.where().indexOf("FullPhone",number).ne(-1);
                    } else {
                        query.or().indexOf("FullPhone",number).ne(-1);
                    }
                }
            }

            if(query != null) {
                MobileServiceList<DeviceItem> items = devicesTable.execute(query).get();
                for(Contact c : DataStorageHandler.AllContacts.values()) {
                    boolean hasFlare = false;
                    for(PhoneNumber phone : c.allPhoneNumbers) {
                        boolean isSavedAsHasFlare =  DataStorageHandler.doesNumberHaveFlare(phone.number);
                        boolean result = false;
                        for (DeviceItem item : items) {
                            if(item.fullPhone.contains(phone.number)) {
                                result = true;
                                break;
                            }
                        }
                        if(result) {
                            hasFlare = true;
                            phone.hasFlare = true;
                            if(!isSavedAsHasFlare) {
                                DataStorageHandler.saveContactNumbersWithFlare(phone);
                            }
                        } else {
                            phone.hasFlare = false;
                            if(isSavedAsHasFlare) {
                                DataStorageHandler.deleteContactNumbersWithFlare(phone);
                            }
                        }
                    }
                    c.hasFlare = hasFlare;
                }
            }

        } catch (Exception ex) {
            Log.e("Find Flare Users",ex.getMessage());
        }


        return null;
    }
}
