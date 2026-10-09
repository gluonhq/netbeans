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
package com.gluonhq.netbeans.nbfx.ui.context;

import com.gluonhq.netbeans.nbfx.api.file.FileSelectionContext;

import java.io.IOException;
import java.util.List;

import junit.framework.Test;

import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Verifies that the nbfx {@code ContextGlobalProvider} feeds the nbfx selection into the NetBeans
 * global context, so {@code Utilities.actionsGlobalContext()} reflects the JavaFX frontend.
 */
public class NbfxGlobalContextProviderTest extends NbTestCase {

    public NbfxGlobalContextProviderTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(NbfxGlobalContextProviderTest.class)
                .clusters(".*")
                .enableModules(".*")
                .gui(false)
                .suite();
    }

    public void testGlobalContextReflectsFileSelection() throws IOException {
        FileSelectionContext files = Lookup.getDefault().lookup(FileSelectionContext.class);
        assertNotNull("no FileSelectionContext", files);

        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject file = fs.getRoot().createData("A.java");
        files.setSelectedFiles(List.of(file));

        Lookup global = Utilities.actionsGlobalContext();
        assertSame("global context does not carry the selected file", file, global.lookup(FileObject.class));
    }
}
