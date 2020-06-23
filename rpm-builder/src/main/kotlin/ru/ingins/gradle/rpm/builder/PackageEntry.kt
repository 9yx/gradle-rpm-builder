package ru.ingins.gradle.rpm.builder

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class PackageEntry @Inject constructor(objectFactory: ObjectFactory) : EntryDetails(objectFactory) {
    var name = objectFactory.property(String::class.java)
    var directory = objectFactory.property(Boolean::class.java)
    var file = objectFactory.fileProperty()
    var collect = objectFactory.property(Collector::class.java)
    var linkTo = objectFactory.property(String::class.java)
    var ruleset = objectFactory.property(String::class.java)

    override fun validate() {
        check(name.isPresent && name.get().isNotEmpty()) { "'name' must not be empty" }
        var sources = 0
        sources += if (directory.isPresent) 1 else 0
        sources += if (file.isPresent) 1 else 0
        sources += if (collect.isPresent) 1 else 0
        sources += if (linkTo.isPresent) 1 else 0
        check(sources == 1) { "Exactly one of 'file', 'directory', 'linkTo' or 'collect' must be specified." }
        super.validate()
    }
}