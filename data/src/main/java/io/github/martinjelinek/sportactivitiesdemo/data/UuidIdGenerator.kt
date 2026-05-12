package io.github.martinjelinek.sportactivitiesdemo.data

import io.github.martinjelinek.sportactivitiesdemo.domain.IdGenerator
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UuidIdGenerator @Inject constructor() : IdGenerator {
    override fun next(): String = UUID.randomUUID().toString()
}
