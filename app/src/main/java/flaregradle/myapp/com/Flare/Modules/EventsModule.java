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

    static Bus eventBus = new Bus();

    public static void Post(Object event) {
        eventBus.post(event);
    }

    public static void Register(Object activity) {
        eventBus.register(activity);
    }

    public static void UnRegister(Object activity) {
        eventBus.unregister(activity);
    }
}
