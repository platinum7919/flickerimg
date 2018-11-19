package com.foo.common.utils


fun CharSequence?.isNullOrEmptyOrBlank(): Boolean {
    if (this == null) {
        return true
    } else if (this.isNullOrEmpty()) {
        return true
    } else {
        return this.trim().isEmpty()
    }
}