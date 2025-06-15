package com.javier.mappster.utils

fun normalizeSpellName(name: String): String {
    return name
        .trim() // Eliminar espacios iniciales y finales
        .lowercase() // Convertir a minúsculas
        .replace("[^a-z0-9]+".toRegex(), "-") // Reemplazar caracteres no alfanuméricos por guiones
        .trim('-') // Eliminar guiones iniciales o finales
}

val sourceMap = mapOf(
    "AAG" to "Astral Adventurer's Guide",
    "AI" to "Aquisitions Incorporated",
    "AitFR-AVT" to "A Verdant Tomb",
    "AitFR-FCD" to "A Verdant Tomb",
    "BAM" to "Boo's Astral Managerie",
    "BGDIA" to "Baldur's Gate: Descent into Avernus",
    "BGG" to "Bigby Presents: Glory of the Giants",
    "BMT" to "The Book of Many Things",
    "CM" to "Candlekeep Mysteries",
    "CoS" to "Curse of Strahd",
    "DC" to "Divine Contention",
    "DitLCoT" to "Descent into the Lost Caverns of Tsojcanth",
    "DIP" to "Dragon of Icespire Peak",
    "DMG" to "Dungeon Master's Guide (2014)",
    "DODK" to "Dungeons of Drakkenheim",
    "DoSi" to "Dragons of Stormwreck Island",
    "DSotDQ" to "Dragonlance: Shadow of the Dragon Queen",
    "EGW" to "Explorer's Guide to Wildemount",
    "ERLW" to "Eberron: Rising from the Last War",
    "ESK" to "Essentials Kit",
    "FTD" to "Fizban's Treasury of Dragons",
    "GGR" to "Guildmasters' Guide to Ravnica",
    "GHLoE" to "Grim Hollow",
    "GoS" to "Ghosts of Saltmarsh",
    "HoL" to "House of Lament",
    "HotDQ" to "Hoard of the Dragon Queen",
    "IDRotF" to "Icewind Dale: Rime of the Frostmaiden",
    "JttRC" to "Journey through the Radiant Citadel",
    "KKW" to "Krenko's Way",
    "KftGV" to "Keys from the Golden Vault",
    "LLK" to "Lost Laboratory of Kwalish",
    "LMoP" to "Lost Mine of Phandelver",
    "LoX" to "Light of Xaryxis",
    "MM" to "Monsters Manual (2014)",
    "MOT" to "Mythic Odysseys of Theros",
    "MPP" to "Morte's Planar Parade",
    "MPMM" to "Mordenkainen's Monsters of the Multiverse",
    "MTF" to "Mordekainen's Tome of Foes",
    "OotA" to "Out of the Abyss",
    "OoW" to "The Orrery of the Wanderer",
    "PaBTSO" to "Phandelver and Below: The Shattered Obelisk",
    "PHB" to "Player's Handbook",
    "PotA" to "Princes of the Apocalypse",
    "QftIS" to "Quests from the Infinite Staircase",
    "RoT" to "Rise of Tiamat",
    "SaTO" to "Sigil and the Outlands",
    "SCAG" to "Sword Coast Adventurer's Guide",
    "SCC" to "Strixhaven: Curriculum of Chaos",
    "SDW" to "Sleeping Dragon's Wake",
    "SKT" to "Storm King's Thunder",
    "TCE" to "Tasha's Cauldron of Everything",
    "TDCSR" to "Tal'Dorei Campaign Setting",
    "TalPsi" to "The Talent and Psionics",
    "TftYP" to "Tales from the Yawning Portal",
    "ToA" to "Tomb of Annihilation",
    "ToFW" to "Turn of Fortune`s Wheel",
    "VEoR" to "Vecna: Eve of Ruin",
    "VGM" to "Volo's Guide to Monsters",
    "VRGR" to "Van Richten's Guide to Ravenloft",
    "WBtW" to "Wild beyond the Witchlight",
    "WDH" to "Waterdeep: Dragon Heist",
    "WDMM" to "Waterdeep: Dungeon of the Mad Mage",
    "XDMG" to "Dungeon Master's Guide (2025)",
    "XGE" to "Xanathar's Guide to Everything",
    "XMM" to "Monsters Manual (2025)",
    "XPHB" to "Player's Handbook (2025)"
)

val conditionDescriptions = mapOf(
    "charmed" to "A charmed creature can't attack the charmer or target the charmer with harmful abilities or magical effects.\nThe charmer has advantage on any ability check to interact socially with the creature",
    "unconscious" to "An unconscious creature is incapacitated, can't move or speak, and is unaware of its surroundings.\nThe creature drops whatever it's holding and falls prone.\nThe creature automatically fails Strength and Dexterity saving throws.\nAttack rolls against the creature have advantage.\nAny attack that hits the creature is a critical hit if the attacker is within 5 feet of the creature.",
    "frightened" to "A frightened creature has disadvantage on ability checks and attack rolls while the source of its fear is within line of sight.\nThe creature can't willingly move closer to the source of its fear.",
    "restrained" to "A restrained creature's speed becomes 0, and it can't benefit from any bonus to its speed.\nAttack rolls against the creature have advantage, and the creature's attack rolls have disadvantage.\nThe creature has disadvantage on Dexterity saving throws.",
    "petrified" to "A petrified creature is transformed, along with any nonmagical object it is wearing or carrying, into a solid inanimate substance (usually stone). Its weight increases by a factor of ten, and it ceases aging.\nThe creature is incapacitated, can't move or speak, and is unaware of its surroundings.\nAttack rolls against the creature have advantage.\nThe creature automatically fails Strength and Dexterity saving throws.\nThe creature has resistance to all damage.\nThe creature is immune to poison and disease, although a poison or disease already in its system is suspended, not neutralized.",
    "blinded" to "A blinded creature can't see and automatically fails any ability check that requires sight.\nAttack rolls against the creature have advantage, and the creature's attack rolls have disadvantage.",
    "deafened" to "A deafened creature can't hear and automatically fails any ability check that requires hearing.",
    "poisoned" to "A poisoned creature has disadvantage on attack rolls and ability checks",
    "paralyzed" to "A paralyzed creature is incapacitated and can't move or speak.\nThe creature automatically fails Strength and Dexterity saving throws.\nAttack rolls against the creature have advantage.\nAny attack that hits the creature is a critical hit if the attacker is within 5 feet of the creature.",
    "stunned" to "A stunned creature is incapacitated, can't move, and can speak only falteringly.\nThe creature automatically fails Strength and Dexterity saving throws.\nAttack rolls against the creature have advantage.",
    "incapacitated" to "An incapacitated creature can't take actions or reactions.",
    "invisible" to "An invisible creature is impossible to see without the aid of magic or a special sense. For the purpose of hiding, the creature is heavily obscured. The creature's location can be detected by any noise it makes or any tracks it leaves.\nAttack rolls against the creature have disadvantage, and the creature's attack rolls have advantage.",
    "prone" to "A prone creature's only movement option is to crawl, unless it stands up and thereby ends the condition.\nThe creature has disadvantage on attack rolls.\nAn attack roll against the creature has advantage if the attacker is within 5 feet of the creature. Otherwise, the attack roll has disadvantage.",
    "grappled" to "A grappled creature's speed becomes 0, and it can't benefit from any bonus to its speed.\nThe condition ends if the grappler is incapacitated.\nThe condition also ends if an effect removes the grappled creature from the reach of the grappler or grappling effect, such as when a creature is hurled away by the thunderwave spell.",
    "exhaustion" to "Some special abilities and environmental hazards, such as starvation and the long-term effects of freezing or scorching temperatures, can lead to a special condition called exhaustion. Exhaustion is measured in six levels. An effect can give a creature one or more levels of exhaustion, as specified in the effect's description.\n1: Disadvantage on ability checks\n2: Speed halved\n3: Disadvantage on attack rolls and saving throws\n4: Hit point maximum halved\n5: Speed reduced to 0\n6: Death\nIf an already exhausted creature suffers another effect that causes exhaustion, its current level of exhaustion increases by the amount specified in the effect's description.\nA creature suffers the effect of its current level of exhaustion as well as all lower levels. For example, a creature suffering level 2 exhaustion has its speed halved and has disadvantage on ability checks.\nAn effect that removes exhaustion reduces its level as specified in the effect's description, with all exhaustion effects ending if a creature's exhaustion level is reduced below 1.\nFinishing a long rest reduces a creature's exhaustion level by 1, provided that the creature has also ingested some food and drink. Also, being raised from the dead reduces a creature's exhaustion level by 1."
)

val quickRefDescriptions = mapOf(
    "half cover" to "A target with half cover has a +2 bonus to AC and Dexterity saving throws. A target has half cover if an obstacle blocks at least half of its body. The obstacle might be a low wall, a large piece of furniture, a narrow tree trunk, or a creature, whether that creature is an enemy or a friend.",
    "three-quarters cover" to "A target with three-quarters cover has a +5 bonus to AC and Dexterity saving throws. A target has three-quarters cover if about three-quarters of it is covered by an obstacle. The obstacle might be a portcullis, an arrow slit, or a thick tree trunk.",
    "difficult terrain" to "Every foot of movement in difficult terrain costs 1 extra foot. This rule is true even if multiple things in a space count as difficult terrain.",
    "heavily obscured" to "You have the Blinded condition while trying to see something in a Heavily Obscured space.",
    "total cover" to "A target with total cover can't be targeted directly by an attack or a spell, although some spells can reach such a target by including it in an area of effect. A target has total cover if it is completely concealed by an obstacle."
)

val PATTERN = Regex(
    "\\{@damage\\s*(\\d+d\\d+\\s*\\+\\s*\\d+|\\d+d\\d+)\\}|" +
            "\\{@dice\\s*(\\d*d\\d+)\\}|" +
            "\\{@scaledice\\s*(\\d+d\\d+)\\|(\\d+-\\d+)\\|(\\d+d\\d+)\\}|" +
            "\\{@scaledamage\\s*(\\d+d\\d+)\\|(\\d+-\\d+)\\|(\\d+d\\d+)\\}|" +
            "\\{\\condition\\s*(charmed|unconscious|frightened|restrained|petrified|blinded|deafened|poisoned|paralyzed|stunned|incapacitated|invisible|prone|grappled|exhaustion|deafened\\|\\|deaf|blinded\\|\\|blind)\\}|" +
            "\\{@spell\\s*([^\\}]+)\\}|" +
            "\\{@chance\\s*(\\d+)\\s*\\|\\|\\|\\s*([^\\|]+)\\|([^\\}]+)\\}|" +
            "\\{@quickref\\s+(Cover\\|\\|3\\|\\|(half cover|three-quarters cover|total cover))\\}|" +
            "\\{@quickref\\s+(Vision and Light\\|PHB\\|2\\|\\|heavily obscured)\\}|" +
            "\\{@quickref\\s+(difficult terrain\\|\\|3)\\}|" +
            "\\{@quickref\\s*([^\\|\\}]+?)(?:\\|\\|(\\d+)?(?:\\|\\|([^\\}]+))?)?\\s*\\}|" +
            "\\{@quickref\\s*[^\\}]+\\|PHB\\|\\d+\\|\\d*\\|([^\\}]+)\\}|" +
            "\\{@skill\\s*([^\\}]+)\\}|" +
            "\\{@d20\\s*(-?\\d+)\\}"
)