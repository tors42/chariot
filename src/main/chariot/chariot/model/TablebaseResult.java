package chariot.model;

import java.util.List;

public record TablebaseResult(
        Integer wdl,
        Integer dtz,
        Integer dtm,
        boolean checkmate,
        boolean stalemate,
        boolean insufficient_material,
        boolean variant_win,
        boolean variant_loss,
        List<Move> moves) implements Model {

    public record Move(
            String uci,
            String san,
            Integer wdl,
            Integer dtz,
            Integer dtm,
            boolean zeroing,
            boolean checkmate,
            boolean stalemate,
            boolean variant_win,
            boolean variant_loss,
            boolean insufficient_material) {}
}
