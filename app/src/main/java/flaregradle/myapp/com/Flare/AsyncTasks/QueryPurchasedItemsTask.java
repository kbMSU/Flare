package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.vending.billing.IInAppBillingService;

import java.util.ArrayList;

import flaregradle.myapp.com.Flare.Events.QueryPurchasedItemsError;
import flaregradle.myapp.com.Flare.Events.QueryPurchasedItemsSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class QueryPurchasedItemsTask extends AsyncTask<Context, Void, Void> {

    private IInAppBillingService _service;
    private Context _context;
    private Exception _exception;
    private ArrayList<String> _ownedItemNames;

    public QueryPurchasedItemsTask() {
        _service = DataStorageHandler.BillingService;
    }

    @Override
    protected Void doInBackground(Context... params) {
        _context = params[0];

        try {
            Bundle ownedItems = _service.getPurchases(3, _context.getPackageName(), "inapp", null);

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                _ownedItemNames = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            } else {
                throw new Exception("service returned RESPONSE_CODE 0");
            }
        } catch (Exception ex) {
            _exception = ex;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if(_exception == null) {
            EventsModule.Post(new QueryPurchasedItemsSuccess(_ownedItemNames));
        } else {
            EventsModule.Post(new QueryPurchasedItemsError(_exception));
        }
    }
}
