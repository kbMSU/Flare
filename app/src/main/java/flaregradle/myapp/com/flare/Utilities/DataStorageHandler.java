package flaregradle.myapp.com.Flare.Utilities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;

import com.android.vending.billing.IInAppBillingService;
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
        }
        return instance;
    }

    private static String _defaultDeclineResponse;
    private static Integer _notificationId = 1;
    private static String _defaultAcceptResponse;
    private static boolean _registered;
    private static boolean _phoneNumberVerified;
    private static boolean _sendCloudMessage;
    private static String _countryCode;
    private static String _phoneNumber;
    private static boolean _checkContactsWithFlare;
    private static boolean _haveAskedToCheckContactsWithFlare;
    private static boolean _haveAskedToSaveTheUsersInformation;
    private static boolean _canWeSaveTheUsersInformation;
    private static boolean _havePurchasedAdFreeUpgrade;

    public static SharedPreferences Preferences;
    public static TreeMap<String,Contact> AllContacts;
    public static ArrayList<Contact> SelectedContacts;
    public static HashMap<String,Group> SavedContactGroups;
    public static Location CurrentLocation;
    public static boolean IsSetupComplete;
    public static IInAppBillingService BillingService;
    public static Bitmap DefaultContactImage;

    //region Setup
    public static void setupPreferences() {
        _defaultDeclineResponse = Preferences.getString("DefaultDeclineResponse", "Sorry, I can't make it");
        _defaultAcceptResponse = Preferences.getString("DefaultAcceptResponse","I will be there ASAP");
        _notificationId = Preferences.getInt("notificationId", 1);
        _sendCloudMessage = Preferences.getBoolean("cloudMessage", false);
        _phoneNumberVerified = Preferences.getBoolean("verified", false);
        _registered = Preferences.getBoolean("registered",false);
        _countryCode = Preferences.getString("countryCode", "");
        _phoneNumber = Preferences.getString("phoneNumber","");
        _checkContactsWithFlare = Preferences.getBoolean("checkContactsWithFlare", false);
        _haveAskedToCheckContactsWithFlare = Preferences.getBoolean("haveAskedToCheckContactsWithFlare",false);
        _canWeSaveTheUsersInformation = Preferences.getBoolean("canWeSaveTheUsersInformation",false);
        _haveAskedToSaveTheUsersInformation = Preferences.getBoolean("haveAskedToSaveTheUsersInformation",false);
        _havePurchasedAdFreeUpgrade = Preferences.getBoolean("havePurchasedAdFreeUpgrade",false);

        // Get saved contact groups
        String json = Preferences.getString("SavedContactGroups", null);
        if(json != null) {
            Gson gson = new Gson();
            HashMap<String,Group> groups = gson.fromJson(json,new TypeToken<HashMap<String,Group>>(){}.getType());
            if(groups != null)
                SavedContactGroups = groups;
        }

        IsSetupComplete = true;
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

    public static void writeGroupsToPreferences() {
        Gson gson = new Gson();
        String json = gson.toJson(SavedContactGroups);
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("SavedContactGroups", json);
        editor.apply();
    }

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

    public static void savePhoneNumber(String code,String phone) {
        _countryCode = code;
        _phoneNumber = phone;

        SharedPreferences.Editor editor = Preferences.edit();
        editor.putString("countryCode",_countryCode);
        editor.putString("phoneNumber", _phoneNumber);
        editor.apply();
    }

    public static String getCountryCode() {
        return _countryCode;
    }

    public static String getPhoneNumber() {
        return _phoneNumber;
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
        editor.putBoolean("verified", _phoneNumberVerified);
        editor.apply();
    }

    public static boolean IsRegistered() {
        return _registered;
    }
    public static void SetRegistered() {
        _registered = true;
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("registered", _registered);
        editor.apply();
    }
    public static void SetNotRegistered() {
        _registered = false;
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("registered", _registered);
        editor.apply();
    }

    public static boolean CanSendCloudMessage() { return _sendCloudMessage;}
    public static void SetSendCloudMessage(boolean val) {
        _sendCloudMessage = val;
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("cloudMessage",_sendCloudMessage);
        editor.apply();
    }

    public static boolean CanCheckContactsForFlare() {return _checkContactsWithFlare; }
    public static void SetCanCheckContactsForFlare(boolean val) {
        _checkContactsWithFlare = val;
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("checkContactsWithFlare",_checkContactsWithFlare);
        editor.apply();
    }

    public static boolean HaveAskedToCheckContactsWithFlare() {return _haveAskedToCheckContactsWithFlare; }
    public static void SetHaveAskedToCheckContactsWithFlare(boolean val) {
        _haveAskedToCheckContactsWithFlare = val;
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("haveAskedToCheckContactsWithFlare",_haveAskedToCheckContactsWithFlare);
        editor.apply();
    }
    public static boolean CanWeSaveTheUsersInformation() {return _canWeSaveTheUsersInformation; }
    public static void SetCanWeSaveTheUsersInformation(boolean val) {
        _canWeSaveTheUsersInformation = val;
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("canWeSaveTheUsersInformation",_canWeSaveTheUsersInformation);
        editor.apply();
    }

    public static boolean HaveAskedToSaveTheUsersInformation() {return _haveAskedToSaveTheUsersInformation; }
    public static void SetHaveAskedToSaveTheUsersInformation(boolean val) {
        _haveAskedToSaveTheUsersInformation = val;
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("haveAskedToSaveTheUsersInformation",_haveAskedToSaveTheUsersInformation);
        editor.apply();
    }

    public static boolean HavePurchasedAdFreeUpgrade() {return _havePurchasedAdFreeUpgrade;}
    public static void SetHavePurchasedAdFreeUpgrade(boolean val) {
        _havePurchasedAdFreeUpgrade = val;
        SharedPreferences.Editor editor = Preferences.edit();
        editor.putBoolean("havePurchasedAdFreeUpgrade",_havePurchasedAdFreeUpgrade);
        editor.apply();
    }
    //endregion
}
