package flaregradle.myapp.com.Flare;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import java.util.ArrayList;

import flaregradle.myapp.com.Flare.Adapters.GroupsAdapter;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.Group;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;


public class GroupsActivity extends ActionBarActivity {

    private DataStorageHandler _dataStore;
    private ListView _groupsListView;
    private ArrayAdapter _groupsAdapter;
    private ActionMode mActionMode;
    private Group _currentGroup;
    private ArrayList<Group> _groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        // Setup the groups view
        _dataStore = DataStorageHandler.getInstance();
        _groupsListView = (ListView)findViewById(R.id.groups_list);
        _groups = new ArrayList<>(_dataStore.SavedContactGroups.values());
        _groupsAdapter = new GroupsAdapter(this,R.layout.group_item_view,_groups);
        _groupsListView.setAdapter(_groupsAdapter);
        _groupsListView.setDivider(null);

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

                        getSupportActionBar().show();
                        mode.finish();
                        return true;

                    case R.id.deleteGroup:
                        deleteGroup();

                        getSupportActionBar().show();
                        mode.finish();
                        return true;

                    case R.id.sendGroupFlare:
                        sendGroupFlare();

                        getSupportActionBar().show();
                        mode.finish();
                        return true;

                    default:
                        getSupportActionBar().show();
                        mode.finish();
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                getSupportActionBar().show();
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
                getSupportActionBar().hide();
                view.setSelected(true);

                return true;
            }
        });

        // Setup the ad
        final AdView mAdView = (AdView) findViewById(R.id.groupsAdView);
        AdRequest.Builder adRequest = new AdRequest.Builder();
        if(_dataStore.CurrentLocation != null)
            adRequest.setLocation(_dataStore.CurrentLocation);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }
        });
        mAdView.loadAd(adRequest.build());
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

    private void deleteGroup() {
        _groups.remove(_currentGroup);
        _dataStore.deleteContactGroup(_currentGroup.Name);
        _groupsAdapter.notifyDataSetChanged();
    }

    private void sendGroupFlare() {
        for(Contact c : _currentGroup.Contacts){
            Contact contact = _dataStore.AllContacts.get(c.name);
            contact.selected = true;
            _dataStore.SelectedContacts.add(contact);
        }

        Location location;
        try {
            LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
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
        for(Contact c : _currentGroup.Contacts){
            Contact contact = _dataStore.AllContacts.get(c.name);
            contact.selected = true;
            _dataStore.SelectedContacts.add(contact);
        }

        Intent intent = new Intent(this,CreateGroupActivity.class);
        intent.putExtra("groupName",_currentGroup.Name);
        startActivity(intent);
    }

    private void createNewGroup() {
        Intent intent = new Intent(this,CreateGroupActivity.class);
        startActivity(intent);
    }
}
