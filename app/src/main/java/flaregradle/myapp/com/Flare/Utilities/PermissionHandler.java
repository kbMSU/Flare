package flaregradle.myapp.com.Flare.Utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class PermissionHandler {

    public static boolean canRetrieveLocation(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity, int requestCode) {
        Toast.makeText(activity,"We need your retrieve your location for this",Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }

    public static boolean canRetrieveContacts(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestContactsPermission(Activity activity, int requestCode) {
        Toast.makeText(activity,"We need to retrieve your contacts for this",Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.READ_CONTACTS}, requestCode);
    }

    public static boolean canSendSms(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestSmsPermission(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS}, requestCode);
    }
}
