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

public class DataStorageHandler {
    private static DataStorageHandler instance = null;
    protected DataStorageHandler(){}

    public static DataStorageHandler getInstance() {
        if(instance == null){
            instance = new DataStorageHandler();
            instance.AllContacts = new TreeMap<>();
            instance.SelectedContacts = new ArrayList<>();
            instance.SavedContactGroups = new HashMap<>();
        }
        return instance;
    }

    public static SharedPreferences Preferences;
    public TreeMap<String,Contact> AllContacts;
    public ArrayList<Contact> SelectedContacts;
    public String registrationId;
    public String thisPhone;
    public static HashMap<String,Group> SavedContactGroups;
    public Location CurrentLocation;

    public static Contact findContact(String phoneNumber) {
        for(Contact contact : getInstance().AllContacts.values()) {
            String finalPhone = "";
            for (Character c : contact.phoneNumber.toCharArray()) {
                if(Character.isDigit(c)) {
                    finalPhone+=c;
                }
            }
            if(finalPhone.contains(phoneNumber))
                return contact;
        }
        return null;
    }

    public static void setupPreferences() {
        _sendFlareTextResponse = Preferences.getBoolean("SendFlareTextResponse",true);
        _defaultDeclineResponse = Preferences.getString("DefaultDeclineResponse","Sorry, I can't make it");
        _defaultAcceptResponse = Preferences.getString("DefaultAcceptResponse","I will be there ASAP");
        _notificationId = Preferences.getInt("notificationId",1);

        String json = Preferences.getString("SavedContactGroups",null);
        if(json != null) {
            Gson gson = new Gson();
            HashMap<String,Group> groups = gson.fromJson(json,new TypeToken<HashMap<String,Group>>(){}.getType());
            if(groups != null)
                SavedContactGroups = groups;
        }
    }

    public void saveContactGroup(Group group) {
        for (Contact c : group.Contacts)
            c.photo = null;

        SavedContactGroups.put(group.Name,group);
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
        editor.putString("SavedContactGroups",json);
        editor.apply();
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

    private static boolean _sendFlareTextResponse;
    public boolean GetSendFlareTextResponse() {
        return _sendFlareTextResponse;
    }
    public void SetSendFlareTextResponse(boolean value) {
        _sendFlareTextResponse = value;

        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("SendFlareTextResponse",_sendFlareTextResponse);
        editor.apply();
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
