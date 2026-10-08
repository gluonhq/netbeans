package com.gluonhq.netbeans.nbfx.project.customizer;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import org.netbeans.api.project.Project;
import org.openide.util.NbBundle;

/**
 * The JavaFX project-properties dialog: a category list on the left and the selected panel on the
 * right, with OK / Apply / Cancel. Apply applies without closing; OK applies and closes; Cancel
 * discards. Apply and OK are gated on the panels' {@code isValid}/{@code isChanged}.
 */
public final class FxProjectCustomizerDialog {

    private FxProjectCustomizerDialog() {
    }

    public static void show(Project project) {
        List<FxProjectCustomizerPanel> panels = FxProjectCustomizers.panelsFor(project);
        if (panels.isEmpty()) {
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(NbBundle.getMessage(FxProjectCustomizerDialog.class, "FxProjectCustomizerDialog.Title"));
        dialog.setResizable(true);

        ListView<FxProjectCustomizerPanel> categories = new ListView<>(FXCollections.observableArrayList(panels));
        categories.setPrefWidth(180);
        categories.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(FxProjectCustomizerPanel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.displayName());
            }
        });

        StackPane content = new StackPane();
        content.setPadding(new Insets(12));
        content.setPrefSize(520, 360);
        categories.getSelectionModel().selectedItemProperty().addListener((obs, old, panel) ->
                content.getChildren().setAll(panel == null ? List.of() : List.of(panel.createPanel(project))));
        categories.getSelectionModel().selectFirst();

        SplitPane split = new SplitPane(categories, content);
        split.setDividerPositions(0.28);
        dialog.getDialogPane().setContent(split);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.APPLY, ButtonType.CANCEL);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
        Runnable refreshButtons = () -> {
            boolean valid = panels.stream().allMatch(FxProjectCustomizerPanel::isValid);
            boolean changed = panels.stream().anyMatch(FxProjectCustomizerPanel::isChanged);
            okButton.setDisable(!valid);
            applyButton.setDisable(!valid || !changed);
        };
        refreshButtons.run();

        ((Button) applyButton).addEventFilter(ActionEvent.ACTION, event -> {
            applyAll(panels, project);
            refreshButtons.run();
            event.consume();
        });

        ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
        if (result == ButtonType.OK) {
            applyAll(panels, project);
        } else if (result == ButtonType.CANCEL) {
            for (FxProjectCustomizerPanel panel : panels) {
                panel.cancel();
            }
        }
    }

    private static void applyAll(List<FxProjectCustomizerPanel> panels, Project project) {
        for (FxProjectCustomizerPanel panel : panels) {
            if (panel.isChanged()) {
                panel.apply(project);
            }
        }
    }
}
