package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.TreeMap;

import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.Utilities.ContactsHandler;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;
import flaregradle.myapp.com.Flare.LoadScreen;

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
        DataStorageHandler _dataStore = DataStorageHandler.getInstance();
        if(_dataStore.AllContacts == null || _dataStore.AllContacts.size() == 0){
            TreeMap<String,Contact> contacts = _contactsHandler.getContacts();
            _dataStore.AllContacts = contacts;
        }
        //Collections.sort(_dataStore.AllContacts);
        if(_dataStore.SelectedContacts == null) _dataStore.SelectedContacts = new ArrayList<Contact>();
        _dataStore.setupPreferences();

        return "success";
    }

    @Override
    protected void onPostExecute(String msg) {
        _parent.RegisterDevice();
    }
}
