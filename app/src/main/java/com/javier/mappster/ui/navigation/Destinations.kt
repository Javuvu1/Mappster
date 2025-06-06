package com.javier.mappster.navigation

object Destinations {
    const val LOGIN = "login"
    const val SPELL_LIST = "spell_list"
    const val SPELL_DETAIL = "spell_detail"
    const val CREATE_SPELL = "create_spell"
    const val EDIT_SPELL = "edit_spell/{spellName}"
    const val CUSTOM_SPELL_LISTS = "custom_spell_lists"
    const val SPELL_LIST_VIEW = "spell_list_view/{listId}"
    const val CREATE_SPELL_LIST = "create_spell_list"
    const val EDIT_SPELL_LIST = "create_spell_list/{id}/{name}/{spellIds}"
    const val MONSTER_LIST = "monster_list"
    const val MONSTER_DETAIL = "monster_detail/{name}/{source}"
    const val CUSTOM_MONSTER_LISTS = "custom_monster_lists"
    const val CREATE_MONSTER = "create_monster"
    const val CUSTOM_MONSTER_DETAIL = "custom_monster_detail/{monsterId}"
    const val INITIATIVE_TRACKER = "initiative_tracker"
}