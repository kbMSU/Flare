package flaregradle.myapp.com.Flare.Events;

/**
 * Created by Karthik on 1/25/16.
 */
public abstract class EventError {
    private Exception _exception;

    public EventError(Exception ex) {
        _exception = ex;
    }

    public Exception getException() {
        return _exception;
    }
}
