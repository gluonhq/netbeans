package com.gluonhq.netbeans.nbfx.project.ui.maven;

import com.gluonhq.netbeans.nbfx.project.ui.api.BuildAction;
import com.gluonhq.netbeans.nbfx.project.ui.api.BuildActionProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/** Maven build commands: goals run with a project-local {@code mvnw} when present, else {@code mvn}. */
@ServiceProvider(service = BuildActionProvider.class)
public final class MavenBuildActionProvider implements BuildActionProvider {

    @Override
    public String projectTypeId() {
        return "maven";
    }

    @Override
    public List<String> commandLine(Path dir, BuildAction action) {
        List<String> goals = switch (action) {
            case BUILD -> List.of("package");
            case CLEAN_BUILD -> List.of("clean", "package");
            case CLEAN -> List.of("clean");
            case TEST -> List.of("test");
            case RUN -> List.of("compile", "exec:java");
        };
        List<String> command = new ArrayList<>(goals.size() + 1);
        command.add(executable(dir, "mvnw", "mvn"));
        command.addAll(goals);
        return List.copyOf(command);
    }

    private static String executable(Path dir, String wrapper, String fallback) {
        Path unix = dir.resolve(wrapper);
        if (Files.isRegularFile(unix)) {
            return unix.toString();
        }
        Path windows = dir.resolve(wrapper + ".cmd");
        return Files.isRegularFile(windows) ? windows.toString() : fallback;
    }
}
