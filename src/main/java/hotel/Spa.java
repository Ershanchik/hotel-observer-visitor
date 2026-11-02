package hotel;
public record Spa(String id, double sessionPrice, int sessionsBooked) implements Element {
    @Override public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
