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

    private ContentResolver _contentResolver;
    private Bitmap _defaultContactImage;

    public ContactsHandler(ContentResolver contentResolver){
        _contentResolver = contentResolver;
    }

    public void setDefaultImage(Bitmap image){
        _defaultContactImage = image;
    }

    public TreeMap<String,Contact> getContacts(){
        Cursor cur = _contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                new String[] {ContactsContract.Contacts._ID,ContactsContract.Contacts.DISPLAY_NAME_PRIMARY}, null, null, null);
        TreeMap<String,Contact> contacts = new TreeMap<>();
        try {
            if (cur.getCount() > 0) {
                Integer id_index = cur.getColumnIndex(ContactsContract.Contacts._ID);
                Integer name_index = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);

                while (cur.moveToNext()) {
                    String id = cur.getString(id_index);
                    String name = cur.getString(name_index);

                    byte[] photo = null;
                    try { photo = openPhoto(Long.parseLong(id)); } catch (Exception ex) {}

                    Cursor phones = _contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                    Integer phoneIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    if (phones.moveToNext()) {
                        try {
                            String number = phones.getString(phoneIndex);

                            Contact contact = new Contact();
                            contact.id = id;
                            contact.name = name;
                            contact.phoneNumber = number;

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
                        } catch (Exception ex){
                            Log.e("GettingPhoneNumber",ex.getMessage());
                        }
                    }
                    phones.close();
                }
            }
        } catch (Exception ex) {
            Log.e("GettingContacts",ex.getMessage());
        }
        cur.close();
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
