package de.westnordost.streetcomplete.quests.tree

import android.content.res.AssetManager
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import de.westnordost.streetcomplete.util.logs.Log
import java.io.File

// todo: multiple languages!
class TreeMap(private val assetManager: AssetManager, private val language: String) {
    private val yaml = Yaml(configuration = YamlConfiguration(
        strictMode = false, // ignore unknown properties
    )
    )

    private val treeMap = HashMap<String, TreeInfos>()

    fun getName(tags: Map<String, String>): String? {
        // first species
        // shortcut: if tagged in local language, use it
        val speciesWikidata = tags["species:wikidata"]
        if (speciesWikidata != null) {
            val species = treeData(language).getSpeciesName(speciesWikidata) ?: treeData("default").getSpeciesName(speciesWikidata)
            if (species != null) return species
        }
        val species = tags["species"]
        if (species != null) {
            val wikidata = treeData("default").getSpeciesWikidata(species)
            if (wikidata != null)
                treeData(language).getSpeciesName(wikidata)?.let { return it }
            return species // todo: later not, preferably check other species tags first
        }

        // then same thing for genus
        val genusWikidata = tags["genus:wikidata"]
        if (genusWikidata != null) {
            val genus = treeData(language).getGenusName(genusWikidata) ?: treeData("default").getGenusName(genusWikidata)
            if (genus != null) return genus
        }
        val genus = tags["genus"]
        if (genus != null) {
            val wikidata = treeData("default").getGenusWikidata(genus)
            if (wikidata != null)
                treeData(language).getGenusName(wikidata)?.let { return it }
            return genus // todo: later not, preferably check other species tags first
        }

        // todo: taxon?

        return null
    }

    private fun treeData(languageCode: String): TreeInfos {
        if (!treeMap.containsKey(languageCode)) {
            val info = TreeInfos(
                loadTreeData(languageCode, "genus"),
                loadTreeData(languageCode, "species"),
            )
            treeMap[languageCode] = info
        }
        return treeMap[languageCode]!!
    }

    private fun loadTreeData(languageCode: String, type: String): Map<String, String> {
        val filename = "$languageCode.yml"
        val folder = "tree_taxons" + File.separator + type
        Log.i("test", "loading ${folder + File.separator + filename}")
        if (assetManager.list(folder)?.contains(filename) != true) return emptyMap()
        assetManager.open(folder + File.separator + filename).use { inputStream ->
            return yaml.decodeFromStream(inputStream)
        }
    }
}

private class TreeInfos(
    private val genus: Map<String, String>,
    private val species: Map<String, String>
) {
    private val reverseGenus = genus.entries.associateTo(HashMap()) { it.value.lowercase() to it.key }
    private val reverseSpecies = species.entries.associateTo(HashMap()) { it.value.lowercase() to it.key }
    init {
//        require(genus.size == reverseGenus.size)
//        require(species.size == reverseSpecies.size)
        Log.i("test", "${genus.size} genus, ${species.size} species (${reverseSpecies.size} reverse)")
        // todo: go through species and do things like duplicating entries so x and × are found
    }
    fun getSpeciesWikidata(name: String) = reverseSpecies[name.lowercase()]
    fun getSpeciesName(wikidata: String) = species[wikidata]
    fun getGenusWikidata(name: String) = reverseGenus[name.lowercase()]
    fun getGenusName(wikidata: String) = genus[wikidata]
}
