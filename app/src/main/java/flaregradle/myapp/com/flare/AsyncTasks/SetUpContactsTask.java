package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import flaregradle.myapp.com.Flare.Delegates.ContactsDelegate;
import flaregradle.myapp.com.Flare.Events.FindContactsFailure;
import flaregradle.myapp.com.Flare.Events.FindContactsSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.ContactsHandler;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;
import flaregradle.myapp.com.Flare.Activities.LoadScreen;

public class SetUpContactsTask extends AsyncTask<Context, Void, String> {

    private ContactsHandler _contactsHandler;
    private Context _context;

    public SetUpContactsTask(ContactsHandler handler) {
        _contactsHandler = handler;
    }

    @Override
    protected String doInBackground(Context... params) {
        _context = params[0];

        try {
            if(DataStorageHandler.AllContacts == null || DataStorageHandler.AllContacts.size() == 0){
                DataStorageHandler.AllContacts = _contactsHandler.getContacts();
            }
            if(DataStorageHandler.SelectedContacts == null) DataStorageHandler.SelectedContacts = new ArrayList<>();

            return "success";
        } catch (Exception ex){
            Log.e("contacts",ex.getMessage());
            return "failure";
        }
    }

    @Override
    protected void onPostExecute(String msg) {
        if(msg.equals("success")) {
            EventsModule.Post(new FindContactsSuccess());
        } else {
            EventsModule.Post(new FindContactsFailure());
        }
    }
}
