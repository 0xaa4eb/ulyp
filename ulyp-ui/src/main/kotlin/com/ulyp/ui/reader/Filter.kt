package com.ulyp.ui.reader

import com.ulyp.core.util.ClassUtils
import com.ulyp.storage.Filter

data class Filter(val minimumCount: Int, val excludeClasses: String) {

    fun toStorageFilter(): Filter {
        return com.ulyp.storage.Filter {
            !excludeClasses.contains(ClassUtils.getSimpleNameFromName(it.root.method.declaringType.name)) &&
                    it.callCount() > minimumCount
        }
    }
}