package flaregradle.myapp.com.Flare;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.windowsazure.notifications.NotificationsHandler;

import java.util.logging.Logger;

public class AzureNotificationsHandler extends NotificationsHandler {

    public AzureNotificationsHandler() {

    }

    @Override
    public void onReceive(Context context, Bundle bundle) {
        Log.v("AZURE","Reached Azure notifications handler");
    }
}
