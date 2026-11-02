package hotel;
public record Room(String id, double nightlyRate, boolean occupied) implements Element {
    @Override public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
