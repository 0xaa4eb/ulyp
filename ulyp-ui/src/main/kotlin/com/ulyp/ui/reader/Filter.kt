package com.ulyp.ui.reader

import com.ulyp.storage.Filter

data class Filter(val minimumCount: Int) {

    fun toStorageFilter(): Filter {
        return com.ulyp.storage.Filter {
            it.callCount() > minimumCount
        }
    }
}