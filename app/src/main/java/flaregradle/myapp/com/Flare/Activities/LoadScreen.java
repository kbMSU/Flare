package flaregradle.myapp.com.Flare.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.MyApp.Flare.R;
import com.android.vending.billing.IInAppBillingService;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import flaregradle.myapp.com.Flare.AsyncTasks.FindFlareUsersTask;
import flaregradle.myapp.com.Flare.AsyncTasks.GcmRegistrationAsyncTask;
import flaregradle.myapp.com.Flare.AsyncTasks.QueryPurchasedItemsTask;
import flaregradle.myapp.com.Flare.AsyncTasks.SetUpContactsTask;
import flaregradle.myapp.com.Flare.Events.FindFlareError;
import flaregradle.myapp.com.Flare.Events.FindFlareSuccess;
import flaregradle.myapp.com.Flare.Events.QueryPurchasedItemsError;
import flaregradle.myapp.com.Flare.Events.QueryPurchasedItemsSuccess;
import flaregradle.myapp.com.Flare.Events.RegistrationError;
import flaregradle.myapp.com.Flare.Events.RegistrationSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.ContactsHandler;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class LoadScreen extends Activity {

    private ProgressBar _busyIndicator;
    private TextView _loading;

    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            DataStorageHandler.BillingService = mService;
            loadPurchases();
        }
    };

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

        connectToBilling();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventsModule.UnRegister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventsModule.Register(this);
    }

    private void connectToBilling() {
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void loadPurchases() {
        new QueryPurchasedItemsTask().execute(this);
    }

    @Subscribe
    public void GotPurchasedItems(QueryPurchasedItemsSuccess success) {
        ArrayList<String> items = success.getPurchasedItems();
        boolean found = false;
        for(String item : items) {
            if(item.equals("ad-free upgrade")) {
                DataStorageHandler.SetHavePurchasedAdFreeUpgrade(true);
                found = true;
                break;
            }
        }
        if(!found)
            DataStorageHandler.SetHavePurchasedAdFreeUpgrade(false);

        setUpContacts();
    }

    @Subscribe
    public void ErrorGettingPurchasedItems(QueryPurchasedItemsError error) {
        DataStorageHandler.SetHavePurchasedAdFreeUpgrade(false);

        setUpContacts();
    }

    private void setUpContacts() {
        ContentResolver cr = getContentResolver();

        ContactsHandler _contactsHandler = new ContactsHandler(cr);
        int contactId = getResources().getIdentifier("person", "drawable", getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), contactId);
        _contactsHandler.setDefaultImage(bitmap);

        new SetUpContactsTask(this,_contactsHandler).execute(getApplicationContext());
    }

    public void finishedGettingContacts() {
        boolean haveAskedToCheckContactsWithFlare = DataStorageHandler.HaveAskedToCheckContactsWithFlare();
        if(haveAskedToCheckContactsWithFlare) {
            boolean canCheckContactsWithFlare = DataStorageHandler.CanCheckContactsForFlare();
            if(canCheckContactsWithFlare)
                findContactsWithFlare();
            else {
                if(DataStorageHandler.IsPhoneNumberVerified()) {
                    phoneNumberIsVerified();
                } else {
                    verifyPhoneNumber();
                }
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
                        dialog.cancel();
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
                        dialog.cancel();
                    }
                })
                .show();
        }
    }

    private void findContactsWithFlare() {
        new FindFlareUsersTask().execute(getApplicationContext());
    }

    @Subscribe public void FoundContactsWithFlare(FindFlareSuccess success) {
        DataStorageHandler.SetCanCheckContactsForFlare(true);
        DataStorageHandler.SetHaveAskedToCheckContactsWithFlare(true);
        if (DataStorageHandler.IsPhoneNumberVerified()) {
            phoneNumberIsVerified();
        } else {
            verifyPhoneNumber();
        }
    }

    @Subscribe public void ErrorFindingContactsWithFlare(FindFlareError error) {
        DataStorageHandler.SetCanCheckContactsForFlare(false);
        DataStorageHandler.SetHaveAskedToCheckContactsWithFlare(true);
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Something went wrong with the connection to the cloud ! You can try to find your friends again from the settings page")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DataStorageHandler.IsPhoneNumberVerified()) {
                            phoneNumberIsVerified();
                        } else {
                            verifyPhoneNumber();
                        }
                        dialog.cancel();
                    }
                }).show();
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
                new GcmRegistrationAsyncTask().execute(getApplicationContext());
            else
                moveToHomeScreen();
        } else {
            new AlertDialog.Builder(this)
                .setTitle("Let your friends find you")
                .setMessage("We can let your friends see that you have flare. We will need to save your phone number to the cloud to do this " +
                        "we don't store or share ANY private data without your consent. Do you accept ?")
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new GcmRegistrationAsyncTask().execute(getApplicationContext());
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataStorageHandler.SetCanWeSaveTheUsersInformation(false);
                        DataStorageHandler.SetHaveAskedToSaveTheUsersInformation(true);
                        moveToHomeScreen();
                        dialog.cancel();
                    }
                })
                .show();
        }
    }

    @Subscribe public void RegisteredSuccessfully(RegistrationSuccess success) {
        DataStorageHandler.SetCanWeSaveTheUsersInformation(true);
        DataStorageHandler.SetHaveAskedToSaveTheUsersInformation(true);
        moveToHomeScreen();
    }

    @Subscribe public void ErrorRegistering(RegistrationError error) {
        DataStorageHandler.SetCanWeSaveTheUsersInformation(false);
        DataStorageHandler.SetHaveAskedToSaveTheUsersInformation(true);
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Something went wrong with your connection to the cloud ! You can try to register again from the settings page")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveToHomeScreen();
                        dialog.cancel();
                    }
                }).show();
    }

    private void moveToHomeScreen() {
        Intent intent = new Intent(this,FlareHome.class);
        startActivity(intent);
    }
}
