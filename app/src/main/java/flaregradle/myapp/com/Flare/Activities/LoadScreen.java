package flaregradle.myapp.com.Flare.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.MyApp.Flare.R;
import com.android.vending.billing.IInAppBillingService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import flaregradle.myapp.com.Flare.AsyncTasks.FindFlareUsersTask;
import flaregradle.myapp.com.Flare.AsyncTasks.GcmRegistrationAsyncTask;
import flaregradle.myapp.com.Flare.AsyncTasks.QueryPurchasedItemsTask;
import flaregradle.myapp.com.Flare.AsyncTasks.SetUpContactsTask;
import flaregradle.myapp.com.Flare.Events.FindContactsFailure;
import flaregradle.myapp.com.Flare.Events.FindContactsSuccess;
import flaregradle.myapp.com.Flare.Events.FindFlareError;
import flaregradle.myapp.com.Flare.Events.FindFlareSuccess;
import flaregradle.myapp.com.Flare.Events.QueryPurchasedItemsError;
import flaregradle.myapp.com.Flare.Events.QueryPurchasedItemsSuccess;
import flaregradle.myapp.com.Flare.Events.RegistrationError;
import flaregradle.myapp.com.Flare.Events.RegistrationSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.ContactsHandler;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;
import flaregradle.myapp.com.Flare.Utilities.PermissionHandler;

public class LoadScreen extends Activity {

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
        ProgressBar _busyIndicator = (ProgressBar) findViewById(R.id.busyIndicator);
        _busyIndicator.setVisibility(View.VISIBLE);

        // Set loading message
        TextView _loading = (TextView) findViewById(R.id.loading);
        _loading.setText(R.string.loading);

        // Set the default contact image
        int contactId = getResources().getIdentifier("person", "drawable", getPackageName());
        DataStorageHandler.DefaultContactImage = BitmapFactory.decodeResource(getResources(), contactId);

        if (!DataStorageHandler.CanSendCloudMessage() && !PermissionHandler.canSendSms(this)) {
            DataStorageHandler.SetSendCloudMessage(true);
        }

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

        // TEMP : REMOVE ALL ADS
        DataStorageHandler.SetHavePurchasedAdFreeUpgrade(true);
        //////

        ContentResolver cr = getContentResolver();
        ContactsHandler _contactsHandler = new ContactsHandler(cr);

        if(PermissionHandler.canRetrieveContacts(this)) {
            new SetUpContactsTask(_contactsHandler).execute(getApplicationContext());
        } else {
            verifyPhoneNumber();
        }
    }

    @Subscribe public void FoundContacts(FindContactsSuccess success) {
        findContactsWithFlare();
    }

    @Subscribe public void ErrorFindingContacts(FindContactsFailure failure) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("We could not find your contacts. We will keep trying")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verifyPhoneNumber();
                        dialog.cancel();
                    }
                }).show();
    }

    private void findContactsWithFlare() {
        boolean canCheckContactsWithFlare = DataStorageHandler.CanCheckContactsForFlare();
        if(canCheckContactsWithFlare) {
            new FindFlareUsersTask().execute(getApplicationContext());
        } else {
            verifyPhoneNumber();
        }
    }

    @Subscribe public void FoundContactsWithFlare(FindFlareSuccess success) {
        verifyPhoneNumber();
    }

    @Subscribe public void ErrorFindingContactsWithFlare(FindFlareError error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("We could not find which of your contacts had flare. We will keep trying")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verifyPhoneNumber();
                        dialog.cancel();
                    }
                }).show();
    }

    private void verifyPhoneNumber() {
        if (DataStorageHandler.IsPhoneNumberVerified()) {
            phoneNumberIsVerified();
        } else {
            Intent intent = new Intent(this,VerifyPhoneActivity.class);
            startActivity(intent);
        }
    }

    private void phoneNumberIsVerified() {
        registerDevice();
    }

    private void registerDevice(){
        boolean canWeSave = DataStorageHandler.CanWeSaveTheUsersInformation();
        if(canWeSave)
            new GcmRegistrationAsyncTask().execute(getApplicationContext());
        else
            moveToHomeScreen();
    }

    @Subscribe public void RegisteredSuccessfully(RegistrationSuccess success) {
        moveToHomeScreen();
    }

    @Subscribe public void ErrorRegistering(RegistrationError error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("We could not register you with our cloud. We will keep trying")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveToHomeScreen();
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void moveToHomeScreen() {
        Intent intent = new Intent(this,FlareHome.class);
        startActivity(intent);
    }
}
