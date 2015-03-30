package com.MyApp.FlareBackend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.lang.reflect.Array;
import java.util.ArrayList;

@Entity
public class RegisteredDevice {

    @Id
    String Id;

    String phoneNumber;

    String countryCode;

    String registrationId;

    public RegisteredDevice() {}

    public RegisteredDevice(String regId,String number){
        Id = number;
        registrationId = regId;

        String[] split = phoneUtil.splitPhoneNumber(number);

        phoneNumber = split[1];
        countryCode = split[0];
    }

    public String getId() {return Id;}
    public String getPhoneNumber() {return phoneNumber;}
    public String getRegistrationId() {return registrationId;}
    public String getCountryCode() {return countryCode;}

    public void setPhoneNumber(String phone) {phoneNumber = phone;}
    public void setRegistrationId(String regId) {registrationId = regId;}
    public void setCountryCode(String code) {countryCode = code;}
}
