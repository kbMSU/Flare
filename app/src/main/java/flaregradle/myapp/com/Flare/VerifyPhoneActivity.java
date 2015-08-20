package flaregradle.myapp.com.Flare;

import android.app.Activity;
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

import com.MyApp.Flare.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class VerifyPhoneActivity extends Activity {

    private LinearLayout _verifyPhoneLayout;
    private LinearLayout _enterCodeLayout;
    private LinearLayout _continueLayout;

    private EditText _phoneNumberEntry;
    private EditText _codeEntry;
    private TextView _verifyErrorMessage;
    private TextView _submitErrorMessage;
    private Button _verifyButton;
    private Button _submitButton;
    private Button _resendButton;
    private ProgressBar _progressBar;
    private Spinner _countryList;

    private String _code;
    private String _phone;
    private Runnable _askTwilioForMessage;
    private HashMap<String,String> _countryMap;
    private MobileServiceClient _azureClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        _phoneNumberEntry = (EditText)findViewById(R.id.phone);
        _codeEntry = (EditText)findViewById(R.id.code);
        _verifyButton = (Button)findViewById(R.id.verify_button);
        _submitButton = (Button)findViewById(R.id.submit_button);
        _progressBar = (ProgressBar)findViewById(R.id.progress_circular);
        _verifyErrorMessage = (TextView)findViewById(R.id.error);
        _submitErrorMessage = (TextView)findViewById(R.id.submit_error);
        _resendButton = (Button)findViewById(R.id.resend);
        _countryList = (Spinner)findViewById(R.id.countries);

        _verifyPhoneLayout = (LinearLayout)findViewById(R.id.verify_phone);
        _enterCodeLayout = (LinearLayout)findViewById(R.id.enter_code);
        _continueLayout = (LinearLayout)findViewById(R.id.continue_setup);

        countriesSetup();
        twilioSetup();
        setInitialState();
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
                _phoneNumberEntry.setText(code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        _countryList.setSelection(usIndex);
    }

    private void twilioSetup() {
        try {
            _azureClient = new MobileServiceClient("https://flareservice.azure-mobile.net/","vAymygcCyvnOQrDzLOEjyOQGIxIJMm78",this);
        } catch (Exception ex) {
            Log.e("azure_client_setup",ex.getMessage());
        }

        _askTwilioForMessage = new Runnable() {
            @Override
            public void run() {
                try {
                    setVerifyingState();

                    String to = "+"+_phone;
                    String body = "Your flare code is "+_code;

                    if(_azureClient != null) {
                        List<Pair<String,String>> parameters = new ArrayList<>();
                        parameters.add(new Pair<>("to",to));
                        parameters.add(new Pair<>("body", body));
                        ListenableFuture<JsonElement> result = _azureClient.invokeApi("Messages", "Post", parameters);
                        result.isDone();
                    }

                    setReceivedCode();
                } catch (Exception ex) {
                    setErrorVerifying();
                }
            }
        };
    }

    public void onVerifyPhone(View view) {
        String phone = _phoneNumberEntry.getText().toString();
        generateCode();
        verifyPhone(phone);
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
        _phone = phone;
        _askTwilioForMessage.run();
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
        moveOntoSettingsSetup();
    }

    private void moveOntoSettingsSetup() {
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }
}
