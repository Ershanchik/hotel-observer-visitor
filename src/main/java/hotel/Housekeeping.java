package hotel;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public class Housekeeping extends EventSubject implements Observer<Event> {
    private final Hotel hotel;
    private final Set<String> dirtyRooms = ConcurrentHashMap.newKeySet();
    public Housekeeping(Hotel hotel) { this.hotel = hotel; }
    @Override public void update(Event e) {
        if (e.type() == Event.Type.ROOM_DIRTY) dirtyRooms.add(e.roomId()); else dirtyRooms.remove(e.roomId());
        hotel.floors().stream().filter(f -> f.number() == e.floor()).findFirst().ifPresent(floor -> {
            List<String> plan = floor.accept(new CleaningPlanVisitor(dirtyRooms));
            publish(new Event(Event.Type.CLEANING_PLAN, "", e.floor(), plan.size(), String.join(",", plan)));
        });
    }
}
