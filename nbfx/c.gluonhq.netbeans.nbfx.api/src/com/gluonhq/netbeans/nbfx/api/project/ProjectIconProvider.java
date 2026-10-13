package com.gluonhq.netbeans.nbfx.api.project;

import javafx.scene.Node;
import org.openide.filesystems.FileObject;

/**
 * Resolves the icon a project view shows for a project root - its project type. A targeted service
 * replacing the navigator SPI's icon methods: the search/scope UI asks the registered provider for
 * the icon of a project instead of iterating the navigator views.
 *
 * @since 1.0
 */
public interface ProjectIconProvider {

    /**
     * The icon the Projects view shows for the project rooted at {@code projectRoot} - its project
     * type - or {@code null} when the project is unknown to every provider.
     *
     * @param projectRoot the project's root folder
     * @return an icon node, or {@code null}
     */
    Node createIcon(FileObject projectRoot);
}
