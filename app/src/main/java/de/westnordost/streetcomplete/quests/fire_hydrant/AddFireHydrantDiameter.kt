package de.westnordost.streetcomplete.quests.fire_hydrant


import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.ktx.containsAnyKey
import de.westnordost.streetcomplete.quests.getNameOrBrandOrOperatorOrRef

class AddFireHydrantDiameter : OsmFilterQuestType<FireHydrantDiameterAnswer>() {

    override val elementFilter = """
        nodes with
         emergency = fire_hydrant
         and fire_hydrant:type and
         (fire_hydrant:type = pillar or fire_hydrant:type = underground)
         and !fire_hydrant:diameter
         and fire_hydrant:diameter:signed != no
    """
    override val commitMessage = "Add fire hydrant diameter"
    override val wikiLink = "Tag:emergency=fire_hydrant"
    override val icon = R.drawable.ic_quest_fire_hydrant_diameter
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = emptyList<QuestTypeAchievement>()

    override val enabledInCountries = NoCountriesExcept("DE","BE","GB","PL","IE","FI","NL")

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> =
        arrayOfNotNull(getNameOrBrandOrOperatorOrRef(tags))

    override fun getTitle(tags: Map<String, String>):  Int {
        val hasRef = tags.containsAnyKey("ref")
        return when {
            hasRef -> R.string.quest_fireHydrant_diameter_ref_title
            else   -> R.string.quest_fireHydrant_diameter_title
        }
    }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with emergency = fire_hydrant")

    override fun createForm() = AddFireHydrantDiameterForm()

    override fun applyAnswerTo(answer: FireHydrantDiameterAnswer, changes: StringMapChangesBuilder) {

        when (answer) {
            is FireHydrantDiameter ->       changes.add("fire_hydrant:diameter", answer.diameter.toString())
            is NoFireHydrantDiameterSign -> changes.add("fire_hydrant:diameter:signed", "no")
        }
    }
}
