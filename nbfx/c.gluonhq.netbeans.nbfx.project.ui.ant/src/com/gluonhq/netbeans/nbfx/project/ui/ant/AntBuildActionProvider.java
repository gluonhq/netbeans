package com.gluonhq.netbeans.nbfx.project.ui.ant;

import com.gluonhq.netbeans.nbfx.project.ui.api.BuildAction;
import com.gluonhq.netbeans.nbfx.project.ui.api.BuildActionProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * Ant build commands. A NetBeans module (apisupport) project - recognised by its
 * {@code nbproject/project.xml} - uses the harness targets ({@code build}, {@code clean},
 * {@code test-unit}, {@code run}); a plain Ant project uses {@code jar}/{@code test}/{@code run}.
 */
@ServiceProvider(service = BuildActionProvider.class)
public final class AntBuildActionProvider implements BuildActionProvider {

    @Override
    public String projectTypeId() {
        return "ant";
    }

    @Override
    public List<String> commandLine(Path dir, BuildAction action) {
        boolean netbeansModule = Files.isRegularFile(dir.resolve("nbproject").resolve("project.xml"));
        if (netbeansModule) {
            return switch (action) {
                case BUILD -> List.of("ant", "build");
                case CLEAN_BUILD -> List.of("ant", "clean", "build");
                case CLEAN -> List.of("ant", "clean");
                case TEST -> List.of("ant", "test-unit");
                case RUN -> List.of("ant", "run");
            };
        }
        return switch (action) {
            case BUILD -> List.of("ant", "jar");
            case CLEAN_BUILD -> List.of("ant", "clean", "jar");
            case CLEAN -> List.of("ant", "clean");
            case TEST -> List.of("ant", "test");
            case RUN -> List.of("ant", "run");
        };
    }
}
