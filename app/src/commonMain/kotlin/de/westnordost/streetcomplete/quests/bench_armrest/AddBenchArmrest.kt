package de.westnordost.streetcomplete.quests.bench_armrest

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.bench_armrest.BenchArmrestAnswer.NO
import de.westnordost.streetcomplete.quests.bench_armrest.BenchArmrestAnswer.PICNIC_TABLE
import de.westnordost.streetcomplete.quests.bench_armrest.BenchArmrestAnswer.YES
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_ee
import de.westnordost.streetcomplete.resources.ic_quest_bench_armrest
import de.westnordost.streetcomplete.resources.quest_bench_answer_picnic_table
import de.westnordost.streetcomplete.resources.quest_bench_armrest_title
import de.westnordost.streetcomplete.resources.quest_generic_hasFeature_no
import de.westnordost.streetcomplete.resources.quest_generic_hasFeature_yes
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddBenchArmrest : OsmFilterQuestType<BenchArmrestAnswer>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bench
          and (!area or area = no)
          and !armrest
          and !bench:type
          and (!seasonal or seasonal = no)
          and access !~ private|no
    """
    override val changesetComment = "Survey whether benches have armrests"
    override val wikiLink = "Tag:amenity=bench"
    override val icon = Res.drawable.ic_quest_bench_armrest
    override val title = Res.string.quest_bench_armrest_title
    override val defaultDisabledMessage = Res.string.default_disabled_msg_ee
    override val achievements = listOf(PEDESTRIAN, OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = bench or leisure = picnic_table or amenity = lounger")

    @Composable
    override fun Form(on: (QuestAction<BenchArmrestAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            on = on,
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(
                    NO
                )) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(
                    YES
                )) }
            ),
            otherAnswers = { listOf(
                AnswerItem(stringResource(Res.string.quest_bench_answer_picnic_table)) { on(Answer(
                    PICNIC_TABLE
                )) }
            ) },
        )
    }

    override fun applyAnswerTo(answer: BenchArmrestAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            PICNIC_TABLE -> {
                tags["leisure"] = "picnic_table"
                tags.remove("amenity")
            }
            YES -> tags["armrest"] = "yes"
            NO -> tags["armrest"] = "no"
        }
    }
}
