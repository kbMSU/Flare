package flaregradle.myapp.com.Flare.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.MyApp.Flare.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class FlareHome extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Location _location;
    private Toast _message;
    private GoogleMap _map;
    private DrawerLayout _drawerLayout;
    private ListView _drawerList;
    private LinearLayout _drawer;
    private ActionBarDrawerToggle _drawerToggle;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.MyApp.Flare.R.layout.main);

        // Set up the toast message
        _message = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        // Get the map
        _map =((MapFragment)getFragmentManager().findFragmentById(com.MyApp.Flare.R.id.map)).getMap();
        _map.getUiSettings().setZoomControlsEnabled(false);
        _map.setMyLocationEnabled(false);
        _map.getUiSettings().setMyLocationButtonEnabled(false);
        _map.getUiSettings().setMapToolbarEnabled(false);
        _map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (_location == null) {
                    TextView view = (TextView) findViewById(R.id.searchLocationText);
                    view.setText(R.string.unknown_location);
                    return;
                }

                _location.setLatitude(cameraPosition.target.latitude);
                _location.setLongitude(cameraPosition.target.longitude);
                setLocation();
            }
        });

        // Set the drawer
        _drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        _drawer = (LinearLayout) findViewById(R.id.left_drawer);
        _drawerList = (ListView) findViewById(R.id.left_drawer_list);

        String[] drawerItems = new String[3];
        drawerItems[0] = "Feedback";
        drawerItems[1] = "Share";
        drawerItems[2] = "Settings";

        _drawerList.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, drawerItems));
        _drawerToggle = new ActionBarDrawerToggle(this, _drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        _drawerLayout.setDrawerListener(_drawerToggle);
        _drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent email = new Intent(Intent.ACTION_SEND);
                        email.putExtra(Intent.EXTRA_EMAIL, new String[] { "support@shoresideapps.com" });
                        email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                        // need this to prompts email client only
                        email.setType("message/rfc822");
                        startActivity(Intent.createChooser(email, "Choose an Email client"));
                        break;

                    case 1:
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Download Flare at http://goo.gl/yO6nXj !");
                        shareIntent.setType("text/plain");
                        startActivity(Intent.createChooser(shareIntent, "Share"));
                        break;

                    case 2:
                        Intent settingsIntent = new Intent(getApplicationContext(),SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                }
            }
        });

        // Set action bar
        ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }

        // Set up the google api client
        buildGoogleApiClient();

        // Create the request to get location updates
        createLocationRequest();

        // Connect to the google api client
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        _drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _drawerToggle.onConfigurationChanged(newConfig);
    }

    // region Create Google Services Client
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    // endregion

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(this,SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.feedback:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[] { "support@shoresideapps.com" });
                email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                // need this to prompts email client only
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Choose an Email client"));
                return true;

            case R.id.share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Download Flare at http://goo.gl/yO6nXj !");
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share"));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // endregion

    // region Button Clicks
    public void onUpdateLocationClick(View v) {
        updateLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
    }

    public void onGroupsClick(View v) {
        Intent intent = new Intent(this,GroupsActivity.class);
        startActivity(intent);
    }

    public void onSendFlareClick(View v) {
        if(_location == null) {
            showMessage("Unable to acquire your location");
            return;
        }

        Intent sendFlareIntent = new Intent(this, SendFlareActivity.class);
        sendFlareIntent.putExtra("latitude", String.valueOf(_location.getLatitude()));
        sendFlareIntent.putExtra("longitude", String.valueOf(_location.getLongitude()));
        startActivity(sendFlareIntent);
    }
    // endregion

    private void showMessage(String text){
        _message.setText(text);
        _message.show();
    }

    // region Google Service Connection Call-Backs
    @Override
    public void onConnected(Bundle bundle) {
        // Get the current location
        updateLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    // endregion

    // region Location Changed Call-Back
    @Override
    public void onLocationChanged(Location location) {
        try {
            _location = location;
            setLocation();
            moveMapToLocation();
            animateMapToLocation();
        } catch (Exception ex) {
            showMessage("Uh oh , there was a problem getting your location. Please try again when you have GPS connection");
        }
    }
    // endregion

    // region Update Location
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
    }

    private void updateLocation(Location location) {
        try {
            _location = location;
            setLocation();
            moveMapToLocation();
        } catch (Exception ex) {
            showMessage("Uh oh , there was a problem getting your location. Please try again when you have GPS connection");
        }
    }

    private void setLocation(){
        if(_location == null)
            return;

        DataStorageHandler.CurrentLocation = _location;
        try
        {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(_location.getLatitude(), _location.getLongitude(), 1);
            if(addresses.size() > 0) {
                Address likelyLocation = addresses.get(0);
                TextView view = (TextView)findViewById(R.id.searchLocationText);
                view.setText(likelyLocation.getAddressLine(0));
            }
        }
        catch(Exception ex)
        {
            showMessage(ex.getMessage());
            return;
        }

        _map.clear();
        _map.setMyLocationEnabled(true);
    }

    private void moveMapToLocation() {
        if (_location == null)
            return;

        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(_location.getLatitude(), _location.getLongitude()), 15));
    }

    private void animateMapToLocation() {
        _map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }
    // endregion
}
