package com.ulyp.ui.renderers;

import com.ulyp.core.Type;
import com.ulyp.core.printers.*;
import com.ulyp.ui.RenderSettings;
import javafx.scene.text.TextFlow;

public abstract class RenderedObject extends TextFlow {

    private final Type type;

    protected RenderedObject(Type type) {
        this.type = type;
    }

    public static RenderedObject of(ObjectRepresentation repr, RenderSettings renderSettings) {
        RenderedObject objectValue;

        // TODO replace with map
        if (repr instanceof StringObjectRepresentation) {

            objectValue = new RenderedStringObject((StringObjectRepresentation) repr, repr.getType(), renderSettings);
        } else if (repr instanceof NullObjectRepresentation) {

            objectValue = new RenderedNull(renderSettings);
        } else if (repr instanceof NotRecordedObjectRepresentation) {

            objectValue = new RenderedNotRecordedObject(renderSettings);
        } else if (repr instanceof NumberObjectRepresentation) {

            objectValue = new RenderedNumber((NumberObjectRepresentation) repr, repr.getType(), renderSettings);
        } else if (repr instanceof ObjectArrayRepresentation) {

            objectValue = new RenderedObjectArray((ObjectArrayRepresentation) repr, renderSettings);
        } else if (repr instanceof CollectionRepresentation) {

            objectValue = new RenderedCollection((CollectionRepresentation) repr, renderSettings);
        } else if (repr instanceof MapEntryRepresentation) {

            objectValue = new RenderedMapEntry((MapEntryRepresentation) repr, renderSettings);
        } else if (repr instanceof ClassObjectRepresentation) {

            objectValue = new RenderedClassObject((ClassObjectRepresentation) repr, renderSettings);
        } else if (repr instanceof MapRepresentation) {

            objectValue = new RenderedMap((MapRepresentation) repr, renderSettings);
        } else if (repr instanceof IdentityObjectRepresentation) {

            objectValue = new RenderedIdentityObject((IdentityObjectRepresentation) repr, renderSettings);
        } else if (repr instanceof ThrowableRepresentation) {

            objectValue = new RenderedThrowable((ThrowableRepresentation) repr, renderSettings);
        } else if (repr instanceof EnumRepresentation) {

            objectValue = new RenderedEnum((EnumRepresentation) repr, renderSettings);
        } else if (repr instanceof ToStringPrintedRepresentation) {

            objectValue = new RenderedToStringPrinted((ToStringPrintedRepresentation) repr, renderSettings);
        } else if (repr instanceof DateRepresentation) {

            objectValue = new RenderDate((DateRepresentation) repr, renderSettings);
        } else if (repr instanceof BooleanRepresentation) {

            objectValue = new RenderedBoolean((BooleanRepresentation) repr, renderSettings);
        } else {

            throw new RuntimeException("Not supported for rendering: " + repr);
        }

        objectValue.getChildren().forEach(node -> {
            node.getStyleClass().add("ulyp-ctt");
            node.getStyleClass().add("ulyp-ctt-object-repr");
        });
        return objectValue;
    }
}
