package de.westnordost.streetcomplete.quests.bench_material

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.ic_quest_bench_material
import de.westnordost.streetcomplete.resources.quest_benchMaterial_title
import de.westnordost.streetcomplete.resources.quest_bench_answer_picnic_table
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBenchMaterial : OsmFilterQuestType<BenchMaterial>() {

    override val elementFilter = """
        nodes, ways with
          (amenity = bench or leisure = picnic_table or amenity = lounger)
          and (!area or area = no)
          and !material
          and access !~ private|no
    """
    override val changesetComment = "Add material information to benches"
    override val wikiLink = "Tag:amenity=bench"
    override val icon = Res.drawable.ic_quest_bench_material
    override val title = Res.string.quest_benchMaterial_title
    override val achievements = listOf(EditTypeAchievement.PEDESTRIAN, EditTypeAchievement.OUTDOORS)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = bench or leisure = picnic_table or amenity = lounger")

    @Composable
    override fun Form(on: (QuestAction<BenchMaterial>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = BenchMaterial.entries.filterNot { it == BenchMaterial.PICNIC },
            itemContent = { ImageWithLabel(painterResource(it.icon!!), stringResource(it.title!!)) },
            favoriteKey = "AddBenchMaterialForm",
            otherAnswers = {
                if (element.tags["amenity"] == "bench")
                    listOf(AnswerItem(stringResource(Res.string.quest_bench_answer_picnic_table)) { on(
                        Answer(BenchMaterial.PICNIC)
                    ) })
                else emptyList()
            }
        )
    }

    override fun applyAnswerTo(answer: BenchMaterial, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer == BenchMaterial.PICNIC) {
            tags.remove("amenity")
            tags["leisure"] = "picnic_table"
        } else
            tags["material"] = answer.osmValue
    }
}
