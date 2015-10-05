package flaregradle.myapp.com.Flare.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.MyApp.Flare.R;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;

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

        // Set up Parse
        Parse.enableLocalDatastore(this);
        Parse.setLogLevel(Parse.LOG_LEVEL_INFO);
        Parse.initialize(this, "INoehKZFskuQ6nJ383gzDshdhFHSre9lv5MQrZ7g", "9y6Dx6hqc28c4uyULtzOWrwb0Pmfi0Up3GXDzjpA");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

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
        boolean haveAskedToCheckContactsWithFlare = DataStorageHandler.HaveAskedToCheckContactsWithFlare();
        if(haveAskedToCheckContactsWithFlare) {
            boolean canCheckContactsWithFlare = DataStorageHandler.CanCheckContactsForFlare();
            if(canCheckContactsWithFlare)
                findContactsWithFlare();
            if(DataStorageHandler.IsPhoneNumberVerified()) {
                phoneNumberIsVerified();
            } else {
                verifyPhoneNumber();
            }
        } else {
            new AlertDialog.Builder(this)
                .setTitle("Find friends with flare")
                .setMessage("We can quickly find out if any of your friends has flare. We will need to check your contact list to do this, we promise " +
                        "we don't store or share ANY private data without your consent. Do you accept ?")
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        findContactsWithFlare();
                        DataStorageHandler.SetCanCheckContactsForFlare(true);
                        DataStorageHandler.SetHaveAskedToCheckContactsWithFlare(true);
                        if (DataStorageHandler.IsPhoneNumberVerified()) {
                            phoneNumberIsVerified();
                        } else {
                            verifyPhoneNumber();
                        }
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataStorageHandler.SetCanCheckContactsForFlare(false);
                        DataStorageHandler.SetHaveAskedToCheckContactsWithFlare(true);
                        if (DataStorageHandler.IsPhoneNumberVerified()) {
                            phoneNumberIsVerified();
                        } else {
                            verifyPhoneNumber();
                        }
                    }
                })
                .show();
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
    }

    private void registerDevice(){
        boolean haveWeAsked = DataStorageHandler.HaveAskedToSaveTheUsersInformation();
        if(haveWeAsked) {
            boolean canWeSave = DataStorageHandler.CanWeSaveTheUsersInformation();
            if(canWeSave)
                new GcmRegistrationAsyncTask().execute(this);
            moveToHomeScreen();
        } else {
            new AlertDialog.Builder(this)
                .setTitle("Let your friends find you")
                .setMessage("We can let your friends see that you have flare. We will need to save your phone number to the cloud to do this " +
                        "we don't store or share ANY private data without your consent. Do you accept ?")
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataStorageHandler.SetCanWeSaveTheUsersInformation(true);
                        DataStorageHandler.SetHaveAskedToSaveTheUsersInformation(true);
                        new GcmRegistrationAsyncTask().execute(getApplicationContext());
                        moveToHomeScreen();
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataStorageHandler.SetCanWeSaveTheUsersInformation(false);
                        DataStorageHandler.SetHaveAskedToSaveTheUsersInformation(true);
                        moveToHomeScreen();
                    }
                })
                .show();
        }
    }

    private void moveToHomeScreen() {
        Intent intent = new Intent(this,FlareHome.class);
        startActivity(intent);
    }
}
