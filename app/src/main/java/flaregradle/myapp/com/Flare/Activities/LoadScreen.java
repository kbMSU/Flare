package flaregradle.myapp.com.Flare.Activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.MyApp.Flare.R;

import flaregradle.myapp.com.Flare.AsyncTasks.FindFlareUsersTask;
import flaregradle.myapp.com.Flare.AsyncTasks.GcmRegistrationAsyncTask;
import flaregradle.myapp.com.Flare.AsyncTasks.SetUpContactsTask;
import flaregradle.myapp.com.Flare.Utilities.ContactsHandler;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class LoadScreen extends Activity {

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

        setUpDataStore();
        setUpContacts();
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
        findContactsWithFlare();
        if(DataStorageHandler.IsPhoneNumberVerified()) {
            phoneNumberIsVerified();
        } else {
            verifyPhoneNumber();
        }
    }

    private void findContactsWithFlare() {
        FindFlareUsersTask findTask = new FindFlareUsersTask();
        findTask.execute(this);
    }

    private void verifyPhoneNumber() {
        Intent intent = new Intent(this,VerifyPhoneActivity.class);
        startActivity(intent);
    }

    private void phoneNumberIsVerified() {
        registerDevice();
        moveToHomeScreen();
    }

    private void registerDevice(){
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String number = tm.getLine1Number();
        String country = tm.getSimCountryIso();

        DataStorageHandler.thisPhone = number;
        GcmRegistrationAsyncTask task = new GcmRegistrationAsyncTask(this, number);
        task.execute(this);
    }

    private void moveToHomeScreen() {
        Intent intent = new Intent(this,FlareHome.class);
        startActivity(intent);
    }
}
