package ru.ingins.gradle.rpm.builder

import org.eclipse.packager.rpm.build.PayloadEntryType
import org.gradle.api.model.ObjectFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.String.format
import javax.inject.Inject

open class Rule @Inject constructor(objectFactory: ObjectFactory) : EntryDetails(objectFactory) {

    private val logger: Logger = LoggerFactory.getLogger(Rule::class.java)

    var `when` = objectFactory.property(When::class.java)
    var last = objectFactory.property(Boolean::class.java).convention(false)

    fun matches(any: Any, type: PayloadEntryType, targetName: String): Boolean {
        return if (!this.`when`.isPresent) {
            logger.debug("No 'when'")
            true
        } else {
            logger.debug("Matching 'when': {}", this.`when`.get())
            this.`when`.get().matches(any, type, targetName)
        }
    }

    override fun toString(): String {
        return if (`when`.isPresent) {
            format("[Rule - when: %s, then: %s]", `when`.get(), super.toString())
        } else {
            format("[Rule - when: %s, then: %s]", null, super.toString())
        }
    }
}