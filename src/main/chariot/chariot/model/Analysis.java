package chariot.model;

import java.util.List;

public record Analysis(String fen, int knodes, int depth, List<PV> pvs)  {
    public record PV(String moves, int cp) {}
}
