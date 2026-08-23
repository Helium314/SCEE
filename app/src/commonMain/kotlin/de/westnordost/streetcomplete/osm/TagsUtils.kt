package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

typealias Tags = StringMapChangesBuilder

/**
 * Expands `:both` tags to `:left` and `:right` tags.
 *
 * For example, if [key] is `"sidewalk"`, `sidewalk:both=X` is replaced with `sidewalk:left=X` and
 * `sidewalk:right=X`.
 *
 * If [includeBareTag] is `true`, also in this case `sidewalk=X` is replaced
 * with `sidewalk:left=X` and `sidewalk:right=X`.
 *
 * [postfix] is appended to the key name.
 */
fun Tags.expandSides(key: String, postfix: String? = null, includeBareTag: Boolean = true) {
    val post = if (postfix != null) ":$postfix" else ""
    val both = get("$key:both$post") ?: (if (includeBareTag) get("$key$post") else null)
    if (both != null) {
        // *:left/right is seen as more specific/correct in case the two contradict each other
        if (!containsKey("$key:left$post")) set("$key:left$post", both)
        if (!containsKey("$key:right$post")) set("$key:right$post", both)
    }
    remove("$key:both$post")
    if (includeBareTag) remove("$key$post")
}

/**
 * Replaces `:left` and `:right` tags that are identical with `:both` tags.
 *
 * For example, if [key] is `"sidewalk"`, `sidewalk:left=X` and `sidewalk:right=X` is replaced with
 * `sidewalk:both=X`.
 *
 * [postfix] is appended to the key name.
 */
fun Tags.mergeSides(key: String, postfix: String? = null) {
    val post = if (postfix != null) ":$postfix" else ""
    val left = get("$key:left$post")
    val right = get("$key:right$post")
    if (left != null && left == right) {
        set("$key:both$post", left)
        remove("$key:left$post")
        remove("$key:right$post")
    }
}

// convert simple key = value pairs into tags, and understand simple filter expressions
fun String.toTags(): Map<String, String> {
    val tags = mutableMapOf<String, String>()
    if (!contains('('))
        split("\n", " and ").forEach { line ->
            if (line.isBlank() || line.contains(" or ")) return@forEach
            val kv = line.split("=", "!~", "~")
            if (kv.size != 1 && kv.size != 2) return@forEach
            if ('|' in kv[0] || '!' in kv[0] || '*' in kv[0]) return@forEach
            if (kv.size == 1 || "!=" in line || '~' in line) tags[kv[0].trim()] = ""
            else tags[kv[0].trim()] = kv[1].trim()
        }
    return tags
}
