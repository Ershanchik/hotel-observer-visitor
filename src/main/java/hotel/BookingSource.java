package hotel;
public class BookingSource extends EventSubject {
    public void book(String roomId, int floor, double amount) {
        publish(new Event(Event.Type.BOOKING, roomId, floor, amount, ""));
    }
}
