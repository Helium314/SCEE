package de.westnordost.streetcomplete.quests.kerb_type

enum class KerbType(val osmValue: String) {
    RAISED("raised"),
    LOWERED("lowered"),
    FLUSH("flush"),
    KERB_RAMP("lowered"),
    NO_KERB("no"),
}
