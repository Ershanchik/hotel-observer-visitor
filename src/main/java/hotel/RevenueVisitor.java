package hotel;
public class RevenueVisitor implements Visitor<Double> {
    @Override public Double visit(Hotel h) { return h.floors().stream().mapToDouble(f -> f.accept(this)).sum(); }
    @Override public Double visit(Floor f) { return f.units().stream().mapToDouble(u -> u.accept(this)).sum(); }
    @Override public Double visit(Room r) { return r.occupied() ? r.nightlyRate() : 0.0; }
    @Override public Double visit(Suite s) { return s.occupied() ? s.nightlyRate() : 0.0; }
    @Override public Double visit(Hall h) { return h.hourlyRate() * h.bookedHours(); }
    @Override public Double visit(Spa s) { return s.sessionPrice() * s.sessionsBooked(); }
}
