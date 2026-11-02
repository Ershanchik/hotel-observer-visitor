package hotel;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
public class FrontDesk implements Observer<Event> {
    private final LongAdder bookings = new LongAdder();
    private final Set<String> readyRooms = ConcurrentHashMap.newKeySet();
    @Override public void update(Event e) {
        switch (e.type()) {
            case BOOKING -> bookings.increment();
            case ROOM_CLEANED -> readyRooms.add(e.roomId());
            case ROOM_DIRTY -> readyRooms.remove(e.roomId());
            default -> { }
        }
    }
    public long bookings() { return bookings.sum(); }
    public boolean isReady(String roomId) { return readyRooms.contains(roomId); }
}
