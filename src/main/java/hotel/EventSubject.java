package hotel;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
public class EventSubject implements Subject<Event.Type, Event> {
    private final Map<Event.Type, List<Observer<Event>>> observers = new EnumMap<>(Event.Type.class);
    public EventSubject() {
        for (Event.Type type : Event.Type.values()) observers.put(type, new CopyOnWriteArrayList<>());
    }
    @Override public void subscribe(Event.Type type, Observer<Event> observer) { observers.get(type).add(observer); }
    @Override public void unsubscribe(Event.Type type, Observer<Event> observer) { observers.get(type).remove(observer); }
    public void publish(Event event) { observers.get(event.type()).forEach(o -> o.update(event)); }
}
