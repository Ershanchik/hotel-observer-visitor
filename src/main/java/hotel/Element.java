package hotel;
public interface Element {
    <R> R accept(Visitor<R> visitor);
}
