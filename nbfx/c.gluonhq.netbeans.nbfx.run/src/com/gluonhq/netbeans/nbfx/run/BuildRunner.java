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

import com.gluonhq.netbeans.nbfx.api.progress.FxProgress;
import com.gluonhq.netbeans.nbfx.output.FxConsole;
import com.gluonhq.netbeans.nbfx.output.FxOutput;
import com.gluonhq.netbeans.nbfx.project.ui.api.BuildAction;
import com.gluonhq.netbeans.nbfx.project.ui.api.BuildActionProvider;
import com.gluonhq.netbeans.nbfx.project.ui.api.ProjectKinds;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Runs a build action for a project directory: detects the build tool, opens (and clears) the
 * console named {@code consoleName}, and streams the tool's output into it. Shared by the Build
 * menu command and the navigator context menu.
 * <p>
 * The output service is resolved at call time and writing to a console makes the Output view show
 * itself, so a build always brings its output on screen. While the tool runs, the status bar shows
 * a cancellable progress, the way NetBeans does.
 *
 * @since 1.0
 */
final class BuildRunner {

    private static final Logger LOG = Logger.getLogger(BuildRunner.class.getName());

    private BuildRunner() {
    }

    static void run(Path dir, BuildAction action, String consoleName) {
        if (dir == null) {
            return;
        }
        FxOutput output = Lookup.getDefault().lookup(FxOutput.class);
        if (output == null) {
            LOG.warning("No FxOutput service; build output cannot be shown");
            return;
        }
        FxConsole console = output.console(consoleName);
        console.clear();
        console.show();

        List<String> command = command(dir, action);
        if (command == null) {
            console.append(NbBundle.getMessage(BuildRunner.class, "BuildCommand.noTool") + "\n");
            return;
        }

        FxProgress progress = Lookup.getDefault().lookup(FxProgress.class);
        AtomicReference<Process> process = new AtomicReference<>();
        if (progress != null) {
            progress.start(consoleName, () -> {
                Process running = process.get();
                if (running != null) {
                    running.destroy();
                }
            });
        }
        ProcessRunner.run(command, dir, console, process::set, () -> {
            if (progress != null) {
                progress.finish();
            }
        });
    }

    /**
     * The command line for {@code action}: the project-type {@link BuildActionProvider} for the
     * project at {@code dir} when there is one, otherwise the marker-file detected {@link BuildTool}.
     * This mirrors the original {@code ProjectAction} dispatching to the project's {@code ActionProvider}.
     */
    private static List<String> command(Path dir, BuildAction action) {
        BuildActionProvider provider = providerFor(dir);
        if (provider != null) {
            List<String> command = provider.commandLine(dir, action);
            if (command != null) {
                return command;
            }
        }
        return BuildTool.detect(dir).commandLine(dir, action);
    }

    private static BuildActionProvider providerFor(Path dir) {
        FileObject fileObject = FileUtil.toFileObject(dir.toFile());
        if (fileObject == null) {
            return null;
        }
        try {
            Project project = ProjectManager.getDefault().findProject(fileObject);
            if (project == null) {
                return null;
            }
            String kind = ProjectKinds.providerOf(project).id();
            for (BuildActionProvider provider : Lookup.getDefault().lookupAll(BuildActionProvider.class)) {
                if (kind.equals(provider.projectTypeId())) {
                    return provider;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.FINE, "No project for " + dir, ex);
        }
        return null;
    }
}
