package flaregradle.myapp.com.Flare.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.MyApp.Flare.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;
import flaregradle.myapp.com.Flare.Utilities.PermissionHandler;

public class SettingsSetup extends AppCompatActivity {

    final int SEND_SMS_REQUEST = 1;

    @Bind(R.id.select_text_message) CheckBox textMessageCheckBox;
    @Bind(R.id.select_cloud_message) CheckBox cloudMessageCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_setup);
        ButterKnife.bind(this);

        final Activity thisActivity = this;

        cloudMessageCheckBox.setChecked(true);

        textMessageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (PermissionHandler.canSendSms(thisActivity)) {
                        cloudMessageCheckBox.setChecked(false);
                    } else {
                        PermissionHandler.requestSmsPermission(thisActivity,SEND_SMS_REQUEST);
                    }
                }
            }
        });
        cloudMessageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    textMessageCheckBox.setChecked(false);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SEND_SMS_REQUEST) {
            if (grantResults.length > 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cloudMessageCheckBox.setChecked(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doneClick(View view) {
        DataStorageHandler.SetSendCloudMessage(cloudMessageCheckBox.isChecked());
        Intent intent = new Intent(this,FlareHome.class);
        startActivity(intent);
    }
}
