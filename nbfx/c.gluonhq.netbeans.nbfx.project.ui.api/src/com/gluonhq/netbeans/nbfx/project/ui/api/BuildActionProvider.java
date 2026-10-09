package com.gluonhq.netbeans.nbfx.project.ui.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Service provider interface for the build/run commands of a project type. Mirrors the original
 * {@code org.netbeans.spi.project.ActionProvider}: the build actions are contributed by the
 * project-type module, not hard-coded in the build runner.
 *
 * <p>Register implementations with {@code @ServiceProvider(service = BuildActionProvider.class)}.
 * The {@code projectTypeId} matches the id of the project's
 * {@link ProjectKindProvider}.</p>
 *
 * @since 1.0
 */
public interface BuildActionProvider {

    /** The project-kind id this provider serves (see {@link ProjectKindProvider#id()}). */
    String projectTypeId();

    /**
     * The command line to run {@code action} in {@code dir}, or {@code null} when this provider does
     * not support the action.
     */
    List<String> commandLine(Path dir, BuildAction action);
}
