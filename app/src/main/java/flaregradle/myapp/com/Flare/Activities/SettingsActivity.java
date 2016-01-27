package flaregradle.myapp.com.Flare.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
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
import flaregradle.myapp.com.Flare.AsyncTasks.FindFlareUsersTask;
import flaregradle.myapp.com.Flare.AsyncTasks.GcmRegistrationAsyncTask;
import flaregradle.myapp.com.Flare.Events.FindFlareError;
import flaregradle.myapp.com.Flare.Events.FindFlareSuccess;
import flaregradle.myapp.com.Flare.Events.RegistrationError;
import flaregradle.myapp.com.Flare.Events.RegistrationSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class SettingsActivity extends AppCompatActivity {

    private TextView _declineResponseTextView;
    private EditText _declineResponseEditText;
    private TextView _acceptResponseTextView;
    private EditText _acceptResponsetEditText;

    @Bind(R.id.select_text_message)
    CheckBox textMessageCheckBox;
    @Bind(R.id.select_cloud_message)
    CheckBox cloudMessageCheckBox;
    @Bind(R.id.select_allow_save)
    CheckBox selectAllowSaveCheckBox;
    @Bind(R.id.select_find_friends)
    CheckBox selectFindFriendsCheckBox;
    @Bind(R.id.current_phone_number)
    TextView currentPhoneNumberTextView;
    @Bind(R.id.update_phone_number)
    ImageView updateCurrentNumberButton;

    private AlertDialog _registerAlert;
    private AlertDialog _findFriendsAlert;
    private AlertDialog _errorAlert;
    private AlertDialog _findFriendsSuccessAlert;
    private AlertDialog _registerSuccessAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        if(DataStorageHandler.IsRegistered())
            currentPhoneNumberTextView.setText(DataStorageHandler.getPhoneNumber());
        else {
            currentPhoneNumberTextView.setText("NOT REGISTERED");
            currentPhoneNumberTextView.setClickable(false);
            updateCurrentNumberButton.setVisibility(View.GONE);
        }

        cloudMessageCheckBox.setChecked(DataStorageHandler.CanSendCloudMessage());
        textMessageCheckBox.setChecked(!DataStorageHandler.CanSendCloudMessage());
        selectAllowSaveCheckBox.setChecked(DataStorageHandler.CanWeSaveTheUsersInformation());
        selectFindFriendsCheckBox.setChecked(DataStorageHandler.CanCheckContactsForFlare());

        _registerAlert = new AlertDialog.Builder(this)
                            .setTitle("Register")
                            .setMessage("Hold on. We are telling the cloud about you")
                            .create();

        _findFriendsAlert = new AlertDialog.Builder(this)
                .setTitle("Find friends")
                .setMessage("Hold on. We are asking the cloud about your friends")
                .create();

        _errorAlert = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Uh oh, something went wrong with your connection to the cloud")
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

        _registerSuccessAlert = new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Alright ! Our cloud knows about you know")
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
                    cloudMessageCheckBox.setChecked(false);
                    DataStorageHandler.SetSendCloudMessage(false);
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
                if(!DataStorageHandler.IsRegistered() && isChecked) {
                    new GcmRegistrationAsyncTask().execute(getApplicationContext());
                    _registerAlert.show();
                } else {
                    DataStorageHandler.SetCanWeSaveTheUsersInformation(isChecked);
                }
                DataStorageHandler.SetHaveAskedToSaveTheUsersInformation(true);
            }
        });
        selectFindFriendsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!DataStorageHandler.CanCheckContactsForFlare() && isChecked) {
                    new FindFlareUsersTask().execute(getApplicationContext());
                    _findFriendsAlert.show();
                } else {
                    DataStorageHandler.SetCanCheckContactsForFlare(isChecked);
                }
                DataStorageHandler.SetHaveAskedToCheckContactsWithFlare(isChecked);
            }
        });

        _declineResponseTextView = (TextView)findViewById(R.id.default_decline_textview);
        _declineResponseEditText = (EditText)findViewById(R.id.default_decline_edittext);
        _acceptResponseTextView = (TextView)findViewById(R.id.default_accept_textview);
        _acceptResponsetEditText = (EditText)findViewById(R.id.default_accept_edittext);

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

    @Subscribe public void SuccessfullyRegistered(RegistrationSuccess success) {
        DataStorageHandler.SetCanWeSaveTheUsersInformation(true);
        _registerAlert.cancel();
        _registerSuccessAlert.show();
    }

    @Subscribe public void FailedToRegister(RegistrationError error) {
        DataStorageHandler.SetCanWeSaveTheUsersInformation(false);
        _registerAlert.cancel();
        _errorAlert.show();
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
