package flaregradle.myapp.com.Flare.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.MyApp.Flare.R;

import butterknife.Bind;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class SettingsSetup extends AppCompatActivity {

    @Bind(R.id.select_text_message)
    CheckBox textMessageCheckBox;
    @Bind(R.id.select_cloud_message)
    CheckBox cloudMessageCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_setup);

        textMessageCheckBox.setChecked(true);

        textMessageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    cloudMessageCheckBox.setChecked(false);
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
