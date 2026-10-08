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
package com.gluonhq.netbeans.nbfx.launcher;

import java.awt.Desktop;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 * The macOS application-menu integration (Preferences… and Quit).
 * <p>
 * <b>Quarantine.</b> The macOS application menu is owned by the OS, and JavaFX has no API to add
 * items to it: {@code MenuBar.setUseSystemMenuBar(true)} only mirrors the MenuBar's own menus. The
 * only supported mechanism is the AWT {@link Desktop} handlers (the same ones NetBeans' applemenu
 * module uses); no Swing is involved, and the handlers only route to the pure-FX Options dialog and
 * the normal exit path. Like {@link SwingWindowSuppressor}, this is the exception the purity
 * guardrail allows.
 *
 * @since 1.0
 */
final class MacApplicationMenu {

    private static final Logger LOG = Logger.getLogger(MacApplicationMenu.class.getName());

    private MacApplicationMenu() {
    }

    /**
     * Registers the app-menu handlers when running on macOS. {@code openPreferences} and
     * {@code quit} run on the JavaFX Application Thread.
     */
    static void install(Runnable openPreferences, Runnable quit) {
        if (!isMac()) {
            return;
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.setPreferencesHandler(event -> Platform.runLater(openPreferences));
            desktop.setQuitHandler((event, response) -> {
                // Let the application confirm unsaved changes and exit itself.
                response.cancelQuit();
                Platform.runLater(quit);
            });
        } catch (Throwable ex) {
            LOG.log(Level.WARNING, "Could not register the macOS application-menu handlers", ex);
        }
    }

    private static boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac");
    }
}
