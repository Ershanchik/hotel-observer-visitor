package hotel;
public record Hall(String id, double hourlyRate, int bookedHours) implements Element {
    @Override public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
