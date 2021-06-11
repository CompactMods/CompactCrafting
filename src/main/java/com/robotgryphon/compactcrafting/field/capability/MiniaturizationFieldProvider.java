package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.field.MiniaturizationField;

public class MiniaturizationFieldProvider implements IMiniaturizationFieldProvider {

    private MiniaturizationField field;

    public MiniaturizationFieldProvider()  {}

    public MiniaturizationFieldProvider(MiniaturizationField field) {
        this.field = field;
    }

    @Override
    public MiniaturizationField getField() {
        return this.field;
    }
}
