package flaregradle.myapp.com.Flare;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.MyApp.Flare.R;

import flaregradle.myapp.com.Flare.AsyncTasks.FindFlareUsersTask;
import flaregradle.myapp.com.Flare.AsyncTasks.GcmRegistrationAsyncTask;
import flaregradle.myapp.com.Flare.AsyncTasks.SetUpContactsTask;
import flaregradle.myapp.com.Flare.Utilities.ContactsHandler;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class LoadScreen extends Activity {

    private GcmRegistrationAsyncTask _registrationTask;
    private ProgressBar _busyIndicator;
    private TextView _loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_screen);

        // Load the progress bar
        _busyIndicator = (ProgressBar)findViewById(R.id.busyIndicator);
        _busyIndicator.setVisibility(View.VISIBLE);

        // Set loading message
        _loading = (TextView)findViewById(R.id.loading);
        _loading.setText(R.string.loading);

    }

    @Override
    protected void onResume() {
        super.onResume();

        setUpDataStore();
        setUpContacts();
    }

    private void setFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setUpDataStore() {
        DataStorageHandler.getInstance();
        DataStorageHandler.Preferences = getPreferences(MODE_PRIVATE);
        DataStorageHandler.setupPreferences();
    }

    private void setUpContacts() {
        ContentResolver cr = getContentResolver();

        ContactsHandler _contactsHandler = new ContactsHandler(cr);
        int contactId = getResources().getIdentifier("person", "drawable", getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), contactId);
        _contactsHandler.setDefaultImage(bitmap);

        SetUpContactsTask task = new SetUpContactsTask(this,_contactsHandler);
        task.execute(this);
    }

    public void finishedGettingContacts() {
        if(DataStorageHandler.IsPhoneNumberVerified()) {
            phoneNumberIsVerified();
        } else {
            verifyPhoneNumber();
        }
    }

    private void verifyPhoneNumber() {
        Intent intent = new Intent(this,VerifyPhoneActivity.class);
        startActivity(intent);
    }

    private void phoneNumberIsVerified() {
        registerDevice();

        findContactsWithFlare();

        moveToHomeScreen();
    }

    private void registerDevice(){
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String number = tm.getLine1Number();
        String country = tm.getSimCountryIso();

        DataStorageHandler.thisPhone = number;
        _registrationTask = new GcmRegistrationAsyncTask(this,number);
        _registrationTask.execute(this);
    }

    private void findContactsWithFlare() {
        FindFlareUsersTask findTask = new FindFlareUsersTask();
        findTask.execute(this);
    }

    private void moveToHomeScreen() {
        Intent intent = new Intent(this,FlareHome.class);
        startActivity(intent);
    }
}
