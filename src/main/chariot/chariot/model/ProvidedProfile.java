package chariot.model;

public interface ProvidedProfile {
    Opt<String> flag();
    Opt<String> location();
    Opt<String> bio();
    Opt<String> realName();
    @Deprecated default Opt<String> firstName() { return realName(); }
    @Deprecated default Opt<String> lastName()  { return realName(); }
    Opt<String> links();
    Opt<Integer> ratingFide();
    Opt<Integer> ratingUscf();
    Opt<Integer> ratingEcf();
    Opt<Integer> ratingRcf();
    Opt<Integer> ratingCfc();
    Opt<Integer> ratingDsb();

    /**
     * @deprecated
     * Field contains a "flag" and not a "country"<br>
     * This method simply returns the value of {@link #flag()}.<br>
     * This method will be removed in future.<br>
     */
    @Deprecated
    default Opt<String> country() { return flag(); }
}
