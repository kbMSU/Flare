package flaregradle.myapp.com.Flare;

import android.support.multidex.MultiDexApplication;

import com.parse.Parse;
import com.parse.ParseInstallation;

import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class FlareApplication extends MultiDexApplication
{
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.setLogLevel(Parse.LOG_LEVEL_INFO);
        Parse.initialize(this, "INoehKZFskuQ6nJ383gzDshdhFHSre9lv5MQrZ7g", "9y6Dx6hqc28c4uyULtzOWrwb0Pmfi0Up3GXDzjpA");
        ParseInstallation.getCurrentInstallation().saveInBackground();

        DataStorageHandler.getInstance();
        DataStorageHandler.Preferences = getSharedPreferences("flaregradle.myapp.com.Flare_preferences",MODE_PRIVATE);
        DataStorageHandler.setupPreferences();
    }
}
