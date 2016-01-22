package flaregradle.myapp.com.Flare.AsyncTasks;


import android.content.Context;
import android.os.AsyncTask;

import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import flaregradle.myapp.com.Flare.Events.ParseError;
import flaregradle.myapp.com.Flare.Events.ParseSaveDataSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class UpdatePhoneNumberAsyncTask extends AsyncTask<Context,Void,Void> {

    private Context _context;
    private String _countryCode;
    private String _phoneNumber;
    private String _fullPhone;

    public UpdatePhoneNumberAsyncTask(String countryCode,String phoneNumber) {
        _countryCode = countryCode;
        _phoneNumber = phoneNumber;
        _fullPhone = _countryCode+_phoneNumber;
    }

    @Override
    protected Void doInBackground(Context... params) {
        _context = params[0];

        try {
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("CountryCode",_countryCode);
            installation.put("Number",_phoneNumber);
            installation.put("FullPhone",_fullPhone);
            installation.save();

            String oldPhone = DataStorageHandler.getCountryCode()+DataStorageHandler.getPhoneNumber();
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Device").whereEqualTo("FullPhone",oldPhone);
            ParseObject savedPhone = query.getFirst();
            savedPhone.put("FullPhone", _fullPhone);
            savedPhone.put("CountryCode", _countryCode);
            savedPhone.put("Number", _phoneNumber);
            savedPhone.save();

        } catch (Exception ex) {
            EventsModule.Post(new ParseError(ex));
        }

        EventsModule.Post(new ParseSaveDataSuccess());

        return null;
    }
}
