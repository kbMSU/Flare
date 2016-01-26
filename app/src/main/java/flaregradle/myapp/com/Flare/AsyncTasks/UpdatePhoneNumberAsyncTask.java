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
    private Exception _exception;

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
            ParseObject savedPhone = query.find().get(0);
            savedPhone.put("FullPhone", _fullPhone);
            savedPhone.put("CountryCode", _countryCode);
            savedPhone.put("Number", _phoneNumber);
            savedPhone.save();

        } catch (Exception ex) {
            _exception = ex;
        }


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if(_exception != null) {
            EventsModule.Post(new ParseError(_exception));
        } else {
            EventsModule.Post(new ParseSaveDataSuccess());
        }

    }
}
