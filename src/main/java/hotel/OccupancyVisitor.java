package hotel;
public class OccupancyVisitor implements Visitor<Void> {
    private int total;
    private int occupied;
    @Override public Void visit(Hotel h) { h.floors().forEach(f -> f.accept(this)); return null; }
    @Override public Void visit(Floor f) { f.units().forEach(u -> u.accept(this)); return null; }
    @Override public Void visit(Room r) { count(r.occupied()); return null; }
    @Override public Void visit(Suite s) { count(s.occupied()); return null; }
    @Override public Void visit(Hall h) { return null; }
    @Override public Void visit(Spa s) { return null; }
    private void count(boolean isOccupied) { total++; if (isOccupied) occupied++; }
    public int total() { return total; }
    public int occupied() { return occupied; }
    public double rate() { return total == 0 ? 0 : (double) occupied / total; }
}
