package flaregradle.myapp.com.Flare.Utilities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.*;

import flaregradle.myapp.com.Flare.DataItems.Contact;

public class ContactsHandler {

    private static final String[] PROJECTION = new String[] {
      ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
      ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
      ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER,
      ContactsContract.CommonDataKinds.Phone.NUMBER,
      ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    };

    private ContentResolver _contentResolver;
    private Bitmap _defaultContactImage;

    public ContactsHandler(ContentResolver contentResolver){
        _contentResolver = contentResolver;
    }

    public void setDefaultImage(Bitmap image){
        _defaultContactImage = image;
    }

    public TreeMap<String,Contact> getContacts(){
        TreeMap<String,Contact> contacts = new TreeMap<>();
        try {
            Cursor cur = _contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,PROJECTION,null,null,null);
            Integer id_index = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            Integer name_index = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
            Integer hasPhone_index = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            Integer number_index = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            Integer photo_index = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);

            while(cur.moveToNext()) {
                Integer hasPhoneNumber = cur.getInt(hasPhone_index);
                if(hasPhoneNumber.equals(0))
                    continue;

                String name = cur.getString(name_index);
                String number = cur.getString(number_index);

                if(contacts.containsKey(name)) {
                    Contact c = contacts.get(name);
                    c.allPhoneNumbers.add(number);
                    continue;
                }

                String id = cur.getString(id_index);
                byte[] photo = openPhoto(Long.parseLong(id)); //cur.getBlob(photo_index);

                Contact contact = new Contact();
                contact.id = id;
                contact.name = name;
                contact.phoneNumber = number;
                contact.allPhoneNumbers.add(number);

                if(photo != null) {
                    Bitmap image = BitmapFactory.decodeByteArray(photo,0,photo.length);
                    if(image != null) {
                        contact.photo = Bitmap.createScaledBitmap(image,(int)(image.getWidth()*1.5),(int)(image.getHeight()*1.5),false);
                    }
                    else
                        contact.photo = _defaultContactImage;
                } else {
                    contact.photo = _defaultContactImage;
                }

                contacts.put(name, contact);
            }

            cur.close();
        } catch (Exception ex) {
            Log.e("GettingContacts",ex.getMessage());
        }
        return contacts;
    }

    public byte[] openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = _contentResolver.query(photoUri,new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        byte[] photo = null;
        try {
            if (cursor.moveToFirst()) {
                photo = cursor.getBlob(0);
            }
        } catch (Exception ex){
            Log.e("OpenPhoto",ex.getMessage());
        }
        cursor.close();
        return photo;
    }
}
