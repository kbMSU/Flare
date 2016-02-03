package flaregradle.myapp.com.Flare.Events;

/**
 * Created by Karthik on 1/30/16.
 */
public class QueryPurchasedItemsError extends EventError {
    public QueryPurchasedItemsError(Exception ex) {
        super(ex);
    }
}
