package com.gluonhq.netbeans.nbfx.project.ui.api;

/**
 * A build/run action a project can perform, independent of the build system.
 *
 * @since 1.0
 */
public enum BuildAction {
    /** Compiles and packages the project. */
    BUILD,
    /** Cleans the build output and then builds the project. */
    CLEAN_BUILD,
    /** Removes the build output. */
    CLEAN,
    /** Runs the project's tests. */
    TEST,
    /** Runs the project. */
    RUN
}
