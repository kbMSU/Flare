package flaregradle.myapp.com.Flare.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.MyApp.Flare.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TreeSet;

import flaregradle.myapp.com.Flare.AsyncTasks.FindFlareUsersTask;
import flaregradle.myapp.com.Flare.AsyncTasks.GcmRegistrationAsyncTask;
import flaregradle.myapp.com.Flare.AsyncTasks.SendTwilioSmsTask;
import flaregradle.myapp.com.Flare.Events.TwilioError;
import flaregradle.myapp.com.Flare.Events.TwilioSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class VerifyPhoneActivity extends Activity {

    private EventsModule eventsModule = EventsModule.getInstance();

    private LinearLayout _verifyPhoneLayout;
    private LinearLayout _enterCodeLayout;
    private LinearLayout _continueLayout;

    private TextView _countryCodeView;
    private EditText _phoneNumberEntry;
    private EditText _codeEntry;
    private TextView _verifyErrorMessage;
    private TextView _submitErrorMessage;
    private Button _verifyButton;
    private ProgressBar _progressBar;
    private Spinner _countryList;
    private String _code;
    private HashMap<String,String> _countryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        _countryCodeView = (TextView)findViewById(R.id.country_code);
        _phoneNumberEntry = (EditText)findViewById(R.id.phone);
        _codeEntry = (EditText)findViewById(R.id.code);
        _verifyButton = (Button)findViewById(R.id.verify_button);
        _progressBar = (ProgressBar)findViewById(R.id.progress_circular);
        _verifyErrorMessage = (TextView)findViewById(R.id.error);
        _submitErrorMessage = (TextView)findViewById(R.id.submit_error);
        _countryList = (Spinner)findViewById(R.id.countries);

        _verifyPhoneLayout = (LinearLayout)findViewById(R.id.verify_phone);
        _enterCodeLayout = (LinearLayout)findViewById(R.id.enter_code);
        _continueLayout = (LinearLayout)findViewById(R.id.continue_setup);

        countriesSetup();
        setInitialState();
        eventsModule.Register(this);
    }

    @Subscribe public void TwilioMessageSent(TwilioSuccess success) {
        setReceivedCode();
    }

    @Subscribe public void TwilioError(TwilioError error) {
        setErrorVerifying();
    }

    private void countriesSetup() {
        _countryMap = new HashMap<>();
        TreeSet<String> sortedCountries = new TreeSet<>();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for(Locale l : Locale.getAvailableLocales()) {
            if(!_countryMap.containsKey(l.getDisplayCountry())) {
                String country = l.getCountry();
                String displayCountry = l.getDisplayCountry();
                for(String pair : rl) {
                    String[] splitPair = pair.split(",");
                    if(splitPair[1].trim().equals(country.trim())) {
                        _countryMap.put(displayCountry, splitPair[0]);
                        sortedCountries.add(displayCountry);
                        break;
                    }
                }
            }
        }

        String[] countries = new String[200];
        sortedCountries.toArray(countries);

        Integer usIndex = 0;
        Integer index = 0;
        for(String country : countries) {
            if(country.toLowerCase().contains("united states")) {
                usIndex = index;
                break;
            }
            index++;
        }

        ArrayAdapter<String> countriesAdapter =
                new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,countries);
        _countryList.setAdapter(countriesAdapter);

        _countryList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                String displayCountry = (String)itemAtPosition;
                String code = _countryMap.get(displayCountry);
                _countryCodeView.setText(code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        _countryList.setSelection(usIndex);
    }

    public void onVerifyPhone(View view) {
        String phone = _phoneNumberEntry.getText().toString();
        String code = _countryCodeView.getText().toString();
        generateCode();
        verifyPhone(code + phone);
    }

    private void generateCode() {
        Random random = new Random();
        Integer code = random.nextInt(9999);
        while(code < 1000) {
            code *= 2;
        }
        _code = code.toString();
    }

    private void verifyPhone(String phone) {
        String to = "+"+phone;
        List<String> toList = new ArrayList<>();
        toList.add(to);
        String body = "Your flare code is "+_code;
        SendTwilioSmsTask askTwilioForMessage = new SendTwilioSmsTask(this, toList, body);

        setVerifyingState();
        askTwilioForMessage.execute();
    }

    private void setVerifyingState() {
        _progressBar.setVisibility(View.VISIBLE);
        _verifyButton.setVisibility(View.GONE);
        _verifyErrorMessage.setVisibility(View.GONE);
    }

    private void setErrorVerifying() {
        _progressBar.setVisibility(View.GONE);
        _verifyButton.setVisibility(View.VISIBLE);
        _verifyErrorMessage.setVisibility(View.VISIBLE);
    }

    private void setReceivedCode() {
        _verifyPhoneLayout.setVisibility(View.GONE);
        _enterCodeLayout.setVisibility(View.VISIBLE);
        _continueLayout.setVisibility(View.GONE);
    }

    public void onVerifyCode(View view) {
        String code = _codeEntry.getText().toString();
        if(code.equals(_code)) {
            DataStorageHandler.SetPhoneNumberVerified();
            verificationSuccessful();
        } else {
            setInvalidCode();
        }
    }

    private void setInvalidCode() {
        _submitErrorMessage.setVisibility(View.VISIBLE);
    }

    private void verificationSuccessful() {
        _verifyPhoneLayout.setVisibility(View.GONE);
        _enterCodeLayout.setVisibility(View.GONE);
        _continueLayout.setVisibility(View.VISIBLE);
    }

    public void resendVerification(View view) {
        setInitialState();
    }

    private void setInitialState() {
        _continueLayout.setVisibility(View.GONE);
        _enterCodeLayout.setVisibility(View.GONE);
        _verifyPhoneLayout.setVisibility(View.VISIBLE);

        _submitErrorMessage.setVisibility(View.GONE);
        _verifyErrorMessage.setVisibility(View.GONE);

        _progressBar.setVisibility(View.GONE);

        _verifyButton.setVisibility(View.VISIBLE);
    }

    public void onContinueSetup(View view) {
        DataStorageHandler.savePhoneNumber(_countryCodeView.getText().toString(), _phoneNumberEntry.getText().toString());
        registerPhone();
    }

    private void registerPhone() {
        boolean haveWeAsked = DataStorageHandler.HaveAskedToSaveTheUsersInformation();
        if(haveWeAsked) {
            boolean canWeSave = DataStorageHandler.CanWeSaveTheUsersInformation();
            if(canWeSave)
                new GcmRegistrationAsyncTask().execute(this);
            moveOntoSettingsSetup();
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
                            moveOntoSettingsSetup();
                        }
                })
                    .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataStorageHandler.SetCanWeSaveTheUsersInformation(false);
                            DataStorageHandler.SetHaveAskedToSaveTheUsersInformation(true);
                            moveOntoSettingsSetup();
                        }
                    })
                .show();
        }

        new GcmRegistrationAsyncTask().execute(this);
    }

    private void moveOntoSettingsSetup() {
        Intent intent = new Intent(this,SettingsSetup.class);
        startActivity(intent);
    }
}
