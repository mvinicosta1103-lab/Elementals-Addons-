package dev.saperate.elementals.items.scrolls;


import dev.saperate.elementals.elements.Element;
import dev.saperate.elementals.elements.water.WaterElement;


public class WaterScrollItem extends AbstractScrollItem {

    public WaterScrollItem(Properties settings) {
        super(settings);
    }

    @Override
    String getTranslatable() {
        return "item.elementals.water_scroll.tooltip";
    }

    @Override
    Element getElement() {
        return WaterElement.get();
    }

}