package hotel;
import java.util.List;
public record Hotel(String name, List<Floor> floors) implements Element {
    public Hotel { floors = List.copyOf(floors); }
    @Override public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
