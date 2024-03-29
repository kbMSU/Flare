package flaregradle.myapp.com.Flare.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.MyApp.Flare.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.parse.ParseObject;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import flaregradle.myapp.com.Flare.Adapters.ContactsAdapter;
import flaregradle.myapp.com.Flare.Adapters.PhoneNumberAdapter;
import flaregradle.myapp.com.Flare.AsyncTasks.FindFlareUsersTask;
import flaregradle.myapp.com.Flare.AsyncTasks.SendFlareAsyncTask;
import flaregradle.myapp.com.Flare.AsyncTasks.SendTwilioSmsTask;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.Group;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
import flaregradle.myapp.com.Flare.Dialogs.AlternateFlareOptionsDialog;
import flaregradle.myapp.com.Flare.Events.FindFlareError;
import flaregradle.myapp.com.Flare.Events.FindFlareSuccess;
import flaregradle.myapp.com.Flare.Interfaces.ISendFlare;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;
import flaregradle.myapp.com.Flare.Utilities.PermissionHandler;


public class SendFlareActivity extends AppCompatActivity implements ISendFlare {

    private Toast _message;
    private ArrayList<Contact> _sortedContacts;
    private String _latitude;
    private String _longitude;
    private ArrayAdapter _contactAdapter;
    private InterstitialAd _interstitialAd;
    private String _groupName;
    private Contact _currentlyEditingContact;
    private Activity _thisActivity;
    private AlertDialog _findingFriendsWithFlareAlert;
    private AlertDialog _errorFindingFriendsWithFlareAlert;

    @Bind(R.id.contactsHome) ListView _contactsView;
    @Bind(R.id.writeMessage) EditText _flareMessage;
    @Bind(R.id.phone) EditText _phoneText;
    @Bind(R.id.busyIndicator) ProgressBar _busyIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_flare);
        ButterKnife.bind(this);
        _thisActivity = this;

        // Get the location from the bundle
        Bundle extras = getIntent().getExtras();
        _latitude = extras.getString("latitude");
        _longitude = extras.getString("longitude");

        // Set up the toast message
        _message = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        // Set up the contacts view
        DataStorageHandler.getInstance();
        _sortedContacts = new ArrayList<>(DataStorageHandler.AllContacts.values());
        _contactAdapter = new ContactsAdapter(this,com.MyApp.Flare.R.layout.contact_item_view,_sortedContacts,false);
        _contactsView.setAdapter(_contactAdapter);
        _contactsView.setDivider(null);

        // Set up the listener for text
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
                    for (Contact c : DataStorageHandler.AllContacts.values()){
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

                if (!contact.selected) {
                    if (contact.allPhoneNumbers != null && contact.allPhoneNumbers.size() > 1) {
                        _currentlyEditingContact = contact;
                        showSelectNumber(contact.allPhoneNumbers);
                    } else {
                        DataStorageHandler.SelectedContacts.add(contact);
                        contact.selected = true;
                    }
                } else {
                    DataStorageHandler.SelectedContacts.remove(contact);
                    contact.selected = false;
                }

                _contactAdapter.notifyDataSetChanged();
            }
        });

        // Load the progress bar
        _busyIndicator.setVisibility(View.GONE);

        if(!DataStorageHandler.HavePurchasedAdFreeUpgrade()) {
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

        _findingFriendsWithFlareAlert = new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("Finding friends with flare ...")
                .create();

        _errorFindingFriendsWithFlareAlert = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Something went wrong, we could not find your friends with flare")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        if (!DataStorageHandler.HaveAskedToCheckContactsWithFlare()) {
            AlertDialog alert = new AlertDialog.Builder(this)
                    .setTitle("")
                    .setMessage("We can see if any of your friends have flare. We need to check your contacts phone numbers against our " +
                            "cloud to do this. Do we have your permission ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataStorageHandler.SetCanCheckContactsForFlare(true);
                            _findingFriendsWithFlareAlert.show();
                            new FindFlareUsersTask().execute(_thisActivity);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataStorageHandler.SetCanCheckContactsForFlare(false);
                        }
                    })
                    .create();
            alert.show();
            DataStorageHandler.SetHaveAskedToCheckContactsWithFlare(true);
        }
    }

    private void requestNewInterstitial() {
        AdRequest.Builder adRequest = new AdRequest.Builder();
        if(DataStorageHandler.CurrentLocation != null)
            adRequest.setLocation(DataStorageHandler.CurrentLocation);
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
                DataStorageHandler.SelectedContacts.add(_currentlyEditingContact);
                _currentlyEditingContact.selected = true;
                _contactAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        alert.show();
    }

    @Override
    protected void onStop() {
        clearSelectedContacts();
        super.onStop();
    }

    @Override
    protected void onPause() {
        EventsModule.UnRegister(this);
        clearSelectedContacts();
        super.onPause();
    }

    @Override
    protected void onResume() {
        EventsModule.Register(this);
        super.onResume();
    }

    @Subscribe
    public void FindFlareSuccess(FindFlareSuccess success) {
        _findingFriendsWithFlareAlert.cancel();
        _sortedContacts = new ArrayList<>(DataStorageHandler.AllContacts.values());
        _contactAdapter.notifyDataSetChanged();
    }

    @Subscribe public void FindFlareError(FindFlareError error) {
        _findingFriendsWithFlareAlert.cancel();
        _errorFindingFriendsWithFlareAlert.show();
    }

    private void resetSortedContacts() {
        _sortedContacts.clear();
        for(Contact c : DataStorageHandler.AllContacts.values()) {
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
                if(DataStorageHandler.SelectedContacts.size() < 1){
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
                if (value.isEmpty()) {
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
        newContactGroup.Contacts = new ArrayList<>(DataStorageHandler.SelectedContacts);
        DataStorageHandler.saveContactGroup(newContactGroup);

        clearSelectedContacts();
    }

    public void onSendClick(View v) {
        if (!DataStorageHandler.CanSendCloudMessage() && !PermissionHandler.canSendSms(this)) {
            DataStorageHandler.SetSendCloudMessage(true);
        }

        if(DataStorageHandler.SelectedContacts == null || DataStorageHandler.SelectedContacts.size() == 0){
            showMessage("You have not selected any contacts");
            return;
        }

        String text = _flareMessage.getText().toString();
        if(text.equals(""))
            text = "Flare";

        ArrayList<PhoneNumber> contactsWithFlare = new ArrayList<>();
        ArrayList<PhoneNumber> contactsWithoutFlare = new ArrayList<>();

        for(Contact phone : DataStorageHandler.SelectedContacts) {
            if(phone.phoneNumber.hasFlare) {
                contactsWithFlare.add(phone.phoneNumber);
            } else {
                contactsWithoutFlare.add(phone.phoneNumber);
            }
        }

        if(contactsWithFlare.size() > 0)
            new SendFlareAsyncTask(_latitude,_longitude,text,contactsWithFlare).execute(getApplicationContext());

        if(contactsWithoutFlare.size() > 0) {
            String body = text+" http://maps.google.com/?q="+_latitude+","+_longitude+"  "+"Sent from Flare";

            if(DataStorageHandler.CanSendCloudMessage()) {
                List<String> numbers = new ArrayList<>();
                for(PhoneNumber phone : contactsWithoutFlare) {
                    numbers.add(phone.number);
                }
                new SendTwilioSmsTask(this,numbers,body).execute();
            } else {
                for(PhoneNumber phone : contactsWithoutFlare) {
                    SmsManager m = SmsManager.getDefault();
                    m.sendTextMessage(phone.number,DataStorageHandler.getCountryCode()+DataStorageHandler.getPhoneNumber(),body,null,null);
                }
            }
        }

        showFullPageAd();
        clearSelectedContacts();
        showMessage("Message sent");
    }

    private void showMessage(String text){
        _message.setText(text);
        _message.show();
    }

    private void clearSelectedContacts() {
        for(Contact c : DataStorageHandler.SelectedContacts) {
            c.selected = false;
        }
        DataStorageHandler.SelectedContacts.clear();
        _contactAdapter.notifyDataSetChanged();
    }

    public void showFullPageAd() {
        if(DataStorageHandler.HavePurchasedAdFreeUpgrade())
            return;

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
