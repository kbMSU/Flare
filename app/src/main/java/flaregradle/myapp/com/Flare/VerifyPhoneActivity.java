package flaregradle.myapp.com.Flare;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.MyApp.Flare.R;

import java.util.Random;

import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class VerifyPhoneActivity extends Activity {

    private EditText _phoneNumberEntry;
    private EditText _codeEntry;
    private String _code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        _phoneNumberEntry = (EditText)findViewById(R.id.phone);
        _codeEntry = (EditText)findViewById(R.id.code);
    }


    public void onVerifyPhone(View view) {
        String phone = _phoneNumberEntry.getText().toString();
        generateCode();
        verifyPhone(phone);
    }

    private void verifyPhone(String phone) {

    }

    private void generateCode() {
        Random random = new Random();
        Integer code = random.nextInt();
        while(code < 1000) {
            code *= random.nextInt(); 
        }
        _code = code.toString();
    }

    private void setVerifyingState() {

    }

    private void setErrorVerifying() {

    }

    private void setReceivedCode() {

    }

    public void onVerifyCode(View view) {
        String code = _codeEntry.getText().toString();
        if(code.equals(_code)) {
            verificationSuccessful();
        } else {
            setInvalidCode();
        }
    }

    private void setInvalidCode() {

    }

    private void verificationSuccessful() {
        DataStorageHandler.SetPhoneNumberVerified();
        moveOntoSettingsSetup();
    }

    private void moveOntoSettingsSetup() {

    }

    public void resendVerification(View view) {

    }
}
