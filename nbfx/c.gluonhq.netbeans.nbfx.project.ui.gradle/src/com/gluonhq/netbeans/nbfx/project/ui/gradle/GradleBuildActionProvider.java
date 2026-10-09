package com.gluonhq.netbeans.nbfx.project.ui.gradle;

import com.gluonhq.netbeans.nbfx.project.ui.api.BuildActionProvider;
import com.gluonhq.netbeans.nbfx.project.ui.api.BuildCommands;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/** Gradle build commands: tasks run with a project-local {@code gradlew} when present, else {@code gradle}. */
@ServiceProvider(service = BuildActionProvider.class)
public final class GradleBuildActionProvider implements BuildActionProvider {

    @Override
    public String projectTypeId() {
        return "gradle";
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
        String executable = executable(dir, "gradlew", "gradle");
        return switch (command) {
            case BuildCommands.BUILD -> List.of(executable, "build");
            case BuildCommands.CLEAN -> List.of(executable, "clean");
            case BuildCommands.REBUILD -> List.of(executable, "clean", "build");
            case BuildCommands.TEST -> List.of(executable, "test");
            case BuildCommands.RUN -> List.of(executable, "run");
            case BuildCommands.JAVADOC -> List.of(executable, "javadoc");
            default -> null;
        };
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
