package com.example.morningmeditation.domain

enum class Soundscape(val label: String) {
    NONE("소리 없음"),
    FOREST("숲"),
    WAVES("파도"),
    RAIN("비"),
    SINGING_BOWL("싱잉볼");

    companion object {
        fun fromStorage(value: String?): Soundscape =
            entries.firstOrNull { it.name == value } ?: FOREST
    }
}
