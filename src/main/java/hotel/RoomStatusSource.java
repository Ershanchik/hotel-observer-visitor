package hotel;
public class RoomStatusSource extends EventSubject {
    public void markDirty(String roomId, int floor) { publish(new Event(Event.Type.ROOM_DIRTY, roomId, floor, 0, "")); }
    public void markCleaned(String roomId, int floor) { publish(new Event(Event.Type.ROOM_CLEANED, roomId, floor, 0, "")); }
}
