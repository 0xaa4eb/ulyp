package com.ulyp.ui.renderers;

import com.ulyp.core.printers.MapEntryRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.StyledText;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

import static com.ulyp.ui.util.CssClass.CALL_TREE_PLAIN_TEXT;

public class RenderedMapEntry extends RenderedObject {

    protected RenderedMapEntry(MapEntryRepresentation entry, RenderSettings renderSettings) {
        super(entry.getType());

        List<Node> texts = new ArrayList<>();

        texts.add(RenderedObject.of(entry.getKey(), renderSettings));
        texts.add(StyledText.of(" -> ", CALL_TREE_PLAIN_TEXT));
        texts.add(RenderedObject.of(entry.getValue(), renderSettings));

        super.getChildren().addAll(texts);
    }
}
