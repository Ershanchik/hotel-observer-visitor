package hotel;
public interface Visitor<R> {
    R visit(Hotel hotel);
    R visit(Floor floor);
    R visit(Room room);
    R visit(Suite suite);
    R visit(Hall hall);
    R visit(Spa spa);
}
