package com.ulyp.ui.renderers;

import com.ulyp.core.printers.ObjectArrayRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.StyledText;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ulyp.ui.util.CssClass.CALL_TREE_COLLECTION_BRACE;
import static com.ulyp.ui.util.CssClass.CALL_TREE_PLAIN_TEXT;

public class RenderedObjectArray extends RenderedObject {

    protected RenderedObjectArray(ObjectArrayRepresentation arrayRepresentation, RenderSettings renderSettings) {
        super(arrayRepresentation.getType());

        List<RenderedObject> renderedObjects = arrayRepresentation.getRecordedItems()
                .stream()
                .map(repr -> RenderedObject.of(repr, renderSettings))
                .collect(Collectors.toList());

        List<Node> texts = new ArrayList<>();

        texts.add(StyledText.of("[", CALL_TREE_COLLECTION_BRACE));

        for (int i = 0; i < renderedObjects.size(); i++) {
            texts.add(renderedObjects.get(i));
            if (i != renderedObjects.size() - 1 || renderedObjects.size() < arrayRepresentation.getLength()) {
                texts.add(StyledText.of(", ", CALL_TREE_PLAIN_TEXT));
            }
        }

        if (renderedObjects.size() < arrayRepresentation.getLength()) {
            texts.add(StyledText.of((arrayRepresentation.getLength() - renderedObjects.size()) + " more...", CALL_TREE_PLAIN_TEXT));
        }

        texts.add(StyledText.of("]", CALL_TREE_COLLECTION_BRACE));

        super.getChildren().addAll(texts);
    }
}
