package ru.ingins.gradle.rpm.builder

import org.eclipse.packager.rpm.build.PayloadEntryType
import org.gradle.api.model.ObjectFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

open class When @Inject constructor(objectFactory: ObjectFactory) {

    private val logger: Logger = LoggerFactory.getLogger(When::class.java)

    var type = objectFactory.property(String::class.java)
    var prefix = objectFactory.property(String::class.java)
    var suffix = objectFactory.property(String::class.java)

    fun matches(any: Any, type: PayloadEntryType, targetName: String): Boolean {
        if (this.prefix.isPresent && this.prefix.get().isNotEmpty() && !targetName.startsWith(this.prefix.get())) {
            logger.debug("Prefix is set and does not match - expected: '{}', provided: '{}'", this.prefix.get(), targetName)
            return false
        }

        if (this.suffix.isPresent && this.suffix.get().isNotEmpty() && !targetName.endsWith(this.suffix.get())) {
            logger.debug("Suffix is set and does not match - expected: '{}', provided: '{}'", this.suffix.get(), targetName);
            return false
        }

        if (this.type.isPresent && this.type.get().isNotEmpty()) {
            logger.debug("Testing type - expected: {}, actual: {}", this.type.get(), type)
            when (this.type.get().toLowerCase()) {
                "directory" -> if (type != PayloadEntryType.DIRECTORY) {
                    return false
                }
                "file" -> if (type != PayloadEntryType.FILE) {
                    return false
                }
                "link" -> if (type != PayloadEntryType.SYMBOLIC_LINK) {
                    return false
                }
                else -> throw IllegalStateException(String.format("Unknown match type: '%s'", this.type.get()))
            }
        }

        logger.debug("Is a match")
        return true
    }
}