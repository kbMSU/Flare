package flaregradle.myapp.com.Flare.Events;

/**
 * Created by Karthik on 1/22/16.
 */
public class ParseError {
    private Exception _exception;

    public ParseError(Exception ex) { _exception = ex; }

    public Exception getException() { return _exception; }
}
