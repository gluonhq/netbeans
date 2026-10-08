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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * Runs an external process in the background and streams its output into a console. The error
 * stream is merged into the output stream, so the console shows the tool's own ordering.
 *
 * @since 1.0
 */
final class ProcessRunner {

    private ProcessRunner() {
    }

    /** Starts {@code command} in {@code dir} on a daemon thread, streaming into {@code console}. */
    static void run(List<String> command, Path dir, FxConsole console) {
        Thread thread = new Thread(() -> execute(command, dir, console), "nbfx-run");
        thread.setDaemon(true);
        thread.start();
    }

    private static void execute(List<String> command, Path dir, FxConsole console) {
        console.append("$ " + String.join(" ", command) + "\n");
        Process process = null;
        try {
            process = new ProcessBuilder(command)
                    .directory(dir.toFile())
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    console.append(line + "\n");
                }
            }
            console.append("\n[exit " + process.waitFor() + "]\n");
        } catch (IOException ex) {
            console.append("\n[failed to start: " + ex.getMessage() + "]\n");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroy();
            }
            console.append("\n[interrupted]\n");
        }
    }
}
