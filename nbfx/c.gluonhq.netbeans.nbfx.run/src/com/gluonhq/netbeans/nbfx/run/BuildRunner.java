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

import com.gluonhq.netbeans.nbfx.output.FxConsole;
import com.gluonhq.netbeans.nbfx.output.FxOutput;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Runs a build action for a project directory: detects the build tool, opens (and clears) the
 * console named {@code consoleName}, and streams the tool's output into it. Shared by the Build
 * menu command and the navigator context menu.
 * <p>
 * The output service is resolved at call time and writing to a console makes the Output view show
 * itself, so a build always brings its output on screen.
 *
 * @since 1.0
 */
final class BuildRunner {

    private static final Logger LOG = Logger.getLogger(BuildRunner.class.getName());

    private BuildRunner() {
    }

    static void run(Path dir, BuildTool.Action action, String consoleName) {
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
        BuildTool tool = BuildTool.detect(dir);
        List<String> command = tool.commandLine(dir, action);
        if (command == null) {
            console.append(NbBundle.getMessage(BuildRunner.class, "BuildCommand.noTool") + "\n");
            return;
        }
        ProcessRunner.run(command, dir, console);
    }
}
