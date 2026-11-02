package hotel;
import java.util.Objects;
public record Event(Type type, String roomId, int floor, double amount, String detail) {
    public enum Type { BOOKING, ROOM_DIRTY, ROOM_CLEANED, CLEANING_PLAN, REVENUE_SUMMARY }
    public Event { Objects.requireNonNull(type, "type"); }
}
