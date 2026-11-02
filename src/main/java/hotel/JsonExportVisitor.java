package hotel;
import java.util.List;
import java.util.stream.Collectors;
public class JsonExportVisitor implements Visitor<String> {
    @Override public String visit(Hotel h) { return "{\"hotel\":" + quote(h.name()) + ",\"floors\":[" + join(h.floors()) + "]}"; }
    @Override public String visit(Floor f) { return "{\"floor\":" + f.number() + ",\"units\":[" + join(f.units()) + "]}"; }
    @Override public String visit(Room r) { return unit("room", r.id(), "\"nightlyRate\":" + r.nightlyRate() + ",\"occupied\":" + r.occupied()); }
    @Override public String visit(Suite s) { return unit("suite", s.id(), "\"nightlyRate\":" + s.nightlyRate() + ",\"bedrooms\":" + s.bedrooms() + ",\"occupied\":" + s.occupied()); }
    @Override public String visit(Hall h) { return unit("hall", h.id(), "\"hourlyRate\":" + h.hourlyRate() + ",\"bookedHours\":" + h.bookedHours()); }
    @Override public String visit(Spa s) { return unit("spa", s.id(), "\"sessionPrice\":" + s.sessionPrice() + ",\"sessionsBooked\":" + s.sessionsBooked()); }
    private String unit(String type, String id, String fields) { return "{\"type\":\"" + type + "\",\"id\":" + quote(id) + "," + fields + "}"; }
    private String join(List<? extends Element> elements) { return elements.stream().map(e -> e.accept(this)).collect(Collectors.joining(",")); }
    private static String quote(String s) { return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
}
