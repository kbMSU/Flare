package flaregradle.myapp.com.Flare.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.MyApp.Flare.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import flaregradle.myapp.com.Flare.Adapters.ContactsAdapter;
import flaregradle.myapp.com.Flare.Adapters.PhoneNumberAdapter;
import flaregradle.myapp.com.Flare.AsyncTasks.FindFlareUsersTask;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.Group;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
import flaregradle.myapp.com.Flare.Events.FindFlareError;
import flaregradle.myapp.com.Flare.Events.FindFlareSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;


public class CreateGroupActivity extends AppCompatActivity {

    private Toast _message;
    private ArrayList<Contact> _sortedContacts;
    private ArrayAdapter _contactAdapter;
    private String _groupName;
    private Contact _currentlyEditingContact;
    private AlertDialog _findingFriendsWithFlareAlert;
    private AlertDialog _errorFindingFriendsWithFlareAlert;
    private Activity _thisActivity;

    @Bind(R.id.contactsHome) ListView _contactsView;
    @Bind(R.id.phone) EditText _phoneText;
    @Bind(R.id.groupsAdView) AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);
        _thisActivity = this;

        // If edit group , get the group name
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            _groupName = extras.getString("groupName");
        }

        // Set up the toast message
        _message = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        // Set up the contacts view
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

                if (charSequence == null || charSequence.length() == 0) {
                    resetSortedContacts();
                } else {
                    for (Contact c : DataStorageHandler.AllContacts.values()) {
                        if (c.name.toLowerCase().contains(charSequence.toString().toLowerCase())) {
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
                        DataStorageHandler.SelectedContacts.add(contact);
                        contact.selected = true;
                    }
                }
                else {
                    DataStorageHandler.SelectedContacts.remove(contact);
                    contact.selected = false;
                }

                _contactAdapter.notifyDataSetChanged();
            }
        });

        if(!DataStorageHandler.HavePurchasedAdFreeUpgrade()) {
            // Setup the ad
            AdRequest.Builder adRequest = new AdRequest.Builder();
            if(DataStorageHandler.CurrentLocation != null)
                adRequest.setLocation(DataStorageHandler.CurrentLocation);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mAdView.setVisibility(View.VISIBLE);
                }
            });
            mAdView.loadAd(adRequest.build());
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

    @Subscribe public void FindFlareSuccess(FindFlareSuccess success) {
        _findingFriendsWithFlareAlert.cancel();
        _sortedContacts = new ArrayList<>(DataStorageHandler.AllContacts.values());
        _contactAdapter.notifyDataSetChanged();
    }

    @Subscribe public void FindFlareError(FindFlareError error) {
        _findingFriendsWithFlareAlert.cancel();
        _errorFindingFriendsWithFlareAlert.show();
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

    private void resetSortedContacts() {
        _sortedContacts.clear();
        for(Contact c : DataStorageHandler.AllContacts.values()) {
            _sortedContacts.add(c);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.saveGroup:
                onSaveGroupClick();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSaveGroupClick() {
        if(DataStorageHandler.SelectedContacts.size() < 1){
            showMessage("Please select contacts to save");
            return;
        }
        showSaveGroupOverlay();
    }

    private void createGroup() {
        Group newContactGroup = new Group();
        newContactGroup.Name = _groupName;
        newContactGroup.Contacts = new ArrayList<>(DataStorageHandler.SelectedContacts);
        DataStorageHandler.saveContactGroup(newContactGroup);

        clearSelectedContacts();
        NavUtils.navigateUpFromSameTask(this);
    }

    private void showSaveGroupOverlay() {
        final EditText input = new EditText(this);
        input.setText(_groupName);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Save group");
        alert.setMessage("Enter a group name");
        alert.setView(input);
        alert.setPositiveButton("Save",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                if(value.isEmpty()) {
                    showMessage("Please enter a name for this group");
                    return;
                }
                _groupName = value;
                createGroup();
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
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
}
