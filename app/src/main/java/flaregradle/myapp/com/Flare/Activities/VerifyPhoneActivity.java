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
import com.google.common.annotations.Beta;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import flaregradle.myapp.com.Flare.AsyncTasks.FindFlareUsersTask;
import flaregradle.myapp.com.Flare.AsyncTasks.GcmRegistrationAsyncTask;
import flaregradle.myapp.com.Flare.AsyncTasks.SendTwilioSmsTask;
import flaregradle.myapp.com.Flare.Events.RegistrationError;
import flaregradle.myapp.com.Flare.Events.RegistrationSuccess;
import flaregradle.myapp.com.Flare.Events.TwilioError;
import flaregradle.myapp.com.Flare.Events.TwilioSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class VerifyPhoneActivity extends Activity {

    @Bind(R.id.verify_phone) LinearLayout _verifyPhoneLayout;
    @Bind(R.id.enter_code) LinearLayout _enterCodeLayout;
    @Bind(R.id.continue_setup) LinearLayout _continueLayout;

    @Bind(R.id.country_code) TextView _countryCodeView;
    @Bind(R.id.phone) EditText _phoneNumberEntry;
    @Bind(R.id.code) EditText _codeEntry;
    @Bind(R.id.error) TextView _verifyErrorMessage;
    @Bind(R.id.submit_error) TextView _submitErrorMessage;
    @Bind(R.id.verify_button) Button _verifyButton;
    @Bind(R.id.progress_circular) ProgressBar _progressBar;
    @Bind(R.id.countries) Spinner _countryList;

    String _code;
    HashMap<String,String> _countryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        ButterKnife.bind(this);

        countriesSetup();
        setInitialState();
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
        new AlertDialog.Builder(this)
                .setTitle("Let your friends find you")
                .setMessage("We can let your friends see that you have flare. We will need to save your phone number to the cloud to do this"
                        + ". Do you accept ?")
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataStorageHandler.SetCanWeSaveTheUsersInformation(true);
                        DataStorageHandler.SetHaveAskedToSaveTheUsersInformation(true);
                        new GcmRegistrationAsyncTask().execute(getApplicationContext());
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataStorageHandler.SetCanWeSaveTheUsersInformation(false);
                        DataStorageHandler.SetHaveAskedToSaveTheUsersInformation(true);
                        moveOntoSettingsSetup();
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Subscribe public void RegisteredSuccessfully(RegistrationSuccess success) {
        moveOntoSettingsSetup();
    }

    @Subscribe public void ErrorRegistering(RegistrationError error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("We could not register you with the cloud. We will keep trying")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveOntoSettingsSetup();
                        dialog.cancel();
                    }
                }).show();
    }

    private void moveOntoSettingsSetup() {
        Intent intent = new Intent(this,SettingsSetup.class);
        startActivity(intent);
    }
}
