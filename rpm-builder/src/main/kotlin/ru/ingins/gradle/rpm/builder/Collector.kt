package ru.ingins.gradle.rpm.builder

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

class Collector @Inject constructor(objectFactory: ObjectFactory) {
    var from = objectFactory.fileProperty()
    var isDirectories = objectFactory.property(Boolean::class.java).convention(true)
    var isSymbolicLinks = objectFactory.property(Boolean::class.java).convention(false)
    var includes = objectFactory.listProperty(String::class.java).convention(emptyList())
    var excludes = objectFactory.listProperty(String::class.java).convention(emptyList())

    override fun toString(): String {
        return "[collector - from: $from,  directories: $isDirectories, symLinks: $isSymbolicLinks, includes: $includes, excludes: $excludes]"
    }
}