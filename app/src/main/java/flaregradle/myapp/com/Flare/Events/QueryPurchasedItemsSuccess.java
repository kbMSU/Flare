package flaregradle.myapp.com.Flare.Events;

import java.util.ArrayList;

/**
 * Created by Karthik on 1/30/16.
 */
public class QueryPurchasedItemsSuccess {
    private ArrayList<String> _purchasedItems;

    public QueryPurchasedItemsSuccess(ArrayList<String> items) {
        _purchasedItems = items;
    }

    public ArrayList<String> getPurchasedItems() { return _purchasedItems; }
}
