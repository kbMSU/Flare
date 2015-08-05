package flaregradle.myapp.com.Flare.Interfaces;

import java.util.ArrayList;

import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;

/**
 * Created by Karthik on 8/2/15.
 */
public interface ISendFlare extends ICallBack {
    void SendAlternateFlare(ArrayList<PhoneNumber> phoneNumbers, String message);
}
