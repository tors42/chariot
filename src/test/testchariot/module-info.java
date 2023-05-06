module testchariot {
    requires chariot;
    requires jdk.httpserver;

    // Allow chariot module to read our test model classes
    exports tests.internal.yayson to chariot;
}
