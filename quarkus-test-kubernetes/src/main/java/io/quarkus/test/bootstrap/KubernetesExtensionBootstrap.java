package io.quarkus.test.bootstrap;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

import io.quarkus.test.bootstrap.inject.KubectlClient;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.KubernetesScenario;
import io.quarkus.test.utils.FileUtils;

public class KubernetesExtensionBootstrap implements ExtensionBootstrap {

    public static final String CLIENT = "kubectl-client";

    private KubectlClient client;

    @Override
    public boolean appliesFor(ExtensionContext context) {
        return context.getRequiredTestClass().isAnnotationPresent(KubernetesScenario.class);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        client = KubectlClient.create();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        client.deleteNamespace();
    }

    @Override
    public void updateServiceContext(ServiceContext context) {
        context.put(CLIENT, client);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == KubectlClient.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return client;
    }

    @Override
    public void onError(ExtensionContext context, Throwable throwable) {
        Map<String, String> logs = client.logs();
        for (Entry<String, String> podLog : logs.entrySet()) {
            FileUtils.copyContentTo(podLog.getValue(), Paths.get(Log.LOG_OUTPUT_DIRECTORY, podLog.getKey() + Log.LOG_SUFFIX));
        }
    }
}
