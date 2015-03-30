package flaregradle.myapp.com.flare;

import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.MyApp.Flare.R;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Locale;

public class FlareHome extends ActionBarActivity {

    private LocationManager _locationManager;
    private String _provider;
    private LocationListener _locationListener;
    private Location _location;

    private Toast _message;
    private GoogleMap _map;

    private FloatingActionsMenu _fabMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.MyApp.Flare.R.layout.main);

        // Set up the toast message
        _message = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        // Get the map
        _map =((MapFragment)getFragmentManager().findFragmentById(com.MyApp.Flare.R.id.map)).getMap();
        _map.getUiSettings().setZoomControlsEnabled(false);
        _map.setMyLocationEnabled(false);
        _map.getUiSettings().setMyLocationButtonEnabled(false);

        // Set the initial location and location updates
        try {
            _locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            _provider = _locationManager.getBestProvider(new Criteria(), false);
            //_locationListener = new LocationUpdateHandler(this);
            //_locationManager.requestLocationUpdates(_provider, 1000,5, _locationListener);
        } catch (Exception ex) {
            showMessage("Fatal Error : Could not get location service");
            try {
                wait(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finish();
        }

        updateLocation();

        // Set up the Material theme
        setTheToolbar();

        // Set up the fab
        _fabMenu = (FloatingActionsMenu)findViewById(R.id.fab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.MyApp.Flare.R.menu.home_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void setTheToolbar() {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    public void onLocationLookupClick(View v) {

    }

    public void onUpdateLocationClick(View v) {
        updateLocation();
    }

    private void updateLocation() {
        try {
            _location = _locationManager.getLastKnownLocation(_provider);
            setLocation();
            moveMapToLocation();
            animateMapToLocation();
        } catch (Exception ex) {
            showMessage("Uh oh , there was a problem getting your location. Please try again when you have GPS connection");
        }
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

        _fabMenu.collapse();

        Intent sendFlareIntent = new Intent(this,SendFlareActivity.class);
        sendFlareIntent.putExtra("latitude",String.valueOf(_location.getLatitude()));
        sendFlareIntent.putExtra("longitude",String.valueOf(_location.getLongitude()));
        startActivity(sendFlareIntent);
    }

    private void showMessage(String text){
        _message.setText(text);
        _message.show();
    }

    private void setLocation(){
        if(_location == null)
            return;

        MarkerOptions currentLocationMarker = new MarkerOptions().position(new LatLng(_location.getLatitude(),_location.getLongitude()));

        try
        {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(_location.getLatitude(), _location.getLongitude(), 1);

            Address likelyLocation = addresses.get(0);
            String address = likelyLocation.getAddressLine(0)+"\n"+likelyLocation.getAddressLine(1);
            currentLocationMarker.title(address);

            TextView view = (TextView)findViewById(R.id.searchLocationText);
            view.setText(likelyLocation.getAddressLine(0));
        }
        catch(Exception ex)
        {
            showMessage(ex.getMessage());
            return;
        }

        _map.clear();
        _map.addMarker(currentLocationMarker);
        _map.setMyLocationEnabled(true);
    }

    private void moveMapToLocation() {
        if(_location == null)
            return;

        _map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(_location.getLatitude(),_location.getLongitude())));
    }

    private void animateMapToLocation() {
        _map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    public void GetLocationUpdate(Location location){
        _location = location;
        setLocation();
    }
}
