package com.ulyp.ui.renderers;

import com.ulyp.core.printers.IdentityObjectRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.ClassNameUtils;
import com.ulyp.ui.util.StyledText;

import java.util.Arrays;

import static com.ulyp.ui.util.CssClass.CALL_TREE_IDENTITY_REPR;
import static com.ulyp.ui.util.CssClass.CALL_TREE_TYPE_NAME;

public class RenderedIdentityObject extends RenderedObject {

    public RenderedIdentityObject(IdentityObjectRepresentation repr, RenderSettings renderSettings) {
        super(repr.getType());

        String className = renderSettings.showTypes() ? repr.getType().getName() : ClassNameUtils.toSimpleName(repr.getType().getName());

        super.getChildren().addAll(
                Arrays.asList(
                        StyledText.of(className, CALL_TREE_TYPE_NAME),
                        StyledText.of("@", CALL_TREE_IDENTITY_REPR),
                        StyledText.of(Integer.toHexString(repr.getHashCode()), CALL_TREE_IDENTITY_REPR)
                )
        );
    }
}
