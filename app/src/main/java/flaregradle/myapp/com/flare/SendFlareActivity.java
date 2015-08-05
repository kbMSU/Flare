package flaregradle.myapp.com.Flare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
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

import com.MyApp.Flare.BuildConfig;
import com.MyApp.Flare.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import flaregradle.myapp.com.Flare.Adapters.ContactsAdapter;
import flaregradle.myapp.com.Flare.Adapters.PhoneNumberAdapter;
import flaregradle.myapp.com.Flare.AsyncTasks.SendFlareAsyncTask;
import flaregradle.myapp.com.Flare.BackendItems.DeviceItem;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.Group;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
import flaregradle.myapp.com.Flare.Dialogs.AlternateFlareOptionsDialog;
import flaregradle.myapp.com.Flare.Interfaces.ICallBack;
import flaregradle.myapp.com.Flare.Interfaces.ISendFlare;
import flaregradle.myapp.com.Flare.Runnables.SendFlare;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;


public class SendFlareActivity extends AppCompatActivity implements ISendFlare {

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
                        } else if (c.phoneNumber.Contains(charSequence.toString())) {
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
                    contact.selected = true;
                }
            }
            else {
                _dataStore.SelectedContacts.remove(contact);
                contact.selected = false;
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

    private void showSelectNumber(List<PhoneNumber> numbers) {
        final List<PhoneNumber> finalNumbers = numbers;
        final ListView view = new ListView(this);
        view.setDivider(null);
        view.setPadding(5,5,5,5);
        final ArrayAdapter<PhoneNumber> numbersAdapter = new PhoneNumberAdapter(this, R.layout.number_view, finalNumbers);
        view.setAdapter(numbersAdapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                PhoneNumber number = (PhoneNumber)itemAtPosition;
                number.isSelected = true;
                for(PhoneNumber otherNumber : finalNumbers) {
                    if(!otherNumber.number.equals(number.number) && otherNumber.isSelected)
                        otherNumber.isSelected = false;
                }

                _currentlyEditingContact.phoneNumber = number;

                numbersAdapter.notifyDataSetChanged();
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
        super.onPause();
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
        alert.setTitle("Save group");
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
        if(_dataStore.SelectedContacts == null || _dataStore.SelectedContacts.size() == 0){
            showMessage("You have not selected any contacts");
            return;
        }

        String text = _flareMessage.getText().toString();
        if(text == null || text.equals(""))
            text = "Flare";

        ArrayList<PhoneNumber> contactsWithFlare = new ArrayList<>();
        ArrayList<PhoneNumber> contactsWithoutFlare = new ArrayList<>();

        for(Contact phone : _dataStore.SelectedContacts) {
            if(!_dataStore.Registered) {
                contactsWithFlare.add(phone.phoneNumber);
                continue;
            }

            if(phone.phoneNumber.hasFlare) {
                contactsWithFlare.add(phone.phoneNumber);
            } else {
                contactsWithoutFlare.add(phone.phoneNumber);
            }
        }

        if(contactsWithFlare.size() > 0)
            new SendFlareAsyncTask(this,_latitude,_longitude,text,contactsWithFlare).execute(this);

        if(contactsWithoutFlare.size() > 0) {
            AlternateFlareOptionsDialog dialog = new AlternateFlareOptionsDialog(this);
            dialog.SetData(_latitude,_longitude,text,this,contactsWithoutFlare);
            dialog.show();
        } else {
            showFullPageAd();
            clearSelectedContacts();
            showMessage("Message sent");
        }
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

    @Override
    public void CallBack() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            showFullPageAd();
            clearSelectedContacts();
            showMessage("Message sent");
            }
        });
    }

    @Override
    public void SendAlternateFlare(ArrayList<PhoneNumber> phoneNumbers, final String message) {
        final ArrayList<PhoneNumber> numbers = phoneNumbers;
        final SendFlareActivity current = this;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AlternateFlareOptionsDialog dialog = new AlternateFlareOptionsDialog(current);
                dialog.SetData(_latitude,_longitude,message,current,numbers);
                dialog.show();
            }
        });
    }
}
