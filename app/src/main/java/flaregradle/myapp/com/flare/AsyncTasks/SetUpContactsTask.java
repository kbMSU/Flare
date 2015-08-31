package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

import flaregradle.myapp.com.Flare.Utilities.ContactsHandler;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;
import flaregradle.myapp.com.Flare.Activities.LoadScreen;

public class SetUpContactsTask extends AsyncTask<Context, Void, String> {

    private LoadScreen _parent;
    private ContactsHandler _contactsHandler;

    public SetUpContactsTask(LoadScreen parent, ContactsHandler handler) {
        _parent = parent;
        _contactsHandler = handler;
    }

    @Override
    protected String doInBackground(Context... params) {
        // Set up the contacts
        if(DataStorageHandler.AllContacts == null || DataStorageHandler.AllContacts.size() == 0){
            DataStorageHandler.AllContacts = _contactsHandler.getContacts();
        }
        if(DataStorageHandler.SelectedContacts == null) DataStorageHandler.SelectedContacts = new ArrayList<>();

        return "success";
    }

    @Override
    protected void onPostExecute(String msg) {
        _parent.finishedGettingContacts();
    }
}
