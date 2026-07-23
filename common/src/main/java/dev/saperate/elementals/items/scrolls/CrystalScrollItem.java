package dev.saperate.elementals.items.scrolls;

import dev.saperate.elementals.elements.Element;
import dev.saperate.elementals.elements.crystal.abilities.CrystalElement;
import dev.saperate.elementals.elements.earth.EarthElement;

public class CrystalScrollItem extends AbstractScrollItem {

    public CrystalScrollItem(Properties settings) {
        super(settings);
    }

    @Override
    String getTranslatable() {
        return "item.elementals.crystal_scroll.tooltip";
    }

    @Override
    Element getElement() {
        return CrystalElement.get();
    }

    @Override
    Element getParentElement() {
        return EarthElement.get();
    }
}