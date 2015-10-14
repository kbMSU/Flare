package flaregradle.myapp.com.Flare;

import android.support.multidex.MultiDexApplication;

import com.parse.Parse;
import com.parse.ParseInstallation;

public class FlareApplication extends MultiDexApplication
{
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.setLogLevel(Parse.LOG_LEVEL_INFO);
        Parse.initialize(this, "INoehKZFskuQ6nJ383gzDshdhFHSre9lv5MQrZ7g", "9y6Dx6hqc28c4uyULtzOWrwb0Pmfi0Up3GXDzjpA");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
