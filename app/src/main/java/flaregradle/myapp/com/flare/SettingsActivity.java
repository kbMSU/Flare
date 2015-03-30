package flaregradle.myapp.com.flare;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.MyApp.Flare.R;

import flaregradle.myapp.com.flare.Utilities.DataStorageHandler;


public class SettingsActivity extends ActionBarActivity {

    private DataStorageHandler _handler;
    private ToggleButton _toggle;
    private TextView _declineResponseTextView;
    private EditText _declineResponseEditText;
    private TextView _acceptResponseTextView;
    private EditText _acceptResponsetEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        _handler = DataStorageHandler.getInstance();
        _toggle = (ToggleButton)findViewById(R.id.send_text_toggle);
        _declineResponseTextView = (TextView)findViewById(R.id.default_decline_textview);
        _declineResponseEditText = (EditText)findViewById(R.id.default_decline_edittext);
        _acceptResponseTextView = (TextView)findViewById(R.id.default_accept_textview);
        _acceptResponsetEditText = (EditText)findViewById(R.id.default_accept_edittext);

        // Set up settings
        //_handler.setupPreferences();

        _toggle.setChecked(_handler.GetSendFlareTextResponse());

        _declineResponseTextView.setText(_handler.GetDefaultDeclineResponse());
        _declineResponseEditText.setText(_handler.GetDefaultDeclineResponse());
        _declineResponseEditText.setVisibility(View.GONE);

        _acceptResponseTextView.setText(_handler.GetDefaultAcceptResponse());
        _acceptResponsetEditText.setText(_handler.GetDefaultAcceptResponse());
        _acceptResponsetEditText.setVisibility(View.GONE);

        // Set up toolbar
        setTheToolbar();
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

    private void setTheToolbar() {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onSendResponseSettingsClick(View v) {
        _toggle.toggle();
        _handler.SetSendFlareTextResponse(_toggle.isChecked());
    }

    public void onSendResponseToggleClick(View v) {
        //ToggleButton toggle = (ToggleButton)findViewById(R.id.send_text_toggle);
        //toggle.toggle();

        //_handler.SetSendFlareTextResponse(toggle.isChecked());

        _toggle.toggle();
        _handler.SetSendFlareTextResponse(_toggle.isChecked());
    }

    public void onDeclineMessageEditClick(View v) {
        if(_declineResponseEditText.getVisibility() == View.GONE) {
            _declineResponseTextView.setVisibility(View.GONE);
            _declineResponseEditText.setVisibility(View.VISIBLE);
            _declineResponseEditText.setText(_handler.GetDefaultDeclineResponse());
            _declineResponseEditText.requestFocus();
        } else {
            _handler.SetDefaultDeclineResponse(_declineResponseEditText.getText().toString());
            _declineResponseTextView.setText(_handler.GetDefaultDeclineResponse());
            _declineResponseTextView.setVisibility(View.VISIBLE);
            _declineResponseEditText.setVisibility(View.GONE);
        }
    }

    public void onAcceptMessageEditClick(View v) {
        if(_acceptResponsetEditText.getVisibility() == View.GONE) {
            _acceptResponseTextView.setVisibility(View.GONE);
            _acceptResponsetEditText.setVisibility(View.VISIBLE);
            _acceptResponsetEditText.setText(_handler.GetDefaultAcceptResponse());
            _acceptResponsetEditText.requestFocus();
        } else {
            _handler.SetDefaultAcceptResponse(_acceptResponsetEditText.getText().toString());
            _acceptResponseTextView.setText(_handler.GetDefaultAcceptResponse());
            _acceptResponseTextView.setVisibility(View.VISIBLE);
            _acceptResponsetEditText.setVisibility(View.GONE);
        }
    }
}
