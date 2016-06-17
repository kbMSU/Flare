package flaregradle.myapp.com.Flare.AsyncTasks;


import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
import flaregradle.myapp.com.Flare.Events.DeleteRegistrationError;
import flaregradle.myapp.com.Flare.Events.DeleteRegistrationSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class DeleteRegistrationAsyncTask extends AsyncTask<Context,Void,Void> {

    private Context _context;
    private Exception _exception;

    public DeleteRegistrationAsyncTask() {
    }

    @Override
    protected Void doInBackground(Context... params) {
        _context = params[0];

        try {
            String code = DataStorageHandler.getCountryCode();
            String phone = DataStorageHandler.getPhoneNumber();
            String fullPhone = code + phone;

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Device").whereEqualTo("FullPhone", fullPhone);
            List<ParseObject> savedPhones = query.find();

            if(savedPhones != null && !savedPhones.isEmpty()) {
                ParseObject device = savedPhones.get(0);
                device.delete();
            }

        } catch (Exception ex) {
            _exception = ex;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(_exception == null) {
            DataStorageHandler.SetNotRegistered();

            String code = DataStorageHandler.getCountryCode();
            String number = DataStorageHandler.getPhoneNumber();
            String fullPhone = code+number;
            for(Contact contact : DataStorageHandler.AllContacts.values()) {
                for(PhoneNumber num : contact.allPhoneNumbers) {
                    if(num.number.equals(number) || num.number.equals(fullPhone)) {
                        removeFlareFromContact(contact);
                        break;
                    }
                }
            }

            EventsModule.Post(new DeleteRegistrationSuccess());
        } else {
            Toast.makeText(_context, _exception.getMessage(), Toast.LENGTH_LONG).show();
            EventsModule.Post(new DeleteRegistrationError(_exception));
        }
    }

    private void removeFlareFromContact(Contact contact) {
        contact.hasFlare = false;
        contact.phoneNumber.hasFlare = false;
        for(PhoneNumber phone : contact.allPhoneNumbers) {
            phone.hasFlare = false;
        }
    }
}
