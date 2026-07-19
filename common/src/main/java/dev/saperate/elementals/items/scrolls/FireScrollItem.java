package dev.saperate.elementals.items.scrolls;

import dev.saperate.elementals.elements.Element;
import dev.saperate.elementals.elements.fire.FireElement;

public class FireScrollItem extends AbstractScrollItem {

    public FireScrollItem(Properties settings) {
        super(settings);
    }

    @Override
    String getTranslatable() {
        return "item.elementals.fire_scroll.tooltip";
    }

    @Override
    Element getElement() {
        return FireElement.get();
    }

}