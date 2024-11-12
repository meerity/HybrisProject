package com.epam.training.decorators;

import de.hybris.platform.impex.jalo.header.AbstractImpExCSVCellDecorator;

import java.util.Map;

//makes string lowercase and without whitespaces
public class ProductCodeDecorator extends AbstractImpExCSVCellDecorator {

    @Override
    public String decorate(int i, Map<Integer, String> map) {
        String parsedValue = map.get(i);
        if (parsedValue == null || parsedValue.isEmpty()){
            return parsedValue;
        }
        return parsedValue.toLowerCase().replaceAll("\\s+", "");
    }
}