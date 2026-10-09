package com.gluonhq.netbeans.nbfx.ui.context;

import com.gluonhq.netbeans.nbfx.api.editor.EditorContext;
import com.gluonhq.netbeans.nbfx.api.editor.EditorDocument;
import com.gluonhq.netbeans.nbfx.api.file.FileSelectionContext;
import com.gluonhq.netbeans.nbfx.api.view.ViewProvider;
import com.gluonhq.netbeans.nbfx.api.view.ViewSelectionContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.openide.filesystems.FileObject;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

/**
 * Feeds the nbfx selection into the NetBeans global context
 * ({@link org.openide.util.Utilities#actionsGlobalContext()}).
 * <p>
 * This mirrors the Swing {@code GlobalActionContextImpl}: where that one reads the activated
 * {@code TopComponent}, this one reads the active nbfx view and the shared selection contexts. The
 * entries are the raw selection - the selected {@link FileObject}s, the active {@link EditorDocument}
 * and the active {@link ViewProvider} - never resolved domain objects: those are produced on demand by
 * per-domain facades (for example {@code ProjectContext}).
 * <p>
 * {@code Utilities.actionsGlobalContext()} uses a single {@code ContextGlobalProvider}; nbfx registers
 * this one so the global context reflects the JavaFX frontend rather than the hidden Swing window. The
 * explicit {@code position} makes {@code Lookup.getDefault()} return this provider before the
 * platform's {@code GlobalActionContextImpl} (which has no position and is therefore ordered last).
 */
@ServiceProvider(service = ContextGlobalProvider.class, position = 100)
public final class NbfxGlobalContextProvider implements ContextGlobalProvider {

    @Override
    public Lookup createGlobalContext() {
        InstanceContent content = new InstanceContent();
        AbstractLookup lookup = new AbstractLookup(content);

        FileSelectionContext files = Lookup.getDefault().lookup(FileSelectionContext.class);
        if (files != null) {
            AtomicReference<List<FileObject>> current = new AtomicReference<>(List.of());
            files.selectedFiles().subscribe(selected -> {
                List<FileObject> now = selected == null ? List.of() : selected;
                for (FileObject file : current.getAndSet(now)) {
                    content.remove(file);
                }
                for (FileObject file : now) {
                    content.add(file);
                }
            });
        }

        EditorContext editors = Lookup.getDefault().lookup(EditorContext.class);
        if (editors != null) {
            AtomicReference<EditorDocument> current = new AtomicReference<>();
            editors.activeDocumentProperty().subscribe(document -> {
                EditorDocument old = current.getAndSet(document);
                if (old != null) {
                    content.remove(old);
                }
                if (document != null) {
                    content.add(document);
                }
            });
        }

        ViewSelectionContext views = Lookup.getDefault().lookup(ViewSelectionContext.class);
        if (views != null) {
            AtomicReference<ViewProvider> current = new AtomicReference<>();
            views.activeView().subscribe(view -> {
                ViewProvider old = current.getAndSet(view);
                if (old != null) {
                    content.remove(old);
                }
                if (view != null) {
                    content.add(view);
                }
            });
        }

        return lookup;
    }
}
