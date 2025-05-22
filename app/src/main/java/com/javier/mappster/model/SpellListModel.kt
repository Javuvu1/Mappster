package com.javier.mappster.model

import com.google.firebase.firestore.DocumentId

data class SpellList(
    @DocumentId val id: String = "",
    val name: String = "",
    val userId: String = "",
    val spellIds: List<String> = emptyList()
)