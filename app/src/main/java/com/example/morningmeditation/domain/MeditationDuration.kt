package com.example.morningmeditation.domain

enum class MeditationDuration(val minutes: Int) {
    ONE_MINUTE(1),
    FIVE_MINUTES(5),
    TEN_MINUTES(10),
    FIFTEEN_MINUTES(15),
    TWENTY_MINUTES(20);

    val millis: Long get() = minutes * 60_000L
}
