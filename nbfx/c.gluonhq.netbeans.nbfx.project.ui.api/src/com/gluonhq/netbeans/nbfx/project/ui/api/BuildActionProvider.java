package com.gluonhq.netbeans.nbfx.project.ui.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Service provider interface for the build/run commands of a project type, mirroring the original
 * {@code org.netbeans.spi.project.ActionProvider}: a project type declares the commands it supports
 * and how to run them, instead of the build runner hard-coding the tool.
 *
 * <p>Register implementations with {@code @ServiceProvider(service = BuildActionProvider.class)}.
 * The {@code projectTypeId} matches the id of the project's {@link ProjectKindProvider}.</p>
 *
 * @since 1.0
 */
public interface BuildActionProvider {

    /** The project-kind id this provider serves (see {@link ProjectKindProvider#id()}). */
    String projectTypeId();

    /**
     * The commands this provider supports, from the constants in {@link BuildCommands}. Mirrors
     * {@code ActionProvider.getSupportedActions()}.
     */
    String[] getSupportedActions();

    /**
     * Whether {@code command} can be invoked. Mirrors {@code ActionProvider.isActionEnabled()};
     * defaults to {@code true} for any supported command.
     */
    default boolean isActionEnabled(String command) {
        return true;
    }

    /**
     * The command line to run {@code command} in {@code dir}, or {@code null} when this provider does
     * not support it.
     */
    List<String> commandLine(Path dir, String command);
}
