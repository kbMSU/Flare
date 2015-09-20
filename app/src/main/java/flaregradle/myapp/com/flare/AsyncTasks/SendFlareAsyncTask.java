package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.parse.ParseCloud;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import flaregradle.myapp.com.Flare.BackendItems.DeviceItem;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
import flaregradle.myapp.com.Flare.Interfaces.ISendFlare;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class SendFlareAsyncTask extends AsyncTask<Context,Void,String> {
    private Context context;

    private String _latitude;
    private String _longitude;
    private String _message;
    private ArrayList<PhoneNumber> _contactNumbers;

    public SendFlareAsyncTask(String latitude,
                              String longitude,
                              String message,
                              ArrayList<PhoneNumber> numbers){
        _latitude = latitude;
        _longitude = longitude;
        _message = message;
        _contactNumbers = numbers;
    }

    @Override
    protected String doInBackground(Context... params) {

        context = params[0];
        String msg = "";

        try {

            /*MobileServiceClient client = new MobileServiceClient(
                    "https://flareservice.azure-mobile.net/",
                    "vAymygcCyvnOQrDzLOEjyOQGIxIJMm78",
                    context);
            MobileServiceTable<DeviceItem> devices = client.getTable(DeviceItem.class);*/

            String code = DataStorageHandler.getCountryCode();
            String phone = DataStorageHandler.getPhoneNumber();

            for(PhoneNumber _phoneNumber : _contactNumbers) {
                String number = _phoneNumber.number;

                /*try {
                    //MobileServiceList<DeviceItem> phoneItems = devices.where().indexOf("FullPhone",reverse).ne(-1).execute().get();
                    ParseObject phoneItem = ParseQuery.getQuery("Device").whereContains("FullPhone",number).getFirst();
                    if(phoneItem != null) {
                        try {
                            List<Pair<String,String>> parameters = new ArrayList<>();
                            parameters.add(new Pair<>("text",_message));
                            parameters.add(new Pair<>("phone",phone));
                            parameters.add(new Pair<>("latitude",_latitude));
                            parameters.add(new Pair<>("longitude",_longitude));
                            parameters.add(new Pair<>("to", phoneItem.getString("FullPhone")));

                            client.invokeApi("Notifications","Post",parameters);

                        } catch (Exception e) {
                            Log.e("SEND_FLARE_ASYNC_TASK", "Failed to invoke api : " + e.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    Log.e("SEND_FLARE_ASYNC_TASK", "Failed to get user : " + ex.getMessage());
                }*/

                HashMap<String, String> pushParams = new HashMap<>();
                pushParams.put("text", _message);
                pushParams.put("phone", phone);
                pushParams.put("latitude",_latitude);
                pushParams.put("longitude",_longitude);
                pushParams.put("to",number);
                ParseCloud.callFunction("SendFlare", pushParams);
            }

        } catch (Exception e) {
            Log.e("SEND_FLARE_ASYNC_TASK", "Failed to send notification - " + e.getMessage());
        }

        return msg;
    }
}
