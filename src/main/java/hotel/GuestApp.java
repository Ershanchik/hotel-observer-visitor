package hotel;
import java.util.concurrent.atomic.LongAdder;
public class GuestApp implements Observer<Event> {
    private final LongAdder confirmations = new LongAdder();
    @Override public void update(Event e) { confirmations.increment(); }
    public long confirmations() { return confirmations.sum(); }
}
