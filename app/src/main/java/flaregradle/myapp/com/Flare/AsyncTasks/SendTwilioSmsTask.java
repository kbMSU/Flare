package flaregradle.myapp.com.Flare.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.util.ArrayList;
import java.util.List;

import flaregradle.myapp.com.Flare.Events.TwilioError;
import flaregradle.myapp.com.Flare.Events.TwilioSuccess;
import flaregradle.myapp.com.Flare.Modules.EventsModule;

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
        MobileServiceClient client;

        try {
            client = new MobileServiceClient("https://flareservice.azure-mobile.net/","vAymygcCyvnOQrDzLOEjyOQGIxIJMm78",context);
        } catch (Exception ex) {
            exception = ex;
            return null;
        }

        for(String to : recipient) {
            try {
                List<Pair<String,String>> parameters = new ArrayList<>();
                parameters.add(new Pair<>("to", to));
                parameters.add(new Pair<>("body", body));
                ListenableFuture<JsonElement> result = client.invokeApi("Messages", "Post", parameters);
                result.isDone();
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
            eventsModule.Post(new TwilioError(exception));
        else
            eventsModule.Post(new TwilioSuccess());
    }
}
