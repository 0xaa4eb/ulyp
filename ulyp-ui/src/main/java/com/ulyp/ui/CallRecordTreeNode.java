package com.ulyp.ui;

import com.ulyp.core.CallRecord;
import com.ulyp.core.CallRecordDatabase;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CallRecordTreeNode extends TreeItem<CallTreeNodeContent> {

    private final RenderSettings renderSettings;
    private final WeakReference<CallRecordDatabase> databaseRef;
    private final long callRecordId;

    private boolean loaded = false;

    public CallRecordTreeNode(CallRecordDatabase database, long callRecordId, RenderSettings renderSettings) {
        super(new CallTreeNodeContent(database.find(callRecordId), renderSettings, database.countAll()));
        this.databaseRef = new WeakReference<>(database);
        this.callRecordId = callRecordId;
        this.renderSettings = renderSettings;
    }

    public void refresh() {
        CallRecordDatabase database = databaseRef.get();
        if (database == null) {
            return;
        }

        setValue(new CallTreeNodeContent(database.find(callRecordId), renderSettings, database.countAll()));

        if (loaded) {
            List<Long> newChildren = database.getChildrenIds(callRecordId);
            int currentLoadedChildrenCount = getChildren().size();

            if (newChildren.size() > currentLoadedChildrenCount) {
                for (int i = currentLoadedChildrenCount; i < newChildren.size(); i++) {
                    getChildren().add(new CallRecordTreeNode(database, newChildren.get(i), renderSettings));
                }
            }

            getChildren().forEach(node -> ((CallRecordTreeNode) node).refresh());
        }
    }

    @Override
    public ObservableList<TreeItem<CallTreeNodeContent>> getChildren() {
        if (!loaded) {
            loadChildren();
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        CallRecordDatabase database = databaseRef.get();
        if (database == null) {
            loaded = true;
            return true;
        }

        if (loaded) {
            return getChildren().isEmpty();
        } else {
            return database.getChildrenIds(callRecordId).isEmpty();
        }
    }

    private void loadChildren() {
        CallRecordDatabase database = databaseRef.get();
        if (database == null) {
            loaded = true;
            return;
        }

        List<CallRecordTreeNode> children = new ArrayList<>();

        List<Long> childrenIds = database.getChildrenIds(callRecordId);
        for (int i = 0; i < childrenIds.size(); i++) {
            children.add(new CallRecordTreeNode(database, childrenIds.get(i), renderSettings));
        }

        super.getChildren().setAll(children);
        loaded = true;
    }

    public CallRecord getCallRecord() {
        CallRecordDatabase database = databaseRef.get();
        if (database == null) {
            return null;
        }

        return database.find(callRecordId);
    }

    @Override
    public String toString() {
        return "FxCallRecord{" +
                "node=" + getCallRecord() +
                '}';
    }
}
