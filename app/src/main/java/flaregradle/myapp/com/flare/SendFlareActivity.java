package flaregradle.myapp.com.Flare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MyApp.Flare.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import flaregradle.myapp.com.Flare.Adapters.ContactsAdapter;
import flaregradle.myapp.com.Flare.AsyncTasks.SendFlareAsyncTask;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.Group;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;


public class SendFlareActivity extends ActionBarActivity {

    private Toast _message;
    private DataStorageHandler _dataStore;
    private ListView _contactsView;
    private ArrayList<Contact> _sortedContacts;
    private String _latitude;
    private String _longitude;
    private EditText _flareMessage;
    private ArrayAdapter _contactAdapter;
    private EditText _phoneText;
    private ProgressBar _busyIndicator;
    private InterstitialAd _interstitialAd;
    private String _groupName;
    private Contact _currentlyEditingContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_flare);

        // Get the location from the bundle
        Bundle extras = getIntent().getExtras();
        _latitude = extras.getString("latitude");
        _longitude = extras.getString("longitude");

        // Set up the toast message
        _message = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        // Set up the flare message view
        _flareMessage = (EditText)findViewById(R.id.writeMessage);

        // Set up the contacts view
        _dataStore = DataStorageHandler.getInstance();
        _sortedContacts = new ArrayList<>(_dataStore.AllContacts.values());
        _contactsView = (ListView)findViewById(com.MyApp.Flare.R.id.contactsHome);
        _contactAdapter = new ContactsAdapter(this,com.MyApp.Flare.R.layout.contact_item_view,_sortedContacts,false);
        _contactsView.setAdapter(_contactAdapter);
        _contactsView.setDivider(null);

        // Set up the listener for text
        _phoneText = (EditText)findViewById(com.MyApp.Flare.R.id.phone);
        _phoneText.clearFocus();
        _phoneText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                _sortedContacts.clear();

                if(charSequence == null || charSequence.length() == 0){
                    resetSortedContacts();
                }
                else{
                    for (Contact c : _dataStore.AllContacts.values()){
                        if(c.name.toLowerCase().contains(charSequence.toString().toLowerCase())){
                            _sortedContacts.add(c);
                        } else if (c.phoneNumber.contains(charSequence.toString())) {
                            _sortedContacts.add(c);
                        }
                    }
                    Collections.sort(_sortedContacts);
                }

                _contactAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Set up listener for contacts view
        _contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Object itemAtPosition = adapterView.getItemAtPosition(i);
            Contact contact = (Contact) itemAtPosition;

            if(!contact.selected)
            {
                if(contact.allPhoneNumbers != null && contact.allPhoneNumbers.size() > 1) {
                    _currentlyEditingContact = contact;
                    showSelectNumber(contact.allPhoneNumbers);
                }
                else {
                    _dataStore.SelectedContacts.add(contact);
                    contact.selected = !contact.selected;
                }
            }
            else {
                _dataStore.SelectedContacts.remove(contact);
                contact.selected = !contact.selected;
            }

            _contactAdapter.notifyDataSetChanged();
            }
        });

        // Load the progress bar
        _busyIndicator = (ProgressBar)findViewById(R.id.busyIndicator);
        _busyIndicator.setVisibility(View.GONE);

        // Load the ad
        _interstitialAd = new InterstitialAd(this);
        _interstitialAd.setAdUnitId("ca-app-pub-9978131204593610/9323499682");
        _interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                finish();
            }
        });
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest.Builder adRequest = new AdRequest.Builder();
        if(_dataStore.CurrentLocation != null)
            adRequest.setLocation(_dataStore.CurrentLocation);
        _interstitialAd.loadAd(adRequest.build());
    }

    private void showSelectNumber(List<String> numbers) {
        final ListView view = new ListView(this);
        view.setDivider(null);
        view.setPadding(5,5,5,5);
        ArrayAdapter<String> numbersAdapter = new ArrayAdapter<>(this, R.layout.number_view, numbers);
        view.setAdapter(numbersAdapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                String number = (String)itemAtPosition;

                _currentlyEditingContact.phoneNumber = number;
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Select Number");
        alert.setView(view);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _dataStore.SelectedContacts.add(_currentlyEditingContact);
                _currentlyEditingContact.selected = true;
                _contactAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        alert.show();
    }

    @Override
    protected void onStop()
    {
        clearSelectedContacts();
        super.onStop();
    }

    @Override
    protected void onPause()
    {
        clearSelectedContacts();
        super.onStop();
    }

    private void resetSortedContacts() {
        _sortedContacts.clear();
        for(Contact c : _dataStore.AllContacts.values()) {
            _sortedContacts.add(c);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send_flare, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.createGroup:
                if(_dataStore.SelectedContacts.size() < 1){
                    showMessage("Please select contacts to save");
                    return true;
                }
                showSaveGroupOverlay();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSaveGroupOverlay() {
        final EditText input = new EditText(this);
        input.setText(_groupName);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Save Group");
        alert.setMessage("Enter a group name");
        alert.setView(input);
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                if (value == null || value.isEmpty()) {
                    showMessage("Please enter a name for this group");
                    return;
                }
                _groupName = value;
                createGroup();
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _groupName = "";
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void createGroup() {
        Group newContactGroup = new Group();
        newContactGroup.Name = _groupName;
        newContactGroup.Contacts = new ArrayList<>(_dataStore.SelectedContacts);
        _dataStore.saveContactGroup(newContactGroup);

        clearSelectedContacts();
    }

    public void onSendClick(View v) {
        RelativeLayout screen = (RelativeLayout)findViewById(R.id.sendFlareScreen);
        EditText phoneText = (EditText)findViewById(com.MyApp.Flare.R.id.phone);
        String phoneNumber = phoneText.getText().toString();
        ArrayList<String> contactNumbers = new ArrayList<String>();
        if(_dataStore.SelectedContacts == null || _dataStore.SelectedContacts.size() == 0){
            if(phoneNumber == null || phoneNumber.length() < 10){
                showMessage("Select a contact or enter a valid phone number");
                return;
            }

            contactNumbers.add(phoneNumber);
        } else {
            for(Contact c : _dataStore.SelectedContacts)
                contactNumbers.add(c.phoneNumber);
        }

        String text = _flareMessage.getText().toString();
        if(text == null || text.equals(""))
            text = "Flare";
        try {
            new SendFlareAsyncTask(this,screen,_busyIndicator,contactNumbers,_latitude,_longitude,text).execute(this);
        } catch (Exception ex){
            showMessage(ex.getMessage());
            return;
        }

        showFullPageAd();
        clearSelectedContacts();
    }

    private void showMessage(String text){
        _message.setText(text);
        _message.show();
    }

    private void clearSelectedContacts() {
        for(Contact c : _dataStore.SelectedContacts) {
            c.selected = false;
        }
        _dataStore.SelectedContacts.clear();
        _contactAdapter.notifyDataSetChanged();
    }

    public void showFullPageAd() {
        if(_interstitialAd.isLoaded())
            _interstitialAd.show();
    }
}
