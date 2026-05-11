package io.github.martinjelinek.sportactivitiesdemo.domain

fun interface IdGenerator {
    fun next(): String
}
