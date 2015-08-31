package flaregradle.myapp.com.Flare.Events;

public class TwilioError {
    private Exception exception;

    public TwilioError(Exception ex) {
        exception = ex;
    }

    public Exception getException() {
        return exception;
    }
}
