package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.util.ArrayList;
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
    private ISendFlare _callBack;

    public SendFlareAsyncTask(ISendFlare callBack,
                              String latitude,
                              String longitude,
                              String message,
                              ArrayList<PhoneNumber> numbers){
        _latitude = latitude;
        _longitude = longitude;
        _message = message;
        _callBack = callBack;
        _contactNumbers = numbers;
    }

    @Override
    protected String doInBackground(Context... params) {

        context = params[0];
        String msg = "";

        try {
            /*ArrayList<PhoneNumber> contactsWithoutFlare = new ArrayList<>();

            if(!DataStorageHandler.getInstance().Registered) {
                for(PhoneNumber number : _contactNumbers) {
                    contactsWithoutFlare.add(number);
                }
                _callBack.SendAlternateFlare(contactsWithoutFlare,_message);
                return "";
            }*/

            MobileServiceClient client = new MobileServiceClient(
                    "https://flareservice.azure-mobile.net/",
                    "vAymygcCyvnOQrDzLOEjyOQGIxIJMm78",
                    context);
            MobileServiceTable<DeviceItem> devices = client.getTable(DeviceItem.class);

            String thisPhone = DataStorageHandler.getInstance().thisPhone;
            String phone = "";
            String code = "";

            for (int i= thisPhone.length()-1 ; i >= 0; i--) {
                if(phone.length() < 10) {
                    phone += thisPhone.charAt(i);
                } else {
                    code += thisPhone.charAt(i);
                }
            }

            code = new StringBuilder(code).reverse().toString();
            phone = new StringBuilder(phone).reverse().toString();

            for(PhoneNumber _phoneNumber : _contactNumbers) {
                String number = _phoneNumber.number;
                String finalPhone = "";
                for(int i=number.length()-1; i>=0; i--)
                {
                    Character ch = number.charAt(i);
                    if(Character.isDigit(ch) && finalPhone.length() < 10)
                        finalPhone+=ch;
                }
                String reverse = new StringBuilder(finalPhone).reverse().toString();

                try {
                    MobileServiceList<DeviceItem> phoneItems = devices.where().indexOf("FullPhone",reverse).ne(-1).execute().get();
                    if(phoneItems.size() > 0) {
                        try {
                            List<Pair<String,String>> parameters = new ArrayList<>();
                            parameters.add(new Pair<>("text",_message));
                            parameters.add(new Pair<>("phone",phone));
                            parameters.add(new Pair<>("latitude",_latitude));
                            parameters.add(new Pair<>("longitude",_longitude));
                            parameters.add(new Pair<>("to", phoneItems.get(0).fullPhone));

                            client.invokeApi("Notifications","Post",parameters);

                        } catch (Exception e) {
                            Log.e("SEND_FLARE_ASYNC_TASK", "Failed to invoke api : " + e.getMessage());
                        }
                        msg = "Message Sent";
                    }
                    //else
                    //    contactsWithoutFlare.add(_phoneNumber);
                } catch (Exception ex) {
                    //contactsWithoutFlare.add(_phoneNumber);
                    Log.e("SEND_FLARE_ASYNC_TASK", "Failed to get user : " + ex.getMessage());
                }
            }

            /*if(contactsWithoutFlare.size() > 0) {
                _callBack.SendAlternateFlare(contactsWithoutFlare,_message);
            } else {
                _callBack.CallBack();
            }*/

        } catch (Exception e) {
            Log.e("SEND_FLARE_ASYNC_TASK", "Failed to send notification - " + e.getMessage());
            msg = "Failed to send flare";
        }

        return msg;
    }

    @Override
    protected void onPostExecute(String msg) {

    }
}
