package flaregradle.myapp.com.Flare.Modules;

import com.squareup.otto.Bus;

public class EventsModule {
    private static EventsModule eventsModule;
    private EventsModule() {}
    public static EventsModule getInstance() {
        if(eventsModule == null) {
            eventsModule = new EventsModule();
        }
        return eventsModule;
    }

    Bus eventBus = new Bus();

    public void Post(Object event) {
        eventBus.post(event);
    }

    public void Register(Object activity) {
        eventBus.register(activity);
    }

    public void UnRegister(Object activity) {
        eventBus.unregister(activity);
    }
}
