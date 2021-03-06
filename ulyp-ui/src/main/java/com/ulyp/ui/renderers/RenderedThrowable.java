package com.ulyp.ui.renderers;

import com.ulyp.core.printers.NullObjectRepresentation;
import com.ulyp.core.printers.ThrowableRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.ClassNameUtils;
import com.ulyp.ui.util.StyledText;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

import static com.ulyp.ui.util.CssClass.CALL_TREE_IDENTITY_REPR;
import static com.ulyp.ui.util.CssClass.CALL_TREE_TYPE_NAME;

public class RenderedThrowable extends RenderedObject {

    protected RenderedThrowable(ThrowableRepresentation representation, RenderSettings renderSettings) {
        super(representation.getType());

        String className = renderSettings.showTypes() ? representation.getType().getName() : ClassNameUtils.toSimpleName(representation.getType().getName());

        List<Node> children = new ArrayList<>();

        children.add(StyledText.of(className, CALL_TREE_TYPE_NAME));
        children.add(StyledText.of("(", CALL_TREE_IDENTITY_REPR));
        if (!(representation.getMessage() instanceof NullObjectRepresentation)) {
            children.add(RenderedObject.of(representation.getMessage(), renderSettings));
        }
        children.add(StyledText.of(")", CALL_TREE_IDENTITY_REPR));

        super.getChildren().addAll(children);
    }
}
