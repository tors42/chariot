package util;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public sealed interface Scenario {

    List<Script> scripts();
    List<ScenarioProperty> properties();

    default Scenario withAdditionalProperty(ScenarioProperty property) {
        return new SScenario(scripts(),
                Stream.concat(properties().stream(), Stream.of(property)).toList());
    }

    static Scenario single(List<RequestProperty> req, List<ResponseProperty> res) {
        return new SScenario(
                List.of(new Script(List.copyOf(req), List.copyOf(res))),
                List.of(timeout(Duration.ofSeconds(2))));
    }

    static SLogging logging(Consumer<String> log) { return new SLogging(log); }
    static SPath path(String path) { return new SPath(path); }
    static SBody body(String body) { return body(body.getBytes()); }
    static SBody body(byte[] body) { return new SBody(body); }
    static SStreamBody streamBodies(List<String> bodies, String... erasureTrickHmm) {
        return streamBodies(bodies.stream().map(String::getBytes).toList());
    }
    static SStreamBody streamBodies(List<byte[]> bodies) { return new SStreamBody(bodies); }
    static SStatus status(int status) { return new SStatus(status); }
    static SHeader header(String key, String value) { return header(key, List.of(value)); }
    static SHeader header(String key, List<String> value) { return new SHeader(key, value); }
    static SQueryParam queryParam(String key, String value) { return new SQueryParam(key, value); }

    static STimeout timeout(Duration duration) { return new STimeout(duration); }

    sealed interface ScenarioProperty {}
    sealed interface RequestProperty {}
    sealed interface ResponseProperty {}

    record SScenario(List<Script> scripts, List<ScenarioProperty> properties) implements Scenario {}

    record STimeout(Duration value) implements ScenarioProperty {}
    record SLogging(Consumer<String> log) implements ScenarioProperty {}

    record Script(List<RequestProperty> req, List<ResponseProperty> res) {}
    record SPath(String value) implements RequestProperty {}
    record SBody(byte[] value) implements RequestProperty, ResponseProperty {}
    record SStreamBody(List<byte[]> value) implements RequestProperty, ResponseProperty {}
    record SMethod(String value) implements RequestProperty {}
    record SHeader(String key, List<String> value) implements RequestProperty, ResponseProperty {}
    record SQueryParam(String key, String value) implements RequestProperty {}
    record SStatus(int value) implements ResponseProperty {}
}
