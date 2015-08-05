package flaregradle.myapp.com.Flare.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.MyApp.Flare.R;

import java.util.List;

import flaregradle.myapp.com.Flare.Adapters.PhoneNumberAdapter;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;
import flaregradle.myapp.com.Flare.Interfaces.ICallBack;

public class AlternateFlareOptionsDialog extends Dialog {

    final ListView _phoneNumberView;
    Button _smsButton;
    Button _cloudButton;
    ImageButton _closeButton;
    String _latitude;
    String _longitude;
    String _message;
    ICallBack _callBack;
    List<PhoneNumber> _phoneNumbers;
    Context _context;

    public AlternateFlareOptionsDialog(Context context) {
        super(context);
        setContentView(R.layout.alternate_flare_view);

        _context = context;
        _phoneNumberView = (ListView)findViewById(R.id.phoneNumbers);
        _smsButton = (Button)findViewById(R.id.smsButton);
        _cloudButton = (Button)findViewById(R.id.cloudButton);
        _closeButton = (ImageButton)findViewById(R.id.closeDialog);
    }

    public void SetData(String latitude,String longitude,String message, ICallBack callBack,List<PhoneNumber> phoneNumbers) {
        _latitude = latitude;
        _longitude = longitude;
        _message = message;
        _callBack = callBack;
        _phoneNumbers = phoneNumbers;

        setUpScreen();
    }

    private void setUpScreen() {
        // Set up list
        final List<PhoneNumber> finalNumbers = _phoneNumbers;
        final ArrayAdapter<PhoneNumber> numbersAdapter = new PhoneNumberAdapter(_context, R.layout.number_view, finalNumbers);
        _phoneNumberView.setAdapter(numbersAdapter);
        _phoneNumberView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                PhoneNumber number = (PhoneNumber) itemAtPosition;
                number.isSelected = true;

                numbersAdapter.notifyDataSetChanged();
            }
        });

        // Set up buttons
        _smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sms();
                close();
            }
        });

        _cloudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cloud();
                close();
            }
        });

        _closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
    }

    private void sms() {
        for(PhoneNumber phone : _phoneNumbers) {
            if(!phone.isSelected)
                continue;

            SmsManager m = SmsManager.getDefault();
            m.sendTextMessage(phone.number,null,_message+" http://maps.google.com/?q="+_latitude+","+_longitude+" "
                    +"  "+"Sent from Flare",null,null);
        }
    }

    private void cloud() {
        // twilio

    }

    private void close() {
        _callBack.CallBack();
        dismiss();
    }
}
