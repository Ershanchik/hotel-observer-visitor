package hotel;
import java.util.List;
public record Floor(int number, List<Element> units) implements Element {
    public Floor { units = List.copyOf(units); }
    @Override public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
