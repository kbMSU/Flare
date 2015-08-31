package flaregradle.myapp.com.Flare.Activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class FlareHome extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Location _location;
    private Toast _message;
    private GoogleMap _map;

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

        // Set up the google api client
        buildGoogleApiClient();

        // Create the request to get location updates
        createLocationRequest();
    }

    // region Create Google Services Client
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    // endregion

    // region State Change Updates
    @Override
     protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }
    // endregion

    // region Start and Stop Location Updates
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    // endregion

    // region Handle Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.MyApp.Flare.R.menu.home_options_menu, menu);
        return true;
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

    // region Show Toast
    private void showMessage(String text){
        _message.setText(text);
        _message.show();
    }
    // endregion

    // region Google Service Connection Call-Backs
    @Override
    public void onConnected(Bundle bundle) {
        // Get the current location
        updateLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

        // Request updates to location
        startLocationUpdates();
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
            animateMapToLocation();
        } catch (Exception ex) {
            showMessage("Uh oh , there was a problem getting your location. Please try again when you have GPS connection");
        }
    }

    private void setLocation(){
        if(_location == null)
            return;

        DataStorageHandler.getInstance().CurrentLocation = _location;

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
        if (_location == null)
            return;

        _map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(_location.getLatitude(), _location.getLongitude())));
    }

    private void animateMapToLocation() {
        _map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }
    // endregion
}
