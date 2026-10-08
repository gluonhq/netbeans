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

import com.gluonhq.netbeans.nbfx.api.actions.Command;
import com.gluonhq.netbeans.nbfx.api.project.OpenProject;
import com.gluonhq.netbeans.nbfx.api.project.ProjectRegistry;
import com.gluonhq.netbeans.nbfx.output.FxConsole;
import com.gluonhq.netbeans.nbfx.output.FxOutput;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.openide.util.NbBundle;

/**
 * A build command (Build / Clean / Test / Run) acting on the selected project. The project's build
 * tool is detected from its directory, its command line is run in the background, and the output is
 * streamed into a console named after the action and the project. The command is disabled while no
 * project is selected.
 *
 * @since 1.0
 */
final class BuildCommand implements Command {

    private final ProjectRegistry projects;
    private final FxOutput output;
    private final String id;
    private final String text;
    private final BuildTool.Action action;
    private final ReadOnlyBooleanWrapper disabled = new ReadOnlyBooleanWrapper(this, "disabled", true);

    BuildCommand(ProjectRegistry projects, FxOutput output, String id, String text,
            BuildTool.Action action) {
        this.projects = projects;
        this.output = output;
        this.id = id;
        this.text = text;
        this.action = action;
        if (projects != null) {
            disabled.bind(Bindings.createBooleanBinding(
                    () -> projects.selectedProjectProperty().getValue() == null,
                    projects.selectedProjectProperty()));
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public boolean isDisabled() {
        return disabled.get();
    }

    @Override
    public ReadOnlyBooleanProperty disabledProperty() {
        return disabled.getReadOnlyProperty();
    }

    @Override
    public void run() {
        OpenProject project = projects == null ? null : projects.getSelected();
        if (project == null || output == null) {
            return;
        }
        Path dir = Paths.get(project.getPath());
        BuildTool tool = BuildTool.detect(dir);
        FxConsole console = output.console(text + " " + project.getDisplayName());
        console.clear();
        console.show();
        List<String> command = tool.commandLine(dir, action);
        if (command == null) {
            console.append(NbBundle.getMessage(BuildCommand.class, "BuildCommand.noTool") + "\n");
            return;
        }
        ProcessRunner.run(command, dir, console);
    }
}
