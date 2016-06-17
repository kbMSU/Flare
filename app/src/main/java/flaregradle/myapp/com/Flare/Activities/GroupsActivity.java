package flaregradle.myapp.com.Flare.Activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.MyApp.Flare.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import flaregradle.myapp.com.Flare.Adapters.GroupsAdapter;
import flaregradle.myapp.com.Flare.AsyncTasks.SetUpContactsTask;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.Group;
import flaregradle.myapp.com.Flare.Events.FindContactsFailure;
import flaregradle.myapp.com.Flare.Events.FindContactsSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.ContactsHandler;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;
import flaregradle.myapp.com.Flare.Utilities.PermissionHandler;


public class GroupsActivity extends AppCompatActivity {

    @Bind(R.id.groups_list)
    ListView _groupsListView;
    @Bind(R.id.groupsAdView)
    AdView mAdView;

    private ArrayAdapter _groupsAdapter;
    private ActionMode mActionMode;
    private Group _currentGroup;
    private ArrayList<Group> _groups;
    private ActionBar _actionBar;
    private AlertDialog _gettingContactsDialog;
    private AlertDialog _failedToGetContactsDialog;
    private ContactsHandler _contactsHandler;
    private boolean _newGroup;
    private boolean _sendingFlare;

    final int READ_CONTACTS_REQUEST = 1;
    final int LOCATION_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);

        // Setup contacts handler
        ContentResolver _contentResolver = getContentResolver();
        _contactsHandler = new ContactsHandler(_contentResolver);

        // Setup the groups view
        _groups = new ArrayList<>(DataStorageHandler.SavedContactGroups.values());
        _groupsAdapter = new GroupsAdapter(this, R.layout.group_item_view, _groups);
        _groupsListView.setAdapter(_groupsAdapter);
        _groupsListView.setDivider(null);

        // Get action bar
        _actionBar = getSupportActionBar();

        // Open the groups context menu
        final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.groups_context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.editGroup:
                        editGroup();

                        _actionBar.show();
                        mode.finish();
                        return true;

                    case R.id.deleteGroup:
                        deleteGroup();

                        _actionBar.show();
                        mode.finish();
                        return true;

                    case R.id.sendGroupFlare:
                        sendGroupFlare();

                        _actionBar.show();
                        mode.finish();
                        return true;

                    default:
                        _actionBar.show();
                        mode.finish();
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                _actionBar.show();
                mActionMode = null;
            }
        };

        _groupsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                _currentGroup = (Group) itemAtPosition;

                if (mActionMode != null)
                    return false;

                startSupportActionMode(mActionModeCallback);
                _actionBar.hide();
                view.setSelected(true);

                return true;
            }
        });

        if (!DataStorageHandler.HavePurchasedAdFreeUpgrade()) {
            AdRequest.Builder adRequest = new AdRequest.Builder();
            if (DataStorageHandler.CurrentLocation != null)
                adRequest.setLocation(DataStorageHandler.CurrentLocation);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mAdView.setVisibility(View.VISIBLE);
                }
            });
            mAdView.loadAd(adRequest.build());
        }

        _gettingContactsDialog = new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("Getting Contacts ...")
                .create();

        _failedToGetContactsDialog = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("We could not get your contacts")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventsModule.UnRegister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventsModule.Register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_groups, menu);
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
                createNewGroup();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_CONTACTS_REQUEST:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContacts();
                }

            case LOCATION_REQUEST:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendGroupFlare();
                }
                break;
        }
    }

    private void deleteGroup() {
        _groups.remove(_currentGroup);
        DataStorageHandler.deleteContactGroup(_currentGroup.Name);
        _groupsAdapter.notifyDataSetChanged();
    }

    private void sendGroupFlare() {
        _sendingFlare = true;

        if (!PermissionHandler.canRetrieveLocation(this)) {
            PermissionHandler.requestLocationPermission(this, LOCATION_REQUEST);
            return;
        }

        if (!PermissionHandler.canRetrieveContacts(this)) {
            setUpContacts();
            return;
        }

        for (Contact c : _currentGroup.Contacts) {
            Contact contact = DataStorageHandler.AllContacts.get(c.name);
            contact.selected = true;
            DataStorageHandler.SelectedContacts.add(contact);
        }

        Location location;
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(new Criteria(), false);
            location = locationManager.getLastKnownLocation(provider);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),"Could not get your location , please try when location is available",Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            Intent sendFlareIntent = new Intent(this,SendFlareActivity.class);
            sendFlareIntent.putExtra("latitude",String.valueOf(location.getLatitude()));
            sendFlareIntent.putExtra("longitude",String.valueOf(location.getLongitude()));
            startActivity(sendFlareIntent);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),"Uh oh, something went wrong. Try again please",Toast.LENGTH_SHORT).show();
        }
    }

    private void editGroup() {
        _newGroup = false;

        if (!PermissionHandler.canRetrieveContacts(this)) {
            setUpContacts();
            return;
        }

        for(Contact c : _currentGroup.Contacts){
            Contact contact = DataStorageHandler.AllContacts.get(c.name);
            contact.selected = true;
            DataStorageHandler.SelectedContacts.add(contact);
        }

        Intent intent = new Intent(this,CreateGroupActivity.class);
        intent.putExtra("groupName",_currentGroup.Name);
        startActivity(intent);
    }

    private void createNewGroup() {
        _newGroup = true;

        if (!PermissionHandler.canRetrieveContacts(this)) {
            setUpContacts();
            return;
        }

        Intent intent = new Intent(this,CreateGroupActivity.class);
        startActivity(intent);
    }

    private void setUpContacts() {
        PermissionHandler.requestContactsPermission(this,READ_CONTACTS_REQUEST);
    }

    private void getContacts() {
        _gettingContactsDialog.show();
        new SetUpContactsTask(_contactsHandler).execute(getApplicationContext());
    }

    @Subscribe public void FoundContacts(FindContactsSuccess success) {
        _gettingContactsDialog.cancel();

        if (_sendingFlare) {
            _sendingFlare = false;
            sendGroupFlare();
        }
        else if (_newGroup)
            createNewGroup();
        else
            editGroup();
    }

    @Subscribe public void ErrorFindingContacts(FindContactsFailure failure) {
        _gettingContactsDialog.cancel();
        _failedToGetContactsDialog.show();
    }
}
