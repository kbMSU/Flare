package flaregradle.myapp.com.Flare.Events;

/**
 * Created by Karthik on 1/25/16.
 */
public class RegistrationError extends EventError {

    public RegistrationError(Exception ex) {
        super(ex);
    }
}
