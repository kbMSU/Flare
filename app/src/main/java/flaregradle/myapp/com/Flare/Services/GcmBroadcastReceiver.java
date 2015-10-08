package flaregradle.myapp.com.Flare.Services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    public GcmBroadcastReceiver() {
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        //ComponentName comp = new ComponentName(context.getPackageName(),PushService.class.getName());
        ComponentName comp = new ComponentName(context.getPackageName(),GcmIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
    }
}
