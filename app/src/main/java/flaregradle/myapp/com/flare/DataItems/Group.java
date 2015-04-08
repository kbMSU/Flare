package flaregradle.myapp.com.flare.DataItems;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Karthik on 12/27/2014.
 */
public class Group implements Serializable {

    public ArrayList<Contact> Contacts;
    public String Name;

    public Group() {
        Contacts = new ArrayList<>();
    }
}
