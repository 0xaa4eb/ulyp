package com.ulyp.ui.reader

import com.ulyp.core.util.ClassUtils
import com.ulyp.storage.tree.Filter

data class Filter(val minimumCount: Int, val excludeClasses: String) {

    fun toStorageFilter(): Filter {
        return Filter {
            !excludeClasses.contains(ClassUtils.getSimpleNameFromName(it.root.method.type.name)) &&
                    it.callCount() > minimumCount
        }
    }
}