package flaregradle.myapp.com.Flare;

import flaregradle.myapp.com.Flare.AsyncTasks.GcmRegistrationAsyncTask;
import flaregradle.myapp.com.Flare.AsyncTasks.SetUpContactsTask;
import flaregradle.myapp.com.Flare.Utilities.ContactsHandler;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;
import flaregradle.myapp.com.Flare.util.SystemUiHider;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.MyApp.Flare.R;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LoadScreen extends ActionBarActivity {

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

        setUpContacts();
    }

    private void registerDevice(){
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String number = tm.getLine1Number();
        DataStorageHandler.getInstance().thisPhone = number;
        _registrationTask = new GcmRegistrationAsyncTask(this,number);
        _registrationTask.execute(this);
    }

    private void setUpContacts() {
        ContentResolver cr = getContentResolver();
        ContactsHandler _contactsHandler = new ContactsHandler(cr);
        int contactId = getResources().getIdentifier("contactdefaultimage","drawable",getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), contactId);
        _contactsHandler.setDefaultImage(bitmap);

        DataStorageHandler _dataStore = DataStorageHandler.getInstance();
        _dataStore.Preferences = getPreferences(MODE_PRIVATE);
        SetUpContactsTask task = new SetUpContactsTask(this,_contactsHandler);
        task.execute(this);
    }

    public void RegisterDevice() {
        _loading.setText(R.string.connecting);
        registerDevice();
    }

    public void continueToHomeScreen() {
        Intent intent = new Intent(this,FlareHome.class);
        startActivity(intent);
    }

    public void retryRegister() {
        _loading.setText(R.string.connecting);
        registerDevice();
    }
}