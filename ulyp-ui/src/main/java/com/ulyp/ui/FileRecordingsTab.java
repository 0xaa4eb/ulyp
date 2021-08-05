package com.ulyp.ui;

import com.ulyp.core.CallRecordDatabase;
import com.ulyp.core.MethodInfoDatabase;
import com.ulyp.core.TypeInfoDatabase;
import com.ulyp.ui.util.FxThreadExecutor;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(scopeName = "prototype")
public class FileRecordingsTab extends Tab {

    private final FileRecordingsTabName name;
    private TabPane callTreeTabs;
    @Autowired
    private ApplicationContext applicationContext;

    private final MethodInfoDatabase methodInfoDatabase = new MethodInfoDatabase();
    private final TypeInfoDatabase typeInfoDatabase = new TypeInfoDatabase();
    private final Map<CallRecordTreeTabId, CallRecordTreeTab> tabsByRecordingId = new ConcurrentHashMap<>();

    FileRecordingsTab(FileRecordingsTabName name) {
        super(name.toString());

        this.name = name;
    }

    @PostConstruct
    public void init() {
        TabPane tabPane = new TabPane();
        this.callTreeTabs = tabPane;
        setContent(tabPane);
    }

    public FileRecordingsTabName getName() {
        return name;
    }

    public CallRecordTreeTab getOrCreateRecordingTab(AggregationStrategy aggregationStrategy, CallRecordTreeChunk chunk) {
        CallRecordTreeTabId id = aggregationStrategy.getId(chunk);

        return FxThreadExecutor.execute(
                () -> tabsByRecordingId.computeIfAbsent(id, rId -> {
                    CallRecordDatabase callRecordDatabase = aggregationStrategy.buildDatabase(methodInfoDatabase, typeInfoDatabase);
                    CallRecordTreeTab tab = applicationContext.getBean(CallRecordTreeTab.class, callTreeTabs, callRecordDatabase, methodInfoDatabase, typeInfoDatabase);
                    callTreeTabs.getTabs().add(tab);
                    tab.setOnClosed(ev -> this.tabsByRecordingId.remove(id));
                    return tab;
                })
        );
    }

    public CallRecordTreeTab getSelectedTreeTab() {
        return (CallRecordTreeTab) callTreeTabs.getSelectionModel().getSelectedItem();
    }

    public void dispose() {
        for (Tab tab : callTreeTabs.getTabs()) {
            CallRecordTreeTab fxCallRecordTreeTab = (CallRecordTreeTab) tab;
            fxCallRecordTreeTab.dispose();
        }
    }
}
