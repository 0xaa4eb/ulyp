package com.ulyp.ui.renderers;

import com.ulyp.core.printers.ToStringPrintedRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.ClassNameUtils;
import com.ulyp.ui.util.StyledText;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

import static com.ulyp.ui.util.CssClass.*;

public class RenderedToStringPrinted extends RenderedObject {

    public RenderedToStringPrinted(ToStringPrintedRepresentation representation, RenderSettings renderSettings) {
        super(representation.getType());

        String className = renderSettings.showTypes() ? representation.getType().getName() : ClassNameUtils.toSimpleName(representation.getType().getName());

        List<Node> nodes = new ArrayList<>();
        nodes.add(StyledText.of(className, CALL_TREE_TYPE_NAME));
        nodes.add(StyledText.of(": ", CALL_TREE_PLAIN_TEXT));
        nodes.add(RenderedObject.of(representation.getPrinted(), renderSettings));
        if (renderSettings.showTypes()) {
            nodes.add(StyledText.of("@", CALL_TREE_IDENTITY_REPR));
            nodes.add(StyledText.of(Integer.toHexString(representation.getIdentityHashCode()), CALL_TREE_IDENTITY_REPR));
        }

        super.getChildren().addAll(nodes);
    }
}
