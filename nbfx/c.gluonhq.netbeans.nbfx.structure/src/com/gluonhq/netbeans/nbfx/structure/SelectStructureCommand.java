package com.gluonhq.netbeans.nbfx.structure;

import com.gluonhq.netbeans.nbfx.api.actions.AbstractCommand;
import com.gluonhq.netbeans.nbfx.api.actions.ActionIds;
import org.openide.util.NbBundle;

/**
 * Window ▸ IDE Tools ▸ Structure: brings the Structure view on screen. The menu entry itself lives
 * in the layer as {@code NbFx/Menus/Window/IDE Tools/selectStructure.ref} (registered on
 * {@link StructureView}); this command only carries the name and the behaviour.
 */
final class SelectStructureCommand extends AbstractCommand {

    SelectStructureCommand() {
        super(ActionIds.SELECT_STRUCTURE,
                NbBundle.getMessage(SelectStructureCommand.class, "CTL_SelectStructureCommand"),
                null, false);
    }

    @Override
    public void run() {
        StructureView view = StructureView.instance();
        if (view != null) {
            view.show();
        }
    }
}