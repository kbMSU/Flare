package flaregradle.myapp.com.flare.Utilities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.*;

import flaregradle.myapp.com.flare.DataItems.Contact;

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
        Cursor cur = _contentResolver.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        TreeMap<String,Contact> contacts = new TreeMap<>();
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                Integer id_index = cur.getColumnIndex(ContactsContract.Contacts._ID);
                if(id_index < 0)
                    continue;

                Integer name_index = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
                if(name_index < 0)
                    continue;

                String id = cur.getString(id_index);
                String name = cur.getString(name_index);

                Integer hasPhone_index = cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                if(hasPhone_index < 0)
                    continue;

                byte[] photo = null;
                try { photo = openPhoto(Long.parseLong(id)); } catch (Exception ex) {}

                Cursor phones = _contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                while (phones.moveToNext()) {
                    String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    try {
                        Contact contact = new Contact();
                        contact.id = id;
                        contact.name = name;
                        contact.phoneNumber = number;

                        if(photo != null) {
                            Bitmap image = BitmapFactory.decodeByteArray(photo,0,photo.length);
                            if(image != null)
                            {
                                contact.photo = Bitmap.createScaledBitmap(image,(int)(image.getWidth()*1.5),(int)(image.getHeight()*1.5),false);
                            }
                            else
                                contact.photo = _defaultContactImage;
                        } else {
                            contact.photo = _defaultContactImage;
                        }

                        contacts.put(name, contact);
                    } catch (Exception ex){}

                    phones.close();
                    break;
                }

            }
            cur.close();
        }
        return contacts;
    }

    public byte[] openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = _contentResolver.query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                return data;
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
