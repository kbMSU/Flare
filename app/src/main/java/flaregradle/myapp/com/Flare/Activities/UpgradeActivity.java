package flaregradle.myapp.com.Flare.Activities;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import com.MyApp.Flare.R;
import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.SecureRandom;

import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class UpgradeActivity extends Activity {

    private String _devPayload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onPurchaseClick(View v) {
        _devPayload = new BigInteger(130,new SecureRandom()).toString(32);
        try {
            Bundle buyIntentBundle = DataStorageHandler.BillingService.getBuyIntent(3, getPackageName(), "upgrade", "inapp", _devPayload);
            int responseCode = buyIntentBundle.getInt("RESPONSE_CODE", 0);
            if(responseCode == 0) {
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
            } else {
                Toast.makeText(this,"Unable to make purchase",Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this,"Unable to connect to google play billing",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String product = jo.getString("productId");
                    String devPayload = jo.getString("developerPayload");
                    String orderId = jo.getString("orderId");

                    if(!product.equals("upgrade") || !devPayload.equals(_devPayload) ) {
                        // Not a unique order, check orderId also
                    } else {
                        DataStorageHandler.SetHavePurchasedAdFreeUpgrade(true);
                    }
                }
                catch (JSONException e) {
                    Toast.makeText(this,"We had a problem verifying the purchase",Toast.LENGTH_LONG);
                }
            }
        }
    }

    private void isBusy() {
        findViewById(R.id.busy_overlay).setVisibility(View.VISIBLE);
    }

    private void isNotBusy() {
        findViewById(R.id.busy_overlay).setVisibility(View.GONE);
    }

}
