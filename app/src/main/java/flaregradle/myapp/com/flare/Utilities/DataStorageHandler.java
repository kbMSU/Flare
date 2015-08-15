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
            AllContacts = new TreeMap<>();
            SelectedContacts = new ArrayList<>();
            SavedContactGroups = new HashMap<>();
            ContactNumbersWithFlare = new HashMap<>();
        }
        return instance;
    }

    private static String _defaultDeclineResponse;
    private static Integer _notificationId = 1;
    private static String _defaultAcceptResponse;
    private static boolean _registered;
    private static boolean _phoneNumberVerified;

    public static SharedPreferences Preferences;
    public static TreeMap<String,Contact> AllContacts;
    public static ArrayList<Contact> SelectedContacts;
    public static String registrationId;
    public static String thisPhone;
    public static HashMap<String,Group> SavedContactGroups;
    public static HashMap<String,PhoneNumber> ContactNumbersWithFlare;
    public static Location CurrentLocation;

    //region Setup
    public static void setupPreferences() {
        _defaultDeclineResponse = Preferences.getString("DefaultDeclineResponse", "Sorry, I can't make it");
        _defaultAcceptResponse = Preferences.getString("DefaultAcceptResponse","I will be there ASAP");
        _notificationId = Preferences.getInt("notificationId", 1);

        _registered = Preferences.getBoolean("registered", false);
        _registered = Preferences.getBoolean("verified",false);

        // Get saved contact groups
        String json = Preferences.getString("SavedContactGroups", null);
        if(json != null) {
            Gson gson = new Gson();
            HashMap<String,Group> groups = gson.fromJson(json,new TypeToken<HashMap<String,Group>>(){}.getType());
            if(groups != null)
                SavedContactGroups = groups;
        }

        // Get contacts with flare
        String contactsWithFlare = Preferences.getString("ContactsWithFlare", null);
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
        editor.putString("SavedContactGroups", json);
        editor.apply();
    }
    //endregion

    //region Methods
    public static Contact findContact(String phoneNumber) {
        for(Contact contact : AllContacts.values()) {
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

    public static boolean doesNumberHaveFlare(String phone) {
        return ContactNumbersWithFlare.containsKey(phone);
    }
    //endregion

    //region Preferences I/O
    public static void writeGroupsToPreferences() {
        Gson gson = new Gson();
        String json = gson.toJson(SavedContactGroups);
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("SavedContactGroups", json);
        editor.apply();
    }

    private static void writeContactsWithFlareToPreferences() {
        Gson gson = new Gson();
        String json = gson.toJson(ContactNumbersWithFlare);
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("ContactsWithFlare", json);
        editor.apply();
    }
    //endregion

    //region Save to Preferences
    public static void saveContactGroup(Group group) {
        for (Contact c : group.Contacts)
            c.photo = null;

        SavedContactGroups.put(group.Name, group);
        writeGroupsToPreferences();
    }

    public static void deleteContactGroup(String groupName) {
        SavedContactGroups.remove(groupName);
        writeGroupsToPreferences();
    }

    public static void saveContactNumbersWithFlare(PhoneNumber number) {
        ContactNumbersWithFlare.put(number.number, number);
        writeContactsWithFlareToPreferences();
    }

    public static void deleteContactNumbersWithFlare(PhoneNumber number) {
        ContactNumbersWithFlare.remove(number.number);
        writeContactsWithFlareToPreferences();
    }

    public static Integer getNotificationId() {
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

    public static String GetDefaultDeclineResponse() {
        return _defaultDeclineResponse;
    }
    public static void SetDefaultDeclineResponse(String decline) {
        _defaultDeclineResponse = decline;

        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("DefaultDeclineResponse",_defaultDeclineResponse);
        editor.apply();
    }

    public static String GetDefaultAcceptResponse() {
        return _defaultAcceptResponse;
    }
    public static void SetDefaultAcceptResponse(String accept) {
        _defaultAcceptResponse = accept;

        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("DefaultAcceptResponse",_defaultAcceptResponse);
        editor.apply();
    }

    public static boolean IsPhoneNumberVerified() { return _phoneNumberVerified; }
    public static void SetPhoneNumberVerified() {
        _phoneNumberVerified = true;
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("verified",_phoneNumberVerified);
        editor.apply();
    }

    public static boolean IsRegistered() { return _registered; }
    public static void SetRegistered() {
        _registered = true;
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("registered",_registered);
        editor.apply();
    }
    //endregion
}
