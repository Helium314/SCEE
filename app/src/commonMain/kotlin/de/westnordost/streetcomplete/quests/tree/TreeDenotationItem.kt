package de.westnordost.streetcomplete.quests.tree

import de.westnordost.streetcomplete.quests.tree.TreeDenotation.AGRICULTURAL
import de.westnordost.streetcomplete.quests.tree.TreeDenotation.AVENUE
import de.westnordost.streetcomplete.quests.tree.TreeDenotation.GARDEN
import de.westnordost.streetcomplete.quests.tree.TreeDenotation.LANDMARK
import de.westnordost.streetcomplete.quests.tree.TreeDenotation.NATURAL
import de.westnordost.streetcomplete.quests.tree.TreeDenotation.NATURAL_MONUMENT
import de.westnordost.streetcomplete.quests.tree.TreeDenotation.PARK
import de.westnordost.streetcomplete.quests.tree.TreeDenotation.URBAN
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource

val TreeDenotation.title: StringResource get() = when (this) {
    LANDMARK ->         Res.string.quest_tree_denotation_landmark
    NATURAL_MONUMENT -> Res.string.quest_tree_denotation_natural_monument
    AGRICULTURAL ->     Res.string.quest_tree_denotation_agricultural
    PARK ->             Res.string.quest_tree_denotation_park
    GARDEN ->           Res.string.quest_tree_denotation_garden
    AVENUE ->           Res.string.quest_tree_denotation_avenue
    URBAN ->            Res.string.quest_tree_denotation_urban
    NATURAL ->          Res.string.quest_tree_denotation_natural
}

val TreeDenotation.description: StringResource get() = when (this) {
    LANDMARK ->         Res.string.quest_tree_denotation_landmark_description
    NATURAL_MONUMENT -> Res.string.quest_tree_denotation_natural_monument_description
    AGRICULTURAL ->     Res.string.quest_tree_denotation_agricultural_description
    PARK ->             Res.string.quest_tree_denotation_park_description
    GARDEN ->           Res.string.quest_tree_denotation_garden_description
    AVENUE ->           Res.string.quest_tree_denotation_avenue_description
    URBAN ->            Res.string.quest_tree_denotation_urban_description
    NATURAL ->          Res.string.quest_tree_denotation_natural_description
}
