package flaregradle.myapp.com.Flare.AsyncTasks;

import android.app.PendingIntent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.vending.billing.IInAppBillingService;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Karthik on 1/31/16.
 */
public class PurchaseAsyncTask extends AsyncTask<Context,Void,Void> {

    private Context _context;
    private String _itemId;
    private String _orderId;
    private String _devPayload;
    private Exception _exception;
    private IInAppBillingService mService;

    public PurchaseAsyncTask(IInAppBillingService service, String orderId) {
        mService = service;
        _orderId = orderId;
    }

    @Override
    protected Void doInBackground(Context... params) {
        _context = params[0];
        _devPayload = new BigInteger(130,new SecureRandom()).toString(32);

        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, _context.getPackageName(),_orderId, "inapp", _devPayload);
            int responseCode = buyIntentBundle.getInt("RESPONSE_CODE", 0);
            if(responseCode == 0) {
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

            } else {

            }
        } catch (Exception ex) {

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);


    }
}
