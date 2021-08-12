package chariot.model;

import java.util.List;

import static chariot.internal.Util.orEmpty;

public record Simuls(
        List<Simul> pending,
        List<Simul> created,
        List<Simul> started) implements Model {

    public record Simul (
            String id,
            String text,
            String name,
            String fullname,
            boolean isCreated,
            boolean isFinished,
            boolean isRunning,
            int nbApplicants,
            int nbPairings,
            Host host,
            List<Variant> variants) {
            public Simul {
                name = orEmpty(name);
                fullname = orEmpty(fullname);
                text = orEmpty(text);
            }

        public record Host(String id, String name, int rating, String title) {
            public Host {
                title = orEmpty(title);
            }
        }
        public record Variant(String key, String name, String icon) {}
    }
}
