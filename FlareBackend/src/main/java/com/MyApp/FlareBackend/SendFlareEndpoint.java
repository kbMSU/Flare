package com.MyApp.FlareBackend;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import static com.MyApp.FlareBackend.OfyService.ofy;

@Api(name = "sendflare", version = "v1", namespace = @ApiNamespace(ownerDomain = "FlareBackend.MyApp.com", ownerName = "FlareBackend.MyApp.com", packagePath=""))
public class SendFlareEndpoint {

    private static final String API_KEY = System.getProperty("gcm.api.key");

    @ApiMethod(name = "sendFlare")
    public void sendFlare(@Named("regId") String myRegId,@Named("phoneNumber") String phoneNumber,
                          @Named("latitude") String latitude, @Named("longitude") String longitude,
                          @Named("text") String text) throws IOException {
        Sender sender = new Sender(API_KEY);
        RegisteredDevice thisDevice = findDeviceById(myRegId);
        RegisteredDevice device = findDevice(phoneNumber);
        Message message = new Message.Builder()
                            .addData("latitude",latitude)
                            .addData("longitude",longitude)
                            .addData("phone",thisDevice.phoneNumber)
                            .addData("text",text).build();
        sender.send(message,device.getRegistrationId(),5);
    }

    @ApiMethod(name = "findDevice")
    public RegisteredDevice findDevice(@Named("phoneNumber") String phone) throws IOException {
        RegisteredDevice record = ofy().load().type(RegisteredDevice.class).filter("phoneNumber", String.valueOf((phoneUtil.splitPhoneNumber(phone)[1]))).first().now();
        return record;
    }

    @ApiMethod(name = "findDeviceById")
    public RegisteredDevice findDeviceById(@Named("regId") String regId) {
        RegisteredDevice record = ofy().load().type(RegisteredDevice.class).filter("registrationId", regId).first().now();
        return record;
    }
}
