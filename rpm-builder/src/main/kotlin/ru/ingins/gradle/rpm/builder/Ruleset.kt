package ru.ingins.gradle.rpm.builder

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class Ruleset @Inject constructor(objectFactory: ObjectFactory) {
    var id = objectFactory.property(String::class.java)
    var rules = objectFactory.listProperty(Rule::class.java)
    var defaultRuleset = objectFactory.property(String::class.java)

    fun validate() {
        this.rules.get().forEach { rule ->
            rule.validate()
        }
    }
}