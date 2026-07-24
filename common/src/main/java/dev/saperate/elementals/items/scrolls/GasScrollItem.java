package dev.saperate.elementals.items.scrolls;

import dev.saperate.elementals.elements.Element;
import dev.saperate.elementals.elements.air.AirElement;
import dev.saperate.elementals.elements.gas.GasElement;

public class GasScrollItem extends AbstractScrollItem {

    public GasScrollItem(Properties settings) {
        super(settings);
    }

    @Override
    String getTranslatable() {
        return "item.elementals.gas_scroll.tooltip";
    }

    @Override
    Element getElement() {
        return GasElement.get();
    }

    @Override
    Element getParentElement() {
        return AirElement.get();
    }
}