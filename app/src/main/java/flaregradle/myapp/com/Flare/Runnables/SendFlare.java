package flaregradle.myapp.com.Flare.Runnables;

import android.content.Context;
import android.util.Log;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.util.ArrayList;

import flaregradle.myapp.com.Flare.AsyncTasks.SendFlareAsyncTask;
import flaregradle.myapp.com.Flare.BackendItems.DeviceItem;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
import flaregradle.myapp.com.Flare.Dialogs.AlternateFlareOptionsDialog;
import flaregradle.myapp.com.Flare.Interfaces.ICallBack;
import flaregradle.myapp.com.Flare.Interfaces.ISendFlare;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class SendFlare implements Runnable {

    private Context _context;
    private String _latitude;
    private String _longitude;
    private String _message;
    private ISendFlare _callBack;

    public SendFlare(Context context,ISendFlare callBack) {
        _context = context;
        _callBack = callBack;
    }

    public void SetData(String latitude,String longitude,String message) {
        _latitude = latitude;
        _longitude = longitude;
        _message = message;
    }

    @Override
    public void run() {
        ArrayList<DeviceItem> contactNumbers = new ArrayList<>();
        ArrayList<PhoneNumber> contactsWithoutFlare = new ArrayList<>();

        if(!DataStorageHandler.getInstance().Registered) {
            for(Contact c : DataStorageHandler.getInstance().SelectedContacts) {
                contactsWithoutFlare.add(c.phoneNumber);
            }
            _callBack.SendAlternateFlare(contactsWithoutFlare,_message);
            return;
        }

        boolean errorConnectingToServer = false;
        MobileServiceClient client = null;
        MobileServiceTable<DeviceItem> devices = null;

        try {
            client = new MobileServiceClient(
                    "https://flareservice.azure-mobile.net/",
                    "vAymygcCyvnOQrDzLOEjyOQGIxIJMm78",
                    _context);

        } catch (Exception ex) {
            errorConnectingToServer = true;
            Log.e("SEND_FLARE","Error connecting to server : "+ex.getMessage());
        }

        if(!errorConnectingToServer) {
            devices = client.getTable(DeviceItem.class);
        }

        for(Contact c : DataStorageHandler.getInstance().SelectedContacts) {
            if(errorConnectingToServer) {
                contactsWithoutFlare.add(c.phoneNumber);
                continue;
            }

            String _phoneNumber = c.phoneNumber.number;
            String finalPhone = "";
            for(int i=_phoneNumber.length()-1; i>=0; i--)
            {
                Character ch = _phoneNumber.charAt(i);
                if(Character.isDigit(ch) && finalPhone.length() < 10)
                    finalPhone+=ch;
            }
            String reverse = new StringBuilder(finalPhone).reverse().toString();

            try {
                MobileServiceList<DeviceItem> phoneItems = devices.where().indexOf("FullPhone",reverse).ne(-1).execute().get();
                if(phoneItems.size() > 0)
                    contactNumbers.add(phoneItems.get(0));
                else
                    contactsWithoutFlare.add(c.phoneNumber);
            } catch (Exception ex) {
                contactsWithoutFlare.add(c.phoneNumber);
            }
        }

        if(contactNumbers.size() > 0) {
            //new SendFlareAsyncTask(contactNumbers,_latitude,_longitude,_message).execute(_context);
        }

        if(contactsWithoutFlare.size() > 0) {
            _callBack.SendAlternateFlare(contactsWithoutFlare,_message);
        } else {
            _callBack.CallBack();
        }
    }
}
