package com.ulyp.ui

import com.ulyp.core.MethodInfoDatabase
import com.ulyp.core.TypeInfoDatabase
import com.ulyp.ui.util.FxThreadExecutor.execute
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Component
@Scope(scopeName = "prototype")
class FileRecordingsTab internal constructor(val name: FileRecordingsTabName) : Tab(name.toString()) {
    private var callTreeTabs: TabPane? = null

    @Autowired
    private val applicationContext: ApplicationContext? = null
    private val methodInfoDatabase = MethodInfoDatabase()
    private val typeInfoDatabase = TypeInfoDatabase()
    private val tabsByRecordingId: MutableMap<CallRecordTreeTabId, CallRecordTreeTab> = ConcurrentHashMap()
    @PostConstruct
    fun init() {
        val tabPane = TabPane()
        callTreeTabs = tabPane
        content = tabPane
    }

    fun getOrCreateRecordingTab(
        aggregationStrategy: AggregationStrategy,
        chunk: CallRecordTreeChunk
    ): CallRecordTreeTab {
        val id = aggregationStrategy.getId(chunk)
        return execute {
            tabsByRecordingId.computeIfAbsent(id) { rId: CallRecordTreeTabId? ->
                val callRecordDatabase = aggregationStrategy.buildDatabase(methodInfoDatabase, typeInfoDatabase)
                val tab = applicationContext!!.getBean(
                    CallRecordTreeTab::class.java,
                    callTreeTabs,
                    callRecordDatabase,
                    methodInfoDatabase,
                    typeInfoDatabase
                )
                callTreeTabs!!.tabs.add(tab)
                tab.onClosed = EventHandler { ev: Event? -> tabsByRecordingId.remove(id) }
                tab
            }
        }
    }

    val selectedTreeTab: CallRecordTreeTab
        get() = callTreeTabs!!.selectionModel.selectedItem as CallRecordTreeTab

    fun dispose() {
        for (tab in callTreeTabs!!.tabs) {
            val fxCallRecordTreeTab = tab as CallRecordTreeTab
            fxCallRecordTreeTab.dispose()
        }
    }
}