package chariot.internal.modeladapter;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.List;

import java.util.function.Function;

import chariot.internal.yayson.Parser.*;
import chariot.model.*;

public interface RatingHistoryAdapter {
    static Object nodeToArray(YayNode yayNode) {
        Function<YayNode, List<RatingHistory.DateResult>> helper = node -> {
            if (node instanceof YayArray yarr) {
                return yarr.value().stream().map( e -> {
                    if (e instanceof YayArray ya) {
                        var list = ya.value().stream().map(yn -> (YayNumber)yn).toList();
                        var y = list.get(0);
                        var m = list.get(1);
                        var d = list.get(2);
                        var p = list.get(3);

                        var date = LocalDate.of(y.value().intValue(), m.value().intValue()+1, d.value().intValue());
                        var points = p.value().intValue();

                        // DateResult model
                        return new RatingHistory.DateResult(date, points);
                    }
                    return null;
                }).toList();
            }
            return List.of();
        };
        if (yayNode instanceof YayArray ya) {
            var list = ya.value().stream()
                .map(e -> {
                    if (e instanceof YayObject yo) {

                        // RatingHistory model
                        return new RatingHistory(yo.getString("name"), helper.apply(yo.value().get("points")));
                    }
                    return null;
                })
            .toList();

            var array = Array.newInstance(RatingHistory.class, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        }
        return null;
    }
}

