package com.gluonhq.netbeans.nbfx.propertysheet;

import static com.gluonhq.netbeans.nbfx.api.actions.ActionIds.SELECT_PROPERTIES;

import com.gluonhq.netbeans.nbfx.annotations.FxActionReference;

/**
 * Declares the {@code Window > IDE Tools > Properties} entry in the layer. The processor writes
 * {@code NbFx/Menus/Window/IDE Tools/selectProperties.ref}, which the window reads.
 * <p>
 * This class carries metadata only; it is never instantiated.
 */
@FxActionReference(id = SELECT_PROPERTIES, path = "Menus/Window/IDE Tools", position = 10)
final class PropertiesMenuRegistrations {

    private PropertiesMenuRegistrations() {
    }
}
