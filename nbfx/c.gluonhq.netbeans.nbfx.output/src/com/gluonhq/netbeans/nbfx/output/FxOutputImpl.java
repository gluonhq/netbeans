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
package com.gluonhq.netbeans.nbfx.output;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default {@link FxOutput}: a concurrent map of consoles and an observable, ordered view of them
 * for the output view.
 *
 * @since 1.0
 */
@ServiceProvider(service = FxOutput.class)
public final class FxOutputImpl implements FxOutput {

    private final Map<String, ConsoleModel> consoles = new ConcurrentHashMap<>();
    private final ObservableList<ConsoleModel> ordered = FXCollections.observableArrayList();

    @Override
    public FxConsole console(String name) {
        return consoles.computeIfAbsent(name, consoleName -> {
            ConsoleModel model = new ConsoleModel(consoleName);
            FxThread.run(() -> ordered.add(model));
            return model;
        });
    }

    /** The consoles in the order they were created, for the output view. */
    ObservableList<ConsoleModel> consoles() {
        return ordered;
    }
}
