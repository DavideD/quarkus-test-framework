package io.quarkus.test.utils;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import io.quarkus.test.bootstrap.ServiceContext;

public final class MavenUtils {

    public static final String MVN_COMMAND = "mvn";
    public static final String PACKAGE_GOAL = "package";
    public static final String INSTALL_GOAL = "install";
    public static final String MVN_REPOSITORY_LOCAL = "maven.repo.local";
    public static final String SKIP_TESTS = "-DskipTests=true";
    public static final String SKIP_ITS = "-DskipITs=true";
    public static final String BATCH_MODE = "-B";
    public static final String DISPLAY_VERSION = "-V";
    public static final String SKIP_CHECKSTYLE = "-Dcheckstyle.skip";
    public static final String QUARKUS_PROFILE = "quarkus.profile";
    public static final String POM_XML = "pom.xml";

    private MavenUtils() {

    }

    public static List<String> mvnCommand(ServiceContext serviceContext) {
        List<String> args = new ArrayList<>();
        args.add(MVN_COMMAND);
        args.add(withQuarkusProfile(serviceContext));
        withMavenRepositoryLocalIfSet(args);
        return args;
    }

    public static String withProperty(String property, String value) {
        return String.format("-D%s=%s", property, value);
    }

    public static void installParentPomsIfNeeded() {
        installParentPomsIfNeeded(Paths.get("."));
    }

    public static void installParentPomsIfNeeded(Path basePath) {
        if (Files.exists(basePath.resolve(POM_XML))) {
            Model mavenModel = getMavenModel(basePath);
            if (mavenModel != null && mavenModel.getParent() != null) {
                Path relativePath = basePath
                        .resolve(StringUtils.defaultIfBlank(mavenModel.getParent().getRelativePath(), ".."));
                installParentPom(relativePath);
                installParentPomsIfNeeded(relativePath);
            }
        }
    }

    private static void installParentPom(Path relativePath) {
        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList(MVN_COMMAND, INSTALL_GOAL, SKIP_CHECKSTYLE, SKIP_TESTS, SKIP_ITS, "-pl", "."));
        withMavenRepositoryLocalIfSet(args);

        Command cmd = new Command(args);
        cmd.onDirectory(relativePath);
        try {
            cmd.runAndWait();
        } catch (Exception ignored) {
            // Could not find POM.xml, ignoring
        }
    }

    private static String withQuarkusProfile(ServiceContext serviceContext) {
        return withProperty(QUARKUS_PROFILE, serviceContext.getTestContext().getRequiredTestClass().getSimpleName());
    }

    private static void withMavenRepositoryLocalIfSet(List<String> args) {
        String mvnRepositoryPath = System.getProperty(MVN_REPOSITORY_LOCAL);
        if (mvnRepositoryPath != null) {
            args.add(withProperty(MVN_REPOSITORY_LOCAL, mvnRepositoryPath));
        }
    }

    private static Model getMavenModel(Path basePath) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (FileReader is = new FileReader(basePath.resolve(POM_XML).toFile())) {
            return reader.read(is);
        } catch (Exception ignored) {
            // Could not find POM.xml, ignoring
        }

        return null;
    }
}
