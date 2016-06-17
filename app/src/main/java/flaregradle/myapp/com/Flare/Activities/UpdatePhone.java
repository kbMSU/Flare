package flaregradle.myapp.com.Flare.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
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
import com.parse.ParseInstallation;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TreeSet;

import butterknife.Bind;
import butterknife.ButterKnife;
import flaregradle.myapp.com.Flare.AsyncTasks.SendTwilioSmsTask;
import flaregradle.myapp.com.Flare.AsyncTasks.UpdatePhoneNumberAsyncTask;
import flaregradle.myapp.com.Flare.Events.ParseError;
import flaregradle.myapp.com.Flare.Events.ParseSaveDataSuccess;
import flaregradle.myapp.com.Flare.Events.TwilioError;
import flaregradle.myapp.com.Flare.Events.TwilioSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class UpdatePhone extends Activity {

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

    private String _code;
    private HashMap<String,String> _countryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_phone);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void TwilioMessageSent(TwilioSuccess success) {
        setReceivedCode();
    }

    @Subscribe
    public void TwilioError(TwilioError error) {
        setErrorVerifying();
    }

    @Subscribe
    public void ParseError(ParseError error) {
        errorSavingPhoneToCloud(error.getException());
    }

    @Subscribe
    public void ParseSuccess(ParseSaveDataSuccess success) {
        savedPhoneToCloud();
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
            savePhoneNumberToCloud();
        } else {
            setInvalidCode();
        }
    }

    private void setInvalidCode() {
        _submitErrorMessage.setVisibility(View.VISIBLE);
    }

    private void savePhoneNumberToCloud() {
        String countryCode = _countryCodeView.getText().toString();
        String phoneNumber = _phoneNumberEntry.getText().toString();

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("CountryCode",countryCode);
        installation.put("Number",phoneNumber);
        installation.put("FullPhone",countryCode+phoneNumber);
        installation.saveInBackground();

        if (DataStorageHandler.CanWeSaveTheUsersInformation()) {
            UpdatePhoneNumberAsyncTask updateTask = new UpdatePhoneNumberAsyncTask(countryCode,phoneNumber);
            updateTask.execute(this);
        } else {
            savedPhoneToCloud();
        }
    }

    private void savedPhoneToCloud() {
        DataStorageHandler.savePhoneNumber(_countryCodeView.getText().toString(), _phoneNumberEntry.getText().toString());
        verificationSuccessful();
    }

    private void errorSavingPhoneToCloud(Exception ex) {
        Toast.makeText(this,"Uh oh, there was a problem updating your phone number",Toast.LENGTH_LONG);
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
        NavUtils.navigateUpFromSameTask(this);
    }

}
