package io.quarkus.test.quarkus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.quarkus.test.ManagedResource;
import io.quarkus.test.logging.FileLoggingHandler;
import io.quarkus.test.logging.LoggingHandler;
import io.quarkus.test.utils.SocketUtils;
import io.quarkus.utilities.JavaBinFinder;

public class LocalhostQuarkusApplicationManagedResource implements ManagedResource {

	private static final String EXPECTED_OUTPUT_FROM_SUCCESSFULLY_STARTED = "features";
	private static final String QUARKUS_HTTP_PORT_PROPERTY = "quarkus.http.port";

	private final QuarkusApplicationManagedResourceBuilder model;

	private int runningPort;
	private Process process;
	private LoggingHandler loggingHandler;

	public LocalhostQuarkusApplicationManagedResource(QuarkusApplicationManagedResourceBuilder model) {
		this.model = model;
	}

	@Override
	public void start() {
		if (process != null && process.isAlive()) {
			// do nothing
			return;
		}

		try {
			runningPort = preparePort();
			loggingHandler = new FileLoggingHandler(model.getContext().getServiceFolder().resolve("out.log"));

			process = new ProcessBuilder(prepareCommand()).redirectErrorStream(true)
					.directory(model.getBuiltResultArtifact().getParent().toFile()).start();

			loggingHandler.startWatching(process.getInputStream());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		loggingHandler.stopWatching();

		try {
			if (process != null) {
				process.destroy();
				process.waitFor();
			}
		} catch (InterruptedException ignored) {

		}
	}

	@Override
	public String getHost() {
		return "http://localhost";
	}

	@Override
	public int getPort() {
		return runningPort;
	}

	@Override
	public boolean isRunning() {
		return loggingHandler.logsContains(EXPECTED_OUTPUT_FROM_SUCCESSFULLY_STARTED);
	}

	private int preparePort() {
		String port = model.getContext().getOwner().getRuntimeProperties().get(QUARKUS_HTTP_PORT_PROPERTY);
		if (StringUtils.isEmpty(port)) {
			return SocketUtils.findAvailablePort();
		}

		return Integer.parseInt(port);
	}

	private List<String> prepareCommand() {
		Map<String, String> runtimeProperties = new HashMap<>(model.getContext().getOwner().getRuntimeProperties());
		runtimeProperties.putIfAbsent(QUARKUS_HTTP_PORT_PROPERTY, "" + runningPort);

		List<String> systemProperties = runtimeProperties.entrySet().stream()
				.map(e -> "-D" + e.getKey() + "=" + e.getValue()).collect(Collectors.toList());
		List<String> command = new ArrayList<>(systemProperties.size() + 3);
		if (model.getBuiltResultArtifact().getFileName().toString().endsWith(".jar")) {
			command.add(JavaBinFinder.findBin());
			command.addAll(systemProperties);
			command.add("-jar");
			command.add(model.getBuiltResultArtifact().toAbsolutePath().toString());
		} else {
			command.add(model.getBuiltResultArtifact().toAbsolutePath().toString());
			command.addAll(systemProperties);
		}

		return command;
	}

}
