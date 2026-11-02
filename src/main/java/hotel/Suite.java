package hotel;
public record Suite(String id, double nightlyRate, int bedrooms, boolean occupied) implements Element {
    @Override public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
