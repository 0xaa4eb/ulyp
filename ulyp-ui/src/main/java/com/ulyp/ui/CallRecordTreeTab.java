package com.ulyp.ui;

import com.ulyp.core.*;
import com.ulyp.transport.RecordingInfo;
import com.ulyp.transport.TStackTraceElement;
import com.ulyp.ui.code.SourceCode;
import com.ulyp.ui.code.SourceCodeView;
import com.ulyp.ui.code.find.SourceCodeFinder;
import com.ulyp.ui.font.FontSizeChanger;
import com.ulyp.ui.util.ClassNameUtils;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

@Component
@Scope(value = "prototype")
public class CallRecordTreeTab extends Tab {

    private final Region parent;
    private final MethodInfoDatabase methodInfoDatabase;
    private final TypeInfoDatabase typeInfoDatabase;
    private final CallRecordDatabase database;

    @Nullable
    private CallRecord root;
    @Nullable
    private RecordingInfo recordingInfo;

    private TreeView<CallTreeNodeContent> treeView;

    @Autowired
    private SourceCodeView sourceCodeView;
    @Autowired
    private RenderSettings renderSettings;
    @Autowired
    private FontSizeChanger fontSizeChanger;

    private boolean initialized = false;

    @SuppressWarnings("unchecked")
    public CallRecordTreeTab(Region parent, CallRecordDatabase database, MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase) {
        this.parent = parent;
        this.methodInfoDatabase = methodInfoDatabase;
        this.typeInfoDatabase = typeInfoDatabase;
        this.database = database;
    }

    public synchronized void init() {
        if (initialized) {
            return;
        }

        treeView = new TreeView<>(new CallRecordTreeNode(database, root.getId(), renderSettings));
        treeView.prefHeightProperty().bind(parent.heightProperty());
        treeView.prefWidthProperty().bind(parent.widthProperty());

        SourceCodeFinder sourceCodeFinder = new SourceCodeFinder(recordingInfo.getProcessInfo().getClasspathList());

        treeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final CallRecordTreeNode selectedNode = (CallRecordTreeNode) newValue;
                    if (selectedNode != null && selectedNode.getCallRecord() != null) {
                        CompletableFuture<SourceCode> sourceCodeFuture = sourceCodeFinder.find(selectedNode.getCallRecord().getClassName());

                        sourceCodeFuture.thenAccept(
                                sourceCode -> {
                                    Platform.runLater(
                                            () -> {
                                                TreeItem<CallTreeNodeContent> currentlySelected = treeView.getSelectionModel().getSelectedItem();
                                                CallRecordTreeNode currentlySelectedNode = (CallRecordTreeNode) currentlySelected;
                                                if (selectedNode.getCallRecord().getId() == currentlySelectedNode.getCallRecord().getId()) {
                                                    sourceCodeView.setText(sourceCode, currentlySelectedNode.getCallRecord().getMethodName());
                                                }
                                            }
                                    );
                                }
                        );
                    }
                }
        );

        treeView.setOnKeyPressed(
                key -> {
                    if (key.getCode() == KeyCode.EQUALS) {
                        fontSizeChanger.upscale(parent.getScene());
                    }
                    if (key.getCode() == KeyCode.MINUS) {
                        fontSizeChanger.downscale(parent.getScene());
                    }
                }
        );

        setText(getTabName());
        setContent(treeView);
        setOnClosed(ev -> dispose());
        setTooltip(getTooltipText());

        initialized = true;
    }

    public synchronized String getTabName() {
        if (root == null || recordingInfo == null || database == null) {
            return "?";
        }

        return recordingInfo.getThreadName() + " " +
                ClassNameUtils.toSimpleName(root.getClassName()) + "." +  root.getMethodName() + "(" + recordingInfo.getLifetimeMillis() + " ms, " + database.countAll() + ")";
    }

    private synchronized Tooltip getTooltipText() {
        if (root == null || recordingInfo == null || database == null) {
            return new Tooltip("");
        }

        StringBuilder builder = new StringBuilder()
                .append("Thread: ").append(recordingInfo.getThreadName()).append("\n")
                .append("Created at: ").append(new Timestamp(this.recordingInfo.getCreateEpochMillis())).append("\n")
                .append("Finished at: ").append(new Timestamp(this.recordingInfo.getCreateEpochMillis() + this.recordingInfo.getLifetimeMillis())).append("\n")
                .append("Lifetime: ").append(recordingInfo.getLifetimeMillis()).append(" millis").append("\n");

        builder.append("Stack trace: ").append("\n");

        for (TStackTraceElement element: recordingInfo.getStackTrace().getElementList()) {
            builder.append("\tat ")
                    .append(element.getDeclaringClass())
                    .append(".")
                    .append(element.getMethodName())
                    .append("(")
                    .append(element.getFileName())
                    .append(":")
                    .append(element.getLineNumber())
                    .append(")")
                    .append("\n");
        }

        return new Tooltip(builder.toString());
    }

    @Nullable
    public CallRecordTreeNode getSelected() {
        return (CallRecordTreeNode) treeView.getSelectionModel().getSelectedItem();
    }

    public void dispose() {
        database.close();
    }

    public synchronized void refreshTreeView() {
        this.init();
        CallRecordTreeNode root = (CallRecordTreeNode) treeView.getRoot();
        setText(getTabName());
        root.refresh();
    }

    public synchronized void uploadChunk(CallRecordTreeChunk chunk) {
        try {

            if (recordingInfo == null) {
                this.recordingInfo = chunk.getRecordingInfo();
            }

            methodInfoDatabase.addAll(new MethodInfoList(chunk.getRequest().getMethodDescriptionList().getData()));
            typeInfoDatabase.addAll(chunk.getRequest().getDescriptionList());

            database.persistBatch(
                    new CallEnterRecordList(chunk.getRequest().getRecordLog().getEnterRecords()),
                    new CallExitRecordList(chunk.getRequest().getRecordLog().getExitRecords())
            );

            if (root == null) {
                root = database.getRoot();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
