package com.gluonhq.netbeans.nbfx.project.ui.maven;

import com.gluonhq.netbeans.nbfx.project.ui.api.BuildActionProvider;
import com.gluonhq.netbeans.nbfx.project.ui.api.BuildCommands;
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
    public String[] getSupportedActions() {
        return new String[] {
            BuildCommands.BUILD, BuildCommands.CLEAN, BuildCommands.REBUILD,
            BuildCommands.RUN, BuildCommands.TEST, BuildCommands.JAVADOC
        };
    }

    @Override
    public List<String> commandLine(Path dir, String command) {
        List<String> goals = switch (command) {
            case BuildCommands.BUILD -> List.of("package");
            case BuildCommands.CLEAN -> List.of("clean");
            case BuildCommands.REBUILD -> List.of("clean", "package");
            case BuildCommands.TEST -> List.of("test");
            case BuildCommands.RUN -> List.of("compile", "exec:java");
            case BuildCommands.JAVADOC -> List.of("javadoc:javadoc");
            default -> null;
        };
        if (goals == null) {
            return null;
        }
        List<String> line = new ArrayList<>(goals.size() + 1);
        line.add(executable(dir, "mvnw", "mvn"));
        line.addAll(goals);
        return List.copyOf(line);
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
