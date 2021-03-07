package com.ulyp.ui.renderers;

import com.ulyp.core.printers.CollectionRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.StyledText;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ulyp.ui.util.CssClass.*;

public class RenderedCollection extends RenderedObject {

    protected RenderedCollection(CollectionRepresentation representation, RenderSettings renderSettings) {
        super(representation.getType());

        List<RenderedObject> renderedObjects = representation.getRecordedItems()
                .stream()
                .map(repr -> RenderedObject.of(repr, renderSettings))
                .collect(Collectors.toList());

        List<Node> texts = new ArrayList<>();

        if (renderSettings.showTypes()) {
            texts.add(StyledText.of(representation.getType().getName(), CALL_TREE_TYPE_NAME));
            texts.add(StyledText.of(": ", CALL_TREE_PLAIN_TEXT));
        }

        texts.add(StyledText.of("{", CALL_TREE_COLLECTION_BRACE));

        for (int i = 0; i < renderedObjects.size(); i++) {
            texts.add(renderedObjects.get(i));
            if (i != renderedObjects.size() - 1 || renderedObjects.size() < representation.getLength()) {
                texts.add(StyledText.of(", ", CALL_TREE_PLAIN_TEXT));
            }
        }

        if (renderedObjects.size() < representation.getLength()) {
            texts.add(StyledText.of((representation.getLength() - renderedObjects.size()) + " more...", CALL_TREE_PLAIN_TEXT));
        }

        texts.add(StyledText.of("}", CALL_TREE_COLLECTION_BRACE));

        super.getChildren().addAll(texts);
    }
}
