package com.foo.common.utils

val JoinTransformNonNullToString: ((Any?) -> CharSequence) = {
    if (it != null) it.toString() else ""
}


/**
 * Returns the first element that is not null (if you want null check)
 */
public fun <T> List<T>.firstNotNull(): T? {
    var i = 0
    while (i < this.size) {
        val item = this.get(i)
        if (item != null) {
            return item
        } else {
            i++
        }
    }
    return null
}
