/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.gluonhq.netbeans.nbfx.run;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A build tool, detected from a project's directory, and the command line to run a build action
 * with it.
 *
 * @since 1.0
 */
public enum BuildTool {

    /** Apache Maven, detected by a {@code pom.xml}. */
    MAVEN,

    /** Gradle, detected by a {@code build.gradle[.kts]} or {@code settings.gradle[.kts]}. */
    GRADLE,

    /** Apache Ant, detected by a {@code build.xml}. */
    ANT,

    /** No supported build tool. */
    UNKNOWN;

    /** The build action to run. */
    public enum Action {
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

    /**
     * Detects the build tool of {@code dir} from its marker files, preferring Maven, then Gradle,
     * then Ant.
     *
     * @param dir the project directory; may be {@code null}
     * @return the detected tool, or {@link #UNKNOWN}
     */
    public static BuildTool detect(Path dir) {
        if (dir == null) {
            return UNKNOWN;
        }
        if (Files.isRegularFile(dir.resolve("pom.xml"))) {
            return MAVEN;
        }
        if (anyFile(dir, "build.gradle", "build.gradle.kts", "settings.gradle", "settings.gradle.kts")) {
            return GRADLE;
        }
        if (Files.isRegularFile(dir.resolve("build.xml"))) {
            return ANT;
        }
        return UNKNOWN;
    }

    /**
     * The command line to run {@code action} in {@code dir}, or {@code null} when this tool is
     * {@link #UNKNOWN}. A project-local wrapper ({@code mvnw}/{@code gradlew}) is preferred over a
     * tool on the {@code PATH}.
     *
     * @param dir    the project directory
     * @param action the action to run
     * @return the command line, or {@code null}
     */
    public List<String> commandLine(Path dir, Action action) {
        return switch (this) {
            case MAVEN -> maven(dir, action);
            case GRADLE -> gradle(dir, action);
            case ANT -> ant(dir, action);
            case UNKNOWN -> null;
        };
    }

    private static List<String> maven(Path dir, Action action) {
        List<String> goals = switch (action) {
            case BUILD -> List.of("package");
            case CLEAN_BUILD -> List.of("clean", "package");
            case CLEAN -> List.of("clean");
            case TEST -> List.of("test");
            case RUN -> List.of("compile", "exec:java");
        };
        return prepend(executable(dir, "mvnw", "mvn"), goals);
    }

    private static List<String> gradle(Path dir, Action action) {
        String executable = executable(dir, "gradlew", "gradle");
        return switch (action) {
            case BUILD -> List.of(executable, "build");
            case CLEAN_BUILD -> List.of(executable, "clean", "build");
            case CLEAN -> List.of(executable, "clean");
            case TEST -> List.of(executable, "test");
            case RUN -> List.of(executable, "run");
        };
    }

    /**
     * Ant targets, mirroring the original NetBeans {@code ModuleActions}: a NetBeans module
     * (apisupport) project — recognised by its {@code nbproject/project.xml} — uses the harness
     * targets ({@code build}, {@code clean}, {@code test-unit}, {@code run}), while a plain Ant
     * project uses the conventional {@code jar}/{@code test}/{@code run}.
     */
    private static List<String> ant(Path dir, Action action) {
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

    private static List<String> prepend(String head, List<String> tail) {
        List<String> command = new ArrayList<>(tail.size() + 1);
        command.add(head);
        command.addAll(tail);
        return List.copyOf(command);
    }

    private static String executable(Path dir, String wrapper, String fallback) {
        Path unix = dir.resolve(wrapper);
        if (Files.isRegularFile(unix)) {
            return unix.toString();
        }
        Path windows = dir.resolve(wrapper + ".cmd");
        if (Files.isRegularFile(windows)) {
            return windows.toString();
        }
        return fallback;
    }

    private static boolean anyFile(Path dir, String... names) {
        for (String name : names) {
            if (Files.isRegularFile(dir.resolve(name))) {
                return true;
            }
        }
        return false;
    }
}
