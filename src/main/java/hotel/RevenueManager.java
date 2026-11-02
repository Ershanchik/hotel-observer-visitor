package hotel;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
public class RevenueManager extends EventSubject implements Observer<Event> {
    private final DoubleAdder total = new DoubleAdder();
    private final LongAdder bookings = new LongAdder();
    @Override public void update(Event e) {
        total.add(e.amount());
        bookings.increment();
        publish(new Event(Event.Type.REVENUE_SUMMARY, "", 0, total.sum(), bookings.sum() + " bookings"));
    }
    public double total() { return total.sum(); }
}
