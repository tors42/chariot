module testchariot {
    requires chariot;
    requires jdk.httpserver;
    requires java.net.http;

    // Allow chariot module to read our test model classes
    exports tests.internal.yayson to chariot;
}
