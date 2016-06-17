package flaregradle.myapp.com.Flare.Utilities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.TreeMap;

import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;

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
        _defaultContactImage = DataStorageHandler.DefaultContactImage;
    }

    public TreeMap<String,Contact> getContacts() {
        TreeMap<String,Contact> contacts = new TreeMap<>();

        Cursor cur = _contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,PROJECTION,null,null,null);
        Integer id_index = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
        Integer name_index = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
        Integer hasPhone_index = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
        Integer number_index = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        while(cur.moveToNext()) {
            Integer hasPhoneNumber = cur.getInt(hasPhone_index);
            if(hasPhoneNumber.equals(0))
                continue;

            String name = cur.getString(name_index);
            String unformattedNumber = cur.getString(number_index);

            String number = "";
            for(char c: unformattedNumber.toCharArray()) {
                if(Character.isDigit(c))
                    number += c;
            }

            if(contacts.containsKey(name)) {
                Contact c = contacts.get(name);
                c.allPhoneNumbers.add(new PhoneNumber(false,number,false));
                continue;
            }

            String id = cur.getString(id_index);
            byte[] photo = openPhoto(Long.parseLong(id));

            Contact contact = new Contact();
            contact.id = id;
            contact.name = name;
            contact.phoneNumber = new PhoneNumber(false,number,true);
            contact.allPhoneNumbers.add(new PhoneNumber(false,number,true));
            contact.hasFlare = false;

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
