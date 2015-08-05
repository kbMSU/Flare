package flaregradle.myapp.com.Flare.Utilities;

import android.content.SharedPreferences;
import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import flaregradle.myapp.com.Flare.DataItems.Contact;
import flaregradle.myapp.com.Flare.DataItems.Group;
import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;

public class DataStorageHandler {
    private static DataStorageHandler instance = null;
    protected DataStorageHandler(){}

    public static DataStorageHandler getInstance() {
        if(instance == null){
            instance = new DataStorageHandler();
            instance.AllContacts = new TreeMap<>();
            instance.SelectedContacts = new ArrayList<>();
            instance.SavedContactGroups = new HashMap<>();
            instance.ContactNumbersWithFlare = new HashMap<>();
        }
        return instance;
    }

    public static SharedPreferences Preferences;
    public TreeMap<String,Contact> AllContacts;
    public ArrayList<Contact> SelectedContacts;
    public String registrationId;
    public String thisPhone;
    public static HashMap<String,Group> SavedContactGroups;
    public static HashMap<String,PhoneNumber> ContactNumbersWithFlare;
    public Location CurrentLocation;
    public boolean Registered;

    public static Contact findContact(String phoneNumber) {
        for(Contact contact : getInstance().AllContacts.values()) {
            for(PhoneNumber phone : contact.allPhoneNumbers) {
                String number = phone.number;

                String finalPhone = "";
                for (Character c : number.toCharArray()) {
                    if(Character.isDigit(c)) {
                        finalPhone+=c;
                    }
                }
                if(finalPhone.contains(phoneNumber))
                    return contact;
            }
        }
        return null;
    }

    public static void setupPreferences() {
        _defaultDeclineResponse = Preferences.getString("DefaultDeclineResponse", "Sorry, I can't make it");
        _defaultAcceptResponse = Preferences.getString("DefaultAcceptResponse","I will be there ASAP");
        _notificationId = Preferences.getInt("notificationId", 1);

        //wipeGroups();

        String json = Preferences.getString("SavedContactGroups",null);
        if(json != null) {
            Gson gson = new Gson();
            HashMap<String,Group> groups = gson.fromJson(json,new TypeToken<HashMap<String,Group>>(){}.getType());
            if(groups != null)
                SavedContactGroups = groups;
        }

        String contactsWithFlare = Preferences.getString("ContactsWithFlare",null);
        if(contactsWithFlare != null) {
            Gson gson = new Gson();
            HashMap<String,PhoneNumber> contacts = gson.fromJson(contactsWithFlare,new TypeToken<HashMap<String,PhoneNumber>>(){}.getType());
            if(contacts != null)
                ContactNumbersWithFlare = contacts;
        }
    }

    private static void wipeGroups() {
        Gson gson = new Gson();
        String json = gson.toJson(null);
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("SavedContactGroups",json);
        editor.apply();
    }

    public void saveContactGroup(Group group) {
        for (Contact c : group.Contacts)
            c.photo = null;

        SavedContactGroups.put(group.Name, group);
        writeGroupsToPreferences();
    }

    public void deleteContactGroup(String groupName) {
        SavedContactGroups.remove(groupName);
        writeGroupsToPreferences();
    }

    public void writeGroupsToPreferences() {
        Gson gson = new Gson();
        String json = gson.toJson(SavedContactGroups);
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("SavedContactGroups", json);
        editor.apply();
    }

    public void saveContactNumbersWithFlare(PhoneNumber number) {
        ContactNumbersWithFlare.put(number.number,number);
        writeContactsWithFlareToPreferences();
    }

    public void deleteContactNumbersWithFlare(PhoneNumber number) {
        ContactNumbersWithFlare.remove(number.number);
        writeContactsWithFlareToPreferences();
    }

    private void writeContactsWithFlareToPreferences() {
        Gson gson = new Gson();
        String json = gson.toJson(ContactNumbersWithFlare);
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("ContactsWithFlare",json);
        editor.apply();
    }

    public static boolean doesNumberHaveFlare(String phone) {
        return ContactNumbersWithFlare.containsKey(phone);
    }

    private static Integer _notificationId = 1;
    public Integer getNotificationId() {
        _notificationId++;

        if(_notificationId > 10)
            _notificationId = 1;

        if(Preferences != null) {
            SharedPreferences.Editor editor = Preferences.edit();
            editor.putInt("notificationId",_notificationId);
            editor.apply();
        }

        return _notificationId;
    }

    private static String _defaultDeclineResponse;
    public String GetDefaultDeclineResponse() {
        return _defaultDeclineResponse;
    }
    public void SetDefaultDeclineResponse(String decline) {
        _defaultDeclineResponse = decline;

        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("DefaultDeclineResponse",_defaultDeclineResponse);
        editor.apply();
    }

    private static String _defaultAcceptResponse;
    public String GetDefaultAcceptResponse() {
        return _defaultAcceptResponse;
    }
    public void SetDefaultAcceptResponse(String accept) {
        _defaultAcceptResponse = accept;

        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("DefaultAcceptResponse",_defaultAcceptResponse);
        editor.apply();
    }
}
