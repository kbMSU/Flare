package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.List;

import flaregradle.myapp.com.Flare.BackendItems.DeviceItem;
import flaregradle.myapp.com.Flare.SendFlareActivity;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class SendFlareAsyncTask extends AsyncTask<Context,Void,String> {
    private Context context;

    private String _latitude;
    private String _longitude;
    private String _message;

    private ArrayList<String> _contactNumbers;

    private ProgressBar _spinner;
    private RelativeLayout _screen;

    private String website = "http://flarebackend.azurewebsites.net";

    private SendFlareActivity _sendFlareActivity;

    public SendFlareAsyncTask(SendFlareActivity activity,
                              RelativeLayout screen,
                              ProgressBar spinner,
                              ArrayList<String> phones,
                              String latitude,
                              String longitude,
                              String message){
        _latitude = latitude;
        _longitude = longitude;
        _message = message;
        _contactNumbers = phones;
        _sendFlareActivity = activity;
        _spinner = spinner;
        _screen = screen;
        _spinner.setVisibility(View.VISIBLE);
        _screen.setAlpha((float)0.3);
        _screen.setClickable(false);
    }

    @Override
    protected String doInBackground(Context... params) {

        context = params[0];
        String msg = "";

        try {
            MobileServiceClient client = new MobileServiceClient(
                    "https://flareservice.azure-mobile.net/",
                    "vAymygcCyvnOQrDzLOEjyOQGIxIJMm78",
                    context);
            MobileServiceTable<DeviceItem> devices = client.getTable(DeviceItem.class);

            String thisPhone = DataStorageHandler.getInstance().thisPhone;
            // Split the phone number
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

            for(String _phoneNumber : _contactNumbers) {
                    // Get the actual number
                    String finalPhone = "";
                    for(int i=_phoneNumber.length()-1; i>=0; i--)
                    {
                        Character c = _phoneNumber.charAt(i);
                        if(Character.isDigit(c) && finalPhone.length() < 10)
                            finalPhone+=c;
                    }
                    String reverse = new StringBuilder(finalPhone).reverse().toString();

                    MobileServiceList<DeviceItem> phoneItems = devices.where().indexOf("FullPhone",reverse).ne(-1).execute().get();
                    if(phoneItems.size() == 0){
                        SmsManager m = SmsManager.getDefault();
                        m.sendTextMessage(_phoneNumber,null,_message+" http://maps.google.com/?q="+_latitude+","+_longitude+" "
                                +"  "+"Sent from Flare",null,null);
                    } else {
                        try {
                            /*HttpUriRequest request = new HttpPost(website+"/api/notifications");
                            request.addHeader("text",_message);
                            request.addHeader("phone",phone);
                            request.addHeader("latitude",_latitude);
                            request.addHeader("longitude",_longitude);
                            request.addHeader("to",phoneItems.get(0).fullPhone);
                            HttpResponse response = new DefaultHttpClient().execute(request);*/

                            List<Pair<String,String>> parameters = new ArrayList<>();
                            parameters.add(new Pair<>("text",_message));
                            parameters.add(new Pair<>("phone",phone));
                            parameters.add(new Pair<>("latitude",_latitude));
                            parameters.add(new Pair<>("longitude",_longitude));
                            parameters.add(new Pair<>("to",phoneItems.get(0).fullPhone));

                            client.invokeApi("Notifications","Post",parameters);

                        } catch (Exception e) {
                            Log.e("MainActivity", "Failed to send notification - " + e.getMessage());
                        }
                    }
                    msg = "Message Sent";
            }

        } catch (Exception e) {
            Log.e("SendFlare", "Failed to send notification - " + e.getMessage());
            msg = "Failed to send flare";
        }

        return msg;
    }

    @Override
    protected void onPostExecute(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        _spinner.setVisibility(View.GONE);
        _screen.setAlpha(1);
        _screen.setClickable(true);
        _sendFlareActivity.showFullPageAd();
    }
}
