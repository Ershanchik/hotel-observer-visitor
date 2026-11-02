package hotel;
import static hotel.Event.Type.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
public class Main {
    private static final int FLOORS = 3;
    private static final double RATE = 100.0;
    public static void main(String[] args) throws Exception {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        int eventsPerThread = args.length > 1 ? Integer.parseInt(args[1]) : 1000;
        AuditLog audit = run(threads, eventsPerThread);
        for (Event.Type type : Event.Type.values()) System.out.println(type + ": " + audit.count(type));
    }
    public static AuditLog run(int threads, int eventsPerThread) throws Exception {
        BookingSource bookings = new BookingSource();
        RoomStatusSource statuses = new RoomStatusSource();
        FrontDesk frontDesk = new FrontDesk();
        Housekeeping housekeeping = new Housekeeping(buildHotel());
        RevenueManager revenue = new RevenueManager();
        GuestApp guestApp = new GuestApp();
        AuditLog audit = new AuditLog();
        wire(bookings, frontDesk, BOOKING);
        wire(bookings, revenue, BOOKING);
        wire(bookings, guestApp, BOOKING);
        wire(bookings, audit, BOOKING);
        wire(statuses, frontDesk, ROOM_DIRTY, ROOM_CLEANED);
        wire(statuses, housekeeping, ROOM_DIRTY, ROOM_CLEANED);
        wire(statuses, audit, ROOM_DIRTY, ROOM_CLEANED);
        wire(housekeeping, audit, CLEANING_PLAN);
        wire(revenue, audit, REVENUE_SUMMARY);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<?>> producers = new ArrayList<>();
        for (int id = 0; id < threads; id++) producers.add(pool.submit(producer(id, eventsPerThread, bookings, statuses)));
        pool.shutdown();
        for (Future<?> producer : producers) producer.get();
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) pool.shutdownNow();
        return audit;
    }
    private static void wire(EventSubject source, Observer<Event> listener, Event.Type... types) {
        for (Event.Type type : types) source.subscribe(type, listener);
    }
    private static Runnable producer(int id, int events, BookingSource bookings, RoomStatusSource statuses) {
        return () -> {
            for (int i = 0; i < events; i++) {
                int floor = i % FLOORS + 1;
                String room = String.valueOf(floor * 100 + (i / FLOORS) % 3 + 1);
                if (id % 2 == 0) bookings.book(room, floor, RATE);
                else if (i % 2 == 0) statuses.markDirty(room, floor);
                else statuses.markCleaned(room, floor);
            }
        };
    }
    static Hotel buildHotel() {
        return new Hotel("Grand Astana", List.of(
            new Floor(1, List.of(new Room("101", 90, false), new Room("102", 90, true), new Room("103", 90, false), new Suite("104", 200, 2, true), new Hall("H1", 150, 6))),
            new Floor(2, List.of(new Room("201", 100, true), new Room("202", 100, false), new Room("203", 100, true), new Suite("204", 220, 2, false))),
            new Floor(3, List.of(new Room("301", 110, true), new Room("302", 110, true), new Room("303", 110, false), new Suite("304", 300, 3, true), new Spa("S1", 60, 8)))));
    }
}
