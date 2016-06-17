package flaregradle.myapp.com.Flare.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MyApp.Flare.R;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import flaregradle.myapp.com.Flare.AsyncTasks.DeleteRegistrationAsyncTask;
import flaregradle.myapp.com.Flare.AsyncTasks.FindFlareUsersTask;
import flaregradle.myapp.com.Flare.AsyncTasks.GcmRegistrationAsyncTask;
import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
import flaregradle.myapp.com.Flare.Events.DeleteRegistrationError;
import flaregradle.myapp.com.Flare.Events.DeleteRegistrationSuccess;
import flaregradle.myapp.com.Flare.Events.FindFlareError;
import flaregradle.myapp.com.Flare.Events.FindFlareSuccess;
import flaregradle.myapp.com.Flare.Events.RegistrationError;
import flaregradle.myapp.com.Flare.Events.RegistrationSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;
import flaregradle.myapp.com.Flare.Utilities.PermissionHandler;

public class SettingsActivity extends AppCompatActivity {

    final int SEND_SMS_REQUEST = 1;

    @Bind(R.id.default_decline_textview) TextView _declineResponseTextView;
    @Bind(R.id.default_decline_edittext) EditText _declineResponseEditText;
    @Bind(R.id.default_accept_textview) TextView _acceptResponseTextView;
    @Bind(R.id.default_accept_edittext) EditText _acceptResponsetEditText;

    @Bind(R.id.select_text_message) CheckBox textMessageCheckBox;
    @Bind(R.id.select_cloud_message) CheckBox cloudMessageCheckBox;
    @Bind(R.id.select_allow_save) CheckBox selectAllowSaveCheckBox;
    @Bind(R.id.select_find_friends) CheckBox selectFindFriendsCheckBox;

    @Bind(R.id.current_phone_number) TextView currentPhoneNumberTextView;
    @Bind(R.id.update_phone_number) ImageView updateCurrentNumberButton;

    private AlertDialog _registerAlert;
    private AlertDialog _findFriendsAlert;
    private AlertDialog _errorAlert;
    private AlertDialog _findFriendsSuccessAlert;
    private AlertDialog _registerSuccessAlert;
    private AlertDialog _deleteRegistrationSuccessAlert;
    private AlertDialog _stopFindingFriendsSuccessAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        final Activity thisActivity = this;

        currentPhoneNumberTextView.setText(DataStorageHandler.getPhoneNumber());

        cloudMessageCheckBox.setChecked(DataStorageHandler.CanSendCloudMessage());
        textMessageCheckBox.setChecked(!DataStorageHandler.CanSendCloudMessage());
        selectAllowSaveCheckBox.setChecked(DataStorageHandler.CanWeSaveTheUsersInformation());
        selectFindFriendsCheckBox.setChecked(DataStorageHandler.CanCheckContactsForFlare());

        _registerAlert = new AlertDialog.Builder(this)
                            .setTitle("Register")
                            .setMessage("Hold on. Updating the cloud")
                            .create();

        _findFriendsAlert = new AlertDialog.Builder(this)
                .setTitle("Find friends")
                .setMessage("Hold on. Updating the cloud")
                .create();

        _errorAlert = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Uh oh, something went wrong")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        _findFriendsSuccessAlert = new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Alright ! We found all your friends who are on the cloud")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        _stopFindingFriendsSuccessAlert = new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Alright ! We stopped checking our cloud for your friends")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        _registerSuccessAlert = new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Alright ! Our cloud knows about you now")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        _deleteRegistrationSuccessAlert = new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Alright ! We removed your registration from our cloud")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        textMessageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (PermissionHandler.canSendSms(thisActivity)) {
                        cloudMessageCheckBox.setChecked(false);
                        DataStorageHandler.SetSendCloudMessage(false);
                    } else {
                        PermissionHandler.requestSmsPermission(thisActivity,SEND_SMS_REQUEST);
                    }
                }
            }
        });

        cloudMessageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    textMessageCheckBox.setChecked(false);
                    DataStorageHandler.SetSendCloudMessage(true);
                }
            }
        });

        selectAllowSaveCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _registerAlert.show();
                if(isChecked) {
                    if(!DataStorageHandler.IsRegistered()) {
                        new GcmRegistrationAsyncTask().execute(getApplicationContext());
                    }
                } else {
                    if (DataStorageHandler.IsRegistered()) {
                        new DeleteRegistrationAsyncTask().execute(getApplicationContext());
                    }
                }
                DataStorageHandler.SetHaveAskedToSaveTheUsersInformation(true);
            }
        });

        selectFindFriendsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    _findFriendsAlert.show();
                    new FindFlareUsersTask().execute(getApplicationContext());
                } else {
                    DataStorageHandler.SetCanCheckContactsForFlare(false);
                    for(Contact contact : DataStorageHandler.AllContacts.values()) {
                        contact.hasFlare = false;
                        contact.phoneNumber.hasFlare = false;
                        for(PhoneNumber number : contact.allPhoneNumbers) {
                            number.hasFlare = false;
                        }
                    }
                    _stopFindingFriendsSuccessAlert.show();
                }
                DataStorageHandler.SetHaveAskedToCheckContactsWithFlare(true);
            }
        });

        _declineResponseTextView.setText(DataStorageHandler.GetDefaultDeclineResponse());
        _declineResponseEditText.setText(DataStorageHandler.GetDefaultDeclineResponse());
        _declineResponseEditText.setVisibility(View.GONE);

        _acceptResponseTextView.setText(DataStorageHandler.GetDefaultAcceptResponse());
        _acceptResponsetEditText.setText(DataStorageHandler.GetDefaultAcceptResponse());
        _acceptResponsetEditText.setVisibility(View.GONE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SEND_SMS_REQUEST) {
            if (grantResults.length > 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cloudMessageCheckBox.setChecked(false);
            }
        }
    }

    @Subscribe public void SuccessfullyRegistered(RegistrationSuccess success) {
        DataStorageHandler.SetCanWeSaveTheUsersInformation(true);
        _registerAlert.cancel();
        _registerSuccessAlert.show();
    }

    @Subscribe public void FailedToRegister(RegistrationError error) {
        DataStorageHandler.SetCanWeSaveTheUsersInformation(false);
        _registerAlert.cancel();
        _errorAlert.show();
        selectAllowSaveCheckBox.setChecked(false);
    }

    @Subscribe public void RegistrationDeleted(DeleteRegistrationSuccess success) {
        DataStorageHandler.SetCanWeSaveTheUsersInformation(false);
        _registerAlert.cancel();
        _deleteRegistrationSuccessAlert.show();
    }

    @Subscribe public void DeleteRegistrationFailed(DeleteRegistrationError error) {
        DataStorageHandler.SetCanWeSaveTheUsersInformation(true);
        _registerAlert.cancel();
        _errorAlert.show();
        selectAllowSaveCheckBox.setChecked(true);
    }

    @Subscribe public void FoundYourFriends(FindFlareSuccess success) {
        DataStorageHandler.SetCanCheckContactsForFlare(true);
        _findFriendsAlert.cancel();
        _findFriendsSuccessAlert.show();
    }

    @Subscribe public void FailedToFindFriends(FindFlareError error) {
        DataStorageHandler.SetCanCheckContactsForFlare(false);
        _findFriendsAlert.cancel();
        _errorAlert.show();
        selectFindFriendsCheckBox.setChecked(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onDeclineMessageEditClick(View v) {
        if(_declineResponseEditText.getVisibility() == View.GONE) {
            _declineResponseTextView.setVisibility(View.GONE);
            _declineResponseEditText.setVisibility(View.VISIBLE);
            _declineResponseEditText.setText(DataStorageHandler.GetDefaultDeclineResponse());
            _declineResponseEditText.requestFocus();
        } else {
            DataStorageHandler.SetDefaultDeclineResponse(_declineResponseEditText.getText().toString());
            _declineResponseTextView.setText(DataStorageHandler.GetDefaultDeclineResponse());
            _declineResponseTextView.setVisibility(View.VISIBLE);
            _declineResponseEditText.setVisibility(View.GONE);
        }
    }

    public void onAcceptMessageEditClick(View v) {
        if(_acceptResponsetEditText.getVisibility() == View.GONE) {
            _acceptResponseTextView.setVisibility(View.GONE);
            _acceptResponsetEditText.setVisibility(View.VISIBLE);
            _acceptResponsetEditText.setText(DataStorageHandler.GetDefaultAcceptResponse());
            _acceptResponsetEditText.requestFocus();
        } else {
            DataStorageHandler.SetDefaultAcceptResponse(_acceptResponsetEditText.getText().toString());
            _acceptResponseTextView.setText(DataStorageHandler.GetDefaultAcceptResponse());
            _acceptResponseTextView.setVisibility(View.VISIBLE);
            _acceptResponsetEditText.setVisibility(View.GONE);
        }
    }

    public void onUpdatePhoneNumberClick(View v) {
        Intent updatePhoneIntent = new Intent(this, UpdatePhone.class);
        startActivity(updatePhoneIntent);
    }
}
