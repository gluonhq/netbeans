package com.gluonhq.netbeans.nbfx.project.ui.gradle;

import com.gluonhq.netbeans.nbfx.project.ui.api.BuildAction;
import com.gluonhq.netbeans.nbfx.project.ui.api.BuildActionProvider;
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
    public List<String> commandLine(Path dir, BuildAction action) {
        String executable = executable(dir, "gradlew", "gradle");
        return switch (action) {
            case BUILD -> List.of(executable, "build");
            case CLEAN_BUILD -> List.of(executable, "clean", "build");
            case CLEAN -> List.of(executable, "clean");
            case TEST -> List.of(executable, "test");
            case RUN -> List.of(executable, "run");
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
