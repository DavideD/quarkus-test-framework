package io.quarkus.qe;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingContainerRegistry)
public class OpenShiftUsingContainerRegistryPingPongResourceIT {

    @QuarkusApplication
    static final Service pingPongApp = new Service("pingpong");

    @Test
    public void shouldPingPongWorks() {
        pingPongApp.restAssured().get("/ping").then().statusCode(HttpStatus.SC_OK).body(is("ping"));
        pingPongApp.restAssured().get("/pong").then().statusCode(HttpStatus.SC_OK).body(is("pong"));
    }
}
