package de.westnordost.streetcomplete.quests.lgbtq

import de.westnordost.streetcomplete.data.quest.AllCountriesExcept

// countries with a LGBT propaganda law, and not in the list of country
// where homosexuality is illegal
val LGBTPropagandaCountries =
    listOf(
	// Belarus, see https://www.reuters.com/world/europe/belarus-prepares-law-against-lgbt-propaganda-state-media-says-2024-02-19/
	// "BY",
	// Georgia, see https://oc-media.org/georgian-dream-announces-draft-law-against-pseudo-liberal-ideology/
	// "GE",
	// Kyrgyzstan, see https://en.wikipedia.org/wiki/Kyrgyz_anti-LGBT_propaganda_law
	"KY",
	// Russia, since 2013 and worst since 2023
	"RU",
)

// countries where homosexuality is illegal
// even if unenforced or just for male
val LGBTHomosexualityIllegalCountries =
    listOf(
        // United Arab Emirates
        "AE",
        // Afghanistan
        "AF",
        // Bangladesh
        "BD",
        // Burundi
        "BI",
        // Brunei Darussalam
        "BN",
        // Cameroon
        "CM",
        // Dominica
        "DM",
        // Algeria
        "DZ",
        // Egypt
        "EG",
        // Eritrea
        "ER",
        // Ethiopia
        "ET",
        // Grenada
        "GD",
        // Ghana
        "GH",
        // Gambia
        "GM",
        // Guinea
        "GN",
        // Guyana
        "GY",
        // Indonesia
        // not illegal, except in one province where it is enforced
        "ID",
        // Iraq
        "IQ",
        // Iran
        "IR",
        // Jamaica
        "JM",
        // Kenya
        "KE",
        // Kiribati, just for male, but not enforced
        "KI",
        // Comoros
        "KM",
        // Kuwait, just for male
        "KW",
        // Lebanon, unsure in 2024, cf wikipedia
        "LB",
        // Sri Lanka
        "LK",
        // Liberia
        "LR",
        // Libya
        "LY",
        // Morocco
        "MA",
        // Myanmar
        "MM",
        // Mauritania
        "MR",
        // Maldives, not enforced
        "MV",
        // Malawi
        "MW",
        // Malaysia
        "MY",
        // Namibia, not enforced
        "NA",
        // Nigeria
        "NG",
        // Niue
        "NU",
        // Oman
        "OM",
        // Papua New Guinea , illegal for male only
        "PG",
        // Pakistan
        "PK",
        // Qatar
        "QA",
        // Saudi Arabia
        "SA",
        // Solomon Islands
        "SB",
        // Sudan
        "SD",
        // Sierra Leone, illegal for male only
        "SL",
        // Senegal
        "SN",
        // Somalia
        "SO",
        // South Sudan
        "SS",
        // Eswatini
        "SZ",
        // Chad
        "TD",
        // Togo
        "TG",
        // Turkmenistan, illegal for male only
        "TM",
        // Tunisia
        "TN",
        // Tonga, illegal for male only
        "TO",
        // Tuvalu,illegal for male only, not enforced
        "TV",
        // Tanzania
        "TZ",
        // Uganda
        "UG",
        // Uzbekistan, illegal for male only
        "UZ",
        // Saint Lucia
        "WL",
        // Samoa, illegal for male only, not enforced
        "WS",
        // Saint Vincent and the Grenadines
        "WV",
        // Yemen
        "YE",
        // Zambia
        "ZM",
        // Zimbabwe
        "ZW"
    )

// country where mapping would be fruitless and/or dangerous
val LGBTExcludedCountries = AllCountriesExcept(
	listOf(
		LGBTHomosexualityIllegalCountries,
		LGBTPropagandaCountries
	).flatten()
)
