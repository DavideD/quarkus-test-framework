package io.quarkus.qe;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftUsingExtensionPingPongResourceIT {

    @QuarkusApplication
    static final RestService pingPongApp = new RestService();

    @Test
    public void shouldPingPongWorks() {
        pingPongApp.given().get("/ping").then().statusCode(HttpStatus.SC_OK).body(is("ping"));
        pingPongApp.given().get("/pong").then().statusCode(HttpStatus.SC_OK).body(is("pong"));
    }
}
