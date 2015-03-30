package com.MyApp.FlareBackend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import java.util.List;

import javax.inject.Named;
import static com.MyApp.FlareBackend.OfyService.ofy;

@Api(name = "deviceregistration", version = "v1", namespace = @ApiNamespace(ownerDomain = "FlareBackend.MyApp.com", ownerName = "FlareBackend.MyApp.com", packagePath=""))
public class DeviceRegistrationEndpoint {

    @ApiMethod(name = "registerDevice")
    public RegisteredDevice registerDevice(@Named("registrationId") String regId, @Named("phoneNumber") String phoneNumber){

        RegisteredDevice existingDevice = ofy().load().type(RegisteredDevice.class)
                                               .filter("Id", phoneNumber)
                                               .first().now();
        if(existingDevice != null){
            ofy().delete().entity(existingDevice).now();
        }

        RegisteredDevice device = new RegisteredDevice(regId,phoneNumber);
        ofy().save().entity(device).now();
        return device;
    }

    @ApiMethod(name = "deleteAllDevices")
    public void deleteAllDevices() {
        List<RegisteredDevice> devices = ofy().load().type(RegisteredDevice.class).list();
        ofy().delete().entities(devices).now();
    }

    @ApiMethod(name = "getAllDevices")
    public List<RegisteredDevice> getAllDevices() {
        List<RegisteredDevice> devices = ofy().load().type(RegisteredDevice.class).list();
        return devices;
    }
}
