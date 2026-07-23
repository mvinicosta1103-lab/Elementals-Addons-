package dev.saperate.elementals.items.scrolls;

import dev.saperate.elementals.elements.Element;
import dev.saperate.elementals.elements.earth.EarthElement;
import dev.saperate.elementals.elements.mud.abilities.MudElement;

public class MudScrollItem extends AbstractScrollItem {

    public MudScrollItem(Properties settings) {
        super(settings);
    }

    @Override
    String getTranslatable() {
        return "item.elementals.mud_scroll.tooltip";
    }

    @Override
    Element getElement() {
        return MudElement.get();
    }

    @Override
    Element getParentElement() {
        return EarthElement.get();
    }
}