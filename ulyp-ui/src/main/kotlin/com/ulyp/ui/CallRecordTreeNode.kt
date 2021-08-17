package com.ulyp.ui

import com.ulyp.core.CallRecord
import com.ulyp.core.CallRecordDatabase
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem
import java.lang.ref.WeakReference
import java.util.function.Consumer

class CallRecordTreeNode(database: CallRecordDatabase, callRecordId: Long, renderSettings: RenderSettings?) :
    TreeItem<CallTreeNodeContent>(
        CallTreeNodeContent(
            database.find(callRecordId),
            renderSettings,
            database.countAll()
        )
    ) {
    private val renderSettings: RenderSettings?
    private val databaseRef: WeakReference<CallRecordDatabase>
    private val callRecordId: Long
    private var loaded = false
    fun refresh() {
        val database = databaseRef.get() ?: return
        value = CallTreeNodeContent(database.find(callRecordId), renderSettings, database.countAll())
        if (loaded) {
            val newChildren = database.getChildrenIds(callRecordId)
            val currentLoadedChildrenCount = children.size
            if (newChildren.size > currentLoadedChildrenCount) {
                for (i in currentLoadedChildrenCount until newChildren.size) {
                    children.add(CallRecordTreeNode(database, newChildren[i], renderSettings))
                }
            }
            children.forEach(Consumer { node: TreeItem<CallTreeNodeContent> -> (node as CallRecordTreeNode).refresh() })
        }
    }

    override fun getChildren(): ObservableList<TreeItem<CallTreeNodeContent>> {
        if (!loaded) {
            loadChildren()
        }
        return super.getChildren()
    }

    override fun isLeaf(): Boolean {
        val database = databaseRef.get()
        if (database == null) {
            loaded = true
            return true
        }
        return if (loaded) {
            children.isEmpty()
        } else {
            database.getChildrenIds(callRecordId).isEmpty()
        }
    }

    private fun loadChildren() {
        val database = databaseRef.get()
        if (database == null) {
            loaded = true
            return
        }
        val children: MutableList<CallRecordTreeNode> = ArrayList()
        val childrenIds = database.getChildrenIds(callRecordId)
        for (i in childrenIds.indices) {
            children.add(CallRecordTreeNode(database, childrenIds[i], renderSettings))
        }
        super.getChildren().setAll(children)
        loaded = true
    }

    val callRecord: CallRecord?
        get() {
            val database = databaseRef.get() ?: return null
            return database.find(callRecordId)
        }

    override fun toString(): String {
        return "FxCallRecord{" +
                "node=" + callRecord +
                '}'
    }

    init {
        databaseRef = WeakReference(database)
        this.callRecordId = callRecordId
        this.renderSettings = renderSettings
    }
}