package com.ulyp.ui.renderers;

import com.ulyp.core.printers.EnumRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.ClassNameUtils;
import com.ulyp.ui.util.StyledText;

import java.util.Arrays;

import static com.ulyp.ui.util.CssClass.*;

public class RenderedEnum extends RenderedObject {

    public RenderedEnum(EnumRepresentation representation, RenderSettings renderSettings) {
        super(representation.getType());

        String className = renderSettings.showTypes() ? representation.getType().getName() : ClassNameUtils.toSimpleName(representation.getType().getName());

        super.getChildren().addAll(
                Arrays.asList(
                        StyledText.of(className, CALL_TREE_TYPE_NAME),
                        StyledText.of(".", CALL_TREE_PLAIN_TEXT),
                        StyledText.of(representation.getName(), CALL_TREE_PLAIN_TEXT)
                )
        );
    }
}
