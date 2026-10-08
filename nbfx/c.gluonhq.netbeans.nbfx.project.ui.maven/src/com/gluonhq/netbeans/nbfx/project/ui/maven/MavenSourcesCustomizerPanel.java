package com.gluonhq.netbeans.nbfx.project.ui.maven;

import com.gluonhq.netbeans.nbfx.project.customizer.FxProjectCustomizerPanel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The Maven "Sources" project-properties category: the project's source roots. Read-only for now;
 * the same registration would back an editable panel.
 */
@ServiceProvider(service = FxProjectCustomizerPanel.class)
public final class MavenSourcesCustomizerPanel implements FxProjectCustomizerPanel {

    @Override
    public String projectTypeId() {
        return "maven";
    }

    @Override
    public String id() {
        return "sources";
    }

    @Override
    public String displayName() {
        return NbBundle.getMessage(MavenSourcesCustomizerPanel.class, "Sources.displayName");
    }

    @Override
    public int position() {
        return 100;
    }

    @Override
    public Node createPanel(Project project) {
        VBox box = new VBox(4);
        Sources sources = ProjectUtils.getSources(project);
        addGroups(box, sources, "java");
        addGroups(box, sources, "resources");
        if (box.getChildren().isEmpty()) {
            box.getChildren().add(new Label(NbBundle.getMessage(MavenSourcesCustomizerPanel.class, "Sources.none")));
        }
        return box;
    }

    private static void addGroups(VBox box, Sources sources, String type) {
        for (SourceGroup group : sources.getSourceGroups(type)) {
            box.getChildren().add(new Label(group.getDisplayName() + ": " + group.getRootFolder().getPath()));
        }
    }
}
