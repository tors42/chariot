package chariot.model;

public enum FideTC { standard, rapid, blitz;

    public interface Provider {
        default FideTC standard() { return standard; }
        default FideTC rapid()    { return rapid; }
        default FideTC bliz()     { return blitz; }
    }
    public static Provider provider() {return new Provider(){};}
}
