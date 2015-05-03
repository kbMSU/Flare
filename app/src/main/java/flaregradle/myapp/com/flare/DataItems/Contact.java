package flaregradle.myapp.com.Flare.DataItems;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Comparator;

public class Contact implements Comparator<Contact>,Comparable<Contact>,Serializable {
    public String phoneNumber;
    public String id;
    public String name;
    public boolean selected = false;
    public Bitmap photo;

    public Contact(Contact copy) {
        phoneNumber = copy.phoneNumber;
        id = copy.id;
        name = copy.name;
        selected = copy.selected;
        photo = copy.photo;
    }

    public Contact(){}

    @Override
    public int compare(Contact contact, Contact contact2) {
        return contact.name.compareTo(contact2.name);
    }

    @Override
    public int compareTo(Contact contact) {
        return this.name.compareTo(contact.name);
    }

}
