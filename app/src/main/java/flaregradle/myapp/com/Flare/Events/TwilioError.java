package flaregradle.myapp.com.Flare.Events;

public class TwilioError extends EventError {
    public TwilioError(Exception ex) {
        super(ex);
    }
}
