package hotel;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
public class AuditLog implements Observer<Event> {
    private final Map<Event.Type, LongAdder> counts = new EnumMap<>(Event.Type.class);
    public AuditLog() {
        for (Event.Type type : Event.Type.values()) counts.put(type, new LongAdder());
    }
    @Override public void update(Event e) { counts.get(e.type()).increment(); }
    public long count(Event.Type type) { return counts.get(type).sum(); }
}
