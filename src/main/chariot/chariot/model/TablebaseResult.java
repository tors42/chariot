package chariot.model;

import java.util.List;

public record TablebaseResult(
        Integer dtz,
        Integer precise_dtz,
        Integer dtm,
        boolean checkmate,
        boolean stalemate,
        boolean insufficient_material,
        boolean variant_win,
        boolean variant_loss,
        String category,
        List<Move> moves) implements Model {

    public record Move(
            String uci,
            String san,
            Integer dtz,
            Integer precise_dtz,
            Integer dtm,
            boolean zeroing,
            boolean checkmate,
            boolean stalemate,
            boolean variant_win,
            boolean variant_loss,
            boolean insufficient_material,
            String category) {}
}
