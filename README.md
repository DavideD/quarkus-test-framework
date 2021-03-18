# Quarkus QE Test Framework

This framework leverages the bootstrap of Quarkus application and other resources like containers.

In order to write Quarkus application in your tests, you first need to add the core dependency in your `pom.xml` file;

```xml
<dependency>
	<groupId>io.quarkus.qe</groupId>
	<artifactId>quarkus-test-core</artifactId>
</dependency>
```

## Architecture

TBD

This framework is designed to follow extension model patterns. Therefore, we can extend any functionality just by adding other dependencies that extend the current functionality. As an example, Quarkus applications will be deployed locally, but in the future, we can deploy it in OpenShift/K8s just by adding an integration test annotation with a new annotation `OpenShiftTest` and adding the new dependency.

## Quarkus Scenarios

The framework aims to think on scenarios to verify. One scenario could include a few Quarkus instances and other container resources:

```java
@QuarkusScenario
public class PingPongResourceTest {

	@QuarkusApplication(classes = PingResource.class)
	static final Service pingApp = new Service("ping");

	@QuarkusApplication(classes = PongResource.class)
	static final Service pongApp = new Service("pong");

    // will include ping and pong resources
	@QuarkusApplication
	static final Service pingPongApp = new Service("pingpong");

	// ...

}
```

As seen in the above example, everything is bounded to a Service object that will contain everything needed to interact with our resources.

Following the extension model architecture, the framework also supports running integration tests on Native:


```java
@NativeTest
public class NativePingPongResourceIT extends PingPongResourceTest {
}
```

### Containers

Also, we can deploy services via docker:

```java
@QuarkusScenario
public class GreetingResourceTest {

	private static final String CUSTOM_PROPERTY = "my.property";

	@Container(image = "quay.io/bitnami/consul:1.9.3", expectedLog = "Synced node info", port = 8500)
	static final Service consul = new Service("consul");

	@QuarkusApplication
	static final Service app = new Service("app");
	
	// ...
}
```

## Features

- `restAssured`: Rest Assured integration.

Rest Assured is already integrated in our services:

```java
@QuarkusApplication
static final Service app = new Service("app");


@Test
public void shouldPing() {
    app.restAssured().get("/ping").then().statusCode(HttpStatus.SC_OK).body(is("ping pong"));
}
```

- `withProperty`: Intuitive usage of properties.

We can configure our resources using configuration of other resources:

```java
@Container(image = "quay.io/bitnami/consul:1.9.3", expectedLog = "Synced node info", port = 8500)
static final Service consul = new Service("consul");

@QuarkusApplication
static final Service app = new Service("app").withProperty("quarkus.consul-config.agent.host-port",
		() -> consul.getHost() + ":" + consul.getPort());
```

The resources will be initiated in order of presence of the test class. 

- Service Lifecycle

The framework allows to add actions via hooks in every stage of the service lifecycle:

```java
@Container(image = "quay.io/bitnami/consul:1.9.3", expectedLog = "Synced node info", port = 8500)
static final Service consul = new Service("consul").onPostStart(GreetingResourceTest::onLoadConfigureConsul);

private static final void onLoadConfigureConsul(Service service) {
	// ...
}
```

- Services are startable and stoppable by default

Any Quarkus application and containers can be stopped to cover more complex scenarios. The test framework will restart the services by you before starting a new test case.

- Discovery of build time properties to build Quarkus applications

The test framework will leverage whether an application runner can be reused or it needs to trigger a new build. Note that for this feature, tests should be written as Integration Tests. 

- Colourify logging

The test framework will output a different colour by service. This will extremely ease the troubleshooting of the logic.

You can enable the logging by adding a `test.properties` in your module with the next property:

```
ts.<YOUR SERVICE NAME>.log.enable=true
```

## TODO
- Improve documentation and architecture diagrams
- Integration with OpenShift using a new annotation
- Integration with Awailability
- Ease to colourify a set of a log only (more about colors here: https://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html)
- Support of Quarkus Applications from Docker images
- Support of Quarkus Applications of Maven modules (select a Maven module instead of using a set of classes)
- Deploy to Maven central
