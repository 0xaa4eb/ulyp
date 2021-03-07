package com.ulyp.ui.renderers;

import com.ulyp.core.printers.MapRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.StyledText;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ulyp.ui.util.CssClass.*;

public class RenderedMap extends RenderedObject {

    protected RenderedMap(MapRepresentation representation, RenderSettings renderSettings) {
        super(representation.getType());

        List<RenderedObject> entries = representation.getEntries()
                .stream()
                .map(repr -> RenderedObject.of(repr, renderSettings))
                .collect(Collectors.toList());

        List<Node> texts = new ArrayList<>();

        if (renderSettings.showTypes()) {
            texts.add(StyledText.of(representation.getType().getName(), CALL_TREE_TYPE_NAME));
            texts.add(StyledText.of(": ", CALL_TREE_PLAIN_TEXT));
        }

        texts.add(StyledText.of("{", CALL_TREE_COLLECTION_BRACE));

        for (int i = 0; i < entries.size(); i++) {
            texts.add(entries.get(i));
            if (i != entries.size() - 1 || entries.size() < representation.getSize()) {
                texts.add(StyledText.of(", ", CALL_TREE_PLAIN_TEXT));
            }
        }

        if (entries.size() < representation.getSize()) {
            texts.add(StyledText.of((representation.getSize() - entries.size()) + " more...", CALL_TREE_PLAIN_TEXT));
        }

        texts.add(StyledText.of("}", CALL_TREE_COLLECTION_BRACE));

        super.getChildren().addAll(texts);
    }
}
