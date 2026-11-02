package hotel;
import java.util.List;
import java.util.Set;
public class CleaningPlanVisitor implements Visitor<List<String>> {
    private final Set<String> dirtyIds;
    public CleaningPlanVisitor(Set<String> dirtyIds) { this.dirtyIds = dirtyIds; }
    @Override public List<String> visit(Hotel h) { return h.floors().stream().flatMap(f -> f.accept(this).stream()).toList(); }
    @Override public List<String> visit(Floor f) { return f.units().stream().flatMap(u -> u.accept(this).stream()).toList(); }
    @Override public List<String> visit(Room r) { return pick(r.id()); }
    @Override public List<String> visit(Suite s) { return pick(s.id()); }
    @Override public List<String> visit(Hall h) { return List.of(); }
    @Override public List<String> visit(Spa s) { return List.of(); }
    private List<String> pick(String id) { return dirtyIds.contains(id) ? List.of(id) : List.of(); }
}
