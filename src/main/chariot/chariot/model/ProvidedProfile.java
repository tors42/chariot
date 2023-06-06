package chariot.model;

public interface ProvidedProfile {
    Opt<String> country();
    Opt<String> location();
    Opt<String> bio();
    Opt<String> firstName();
    Opt<String> lastName();
    Opt<String> links();
    Opt<Integer> ratingFide();
    Opt<Integer> ratingUscf();
    Opt<Integer> ratingEcf();
    Opt<Integer> ratingRcf();
    Opt<Integer> ratingCfc();
    Opt<Integer> ratingDsb();
}
