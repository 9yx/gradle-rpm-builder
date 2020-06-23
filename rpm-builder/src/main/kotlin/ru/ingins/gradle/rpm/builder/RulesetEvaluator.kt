package ru.ingins.gradle.rpm.builder

import org.eclipse.packager.rpm.build.FileInformation
import org.eclipse.packager.rpm.build.PayloadEntryType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class RulesetEvaluator constructor(parent: RulesetEvaluator?, rulesets: Collection<Ruleset>) {

    private val logger: Logger = LoggerFactory.getLogger(RulesetEvaluator::class.java)

    private inner class RulesetInstance(val id: String, val rules: List<Rule>, val parentRuleset: String?) {
        private fun eval(any: Any, type: PayloadEntryType, targetName: String, info: FileInformation, knownSets: MutableSet<String>) {

            if (knownSets.contains(this.id)) {
                throw IllegalStateException(String.format("Recursive calling of rulesets is not allowed- current: %s, previous: %s",
                        this.id, knownSets.joinToString(", ")))
            }

            knownSets.add(this.id)

            this.rules.forEach { rule ->
                this@RulesetEvaluator.logger.debug("Testing rule {}", rule)
                if (rule.matches(any, type, targetName)) {
                    logger.debug("Rule matches. Applying information ...")
                    if (rule.apply(info)) {
                        logger.debug("Information: {}", info)
                    }
                    if (rule.last.get()) {
                        logger.debug("Last rule")
                        return
                    }
                }
            }

            if (!parentRuleset.isNullOrEmpty()) {
                logger.debug("Running parent ruleset: '{}'", parentRuleset)
                this@RulesetEvaluator.eval(parentRuleset, any, type, targetName, info)
            }
        }

        fun eval(any: Any, type: PayloadEntryType, targetName: String, info: FileInformation) {
            eval(any, type, targetName, info, LinkedHashSet())
        }

    }

    private val rulesets: MutableMap<String, RulesetInstance> = LinkedHashMap<String, RulesetInstance>()

    init {
        rulesets.forEach { set ->
            this.rulesets[set.id.get()] = this.convert(set)
        }
    }

    constructor(rulesets: Collection<Ruleset>) : this(null, rulesets)

    private fun convert(set: Ruleset): RulesetInstance {
        set.validate()
        return RulesetInstance(set.id.get(), set.rules.get(), set.defaultRuleset.orNull)
    }

    fun eval(ruleId: String, any: Any, type: PayloadEntryType, targetName: String, info: FileInformation) {
        val ruleset = rulesets[ruleId]
                ?: throw IllegalStateException(String.format("Unknown rule: '%s'", ruleId))
        ruleset.eval(any, type, targetName, info)
    }
}