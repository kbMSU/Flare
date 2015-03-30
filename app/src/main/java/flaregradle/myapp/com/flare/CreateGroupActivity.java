package flaregradle.myapp.com.flare;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;

import flaregradle.myapp.com.flare.Adapters.ContactsAdapter;
import flaregradle.myapp.com.flare.DataItems.Contact;
import flaregradle.myapp.com.flare.DataItems.Group;
import flaregradle.myapp.com.flare.Utilities.DataStorageHandler;


public class CreateGroupActivity extends ActionBarActivity {

    private Toast _message;
    private DataStorageHandler _dataStore;
    private ListView _contactsView;
    private ArrayList<Contact> _sortedContacts;
    private ArrayAdapter _contactAdapter;
    private EditText _phoneText;
    private RelativeLayout _saveGroupOverlay;
    private FrameLayout _overlay;
    private EditText _groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        setTheToolbar();

        _groupName = (EditText)findViewById(R.id.groupName);

        // If edit group , get the group name
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String groupName = extras.getString("groupName");
            if(groupName != null && !groupName.isEmpty())
                _groupName.setText(groupName);
        }


        // Set up the toast message
        _message = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

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

                contact.selected = !contact.selected;

                if(contact.selected)
                    _dataStore.SelectedContacts.add(contact);
                else
                    _dataStore.SelectedContacts.remove(contact);

                _contactAdapter.notifyDataSetChanged();
            }
        });

        // Load the overlay
        _overlay = (FrameLayout)findViewById(R.id.overlay);
        _overlay.setVisibility(View.GONE);

        // Load the save group overlay
        _saveGroupOverlay = (RelativeLayout)findViewById(R.id.saveGroup);
        _saveGroupOverlay.setVisibility(View.GONE);
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

    private void setTheToolbar() {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onSaveGroupClick() {
        if(_dataStore.SelectedContacts.size() < 1){
            showMessage("Please select contacts to save");
            return;
        }
        showSaveGroupOverlay();
    }

    public void onCreateGroupCancelClick(View v) {
        removeSaveGroupOverlay();
    }

    public void onCreateGroupAddClick(View v) {
        if(_groupName.getText() == null || _groupName.getText().toString().isEmpty()) {
            showMessage("Please enter a name for this group");
            return;
        }

        removeSaveGroupOverlay();

        Group newContactGroup = new Group();
        newContactGroup.Name = _groupName.getText().toString();
        newContactGroup.Contacts = new ArrayList<>(_dataStore.SelectedContacts);
        _dataStore.saveContactGroup(newContactGroup);

        _groupName.setText("");
        clearSelectedContacts();
        NavUtils.navigateUpFromSameTask(this);
    }

    private void removeSaveGroupOverlay() {
        LinearLayout screen = (LinearLayout)findViewById(R.id.sendFlareScreen);
        screen.setAlpha(1);
        screen.setClickable(true);
        _overlay.setVisibility(View.GONE);
        _saveGroupOverlay.setVisibility(View.GONE);
    }

    private void showSaveGroupOverlay() {
        LinearLayout screen = (LinearLayout)findViewById(R.id.sendFlareScreen);
        screen.setAlpha((float)0.3);
        screen.setClickable(false);
        screen.setEnabled(false);
        _overlay.setVisibility(View.INVISIBLE);
        _saveGroupOverlay.setVisibility(View.VISIBLE);
        EditText groupName = (EditText)findViewById(R.id.groupName);
        groupName.requestFocus();
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
}
