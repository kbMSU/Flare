package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import flaregradle.myapp.com.Flare.Events.TwilioError;
import flaregradle.myapp.com.Flare.Events.TwilioSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;
import flaregradle.myapp.com.Flare.Utilities.DataStorageHandler;

public class SendTwilioSmsTask extends AsyncTask<Void,Void,Void> {
    private EventsModule eventsModule = EventsModule.getInstance();
    private Exception exception;
    private Context context;
    private List<String> recipient;
    private String body;

    public SendTwilioSmsTask(Context context,List<String> to, String body) {
        this.context = context;
        this.recipient = to;
        this.body = body;
    }

    @Override
    protected Void doInBackground(Void... params) {
        for(String to : recipient) {
            try {
                HashMap<String, String> smsParams = new HashMap<>();
                smsParams.put("to", to);
                smsParams.put("message", body);
                ParseCloud.callFunction("SendTwilioMessage",smsParams);
            } catch (Exception ex) {
                exception = ex;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if(exception != null)
             EventsModule.Post(new TwilioError(exception));
        else
            EventsModule.Post(new TwilioSuccess());
    }
}
