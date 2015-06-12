package flaregradle.myapp.com.Flare.Utilities;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import flaregradle.myapp.com.Flare.FlareHome;

public class LocationUpdateHandler implements LocationListener {

    private FlareHome _parent;

    public LocationUpdateHandler(FlareHome parent)
    {
        _parent = parent;
    }

    @Override
    public void onLocationChanged(Location location) {
        //_parent.GetLocationUpdate(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
