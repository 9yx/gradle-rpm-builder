package ru.ingins.gradle.rpm.builder

import org.eclipse.packager.rpm.build.BuilderContext
import org.eclipse.packager.rpm.build.FileInformation
import org.eclipse.packager.rpm.build.FileInformationProvider
import org.eclipse.packager.rpm.build.PayloadEntryType
import java.util.function.Consumer

class GradleFileInformationProvider(rulesetEval: RulesetEvaluator?, ruleId: String?, entry: PackageEntry?, logger: Consumer<String>?) : FileInformationProvider<Any> {
    private val rulesetEval: RulesetEvaluator = checkNotNull(rulesetEval)
    private val logger: Consumer<String> = logger ?: Consumer { }
    private val ruleId: String? = ruleId
    private val entry: PackageEntry? = entry

    override fun provide(targetName: String, `object`: Any, type: PayloadEntryType): FileInformation {
        val result = provideByRule(targetName, `object`, type)
                ?: throw IllegalStateException("Unable to provide file information")
        if (entry != null) {
            if (entry.apply(result)) {
                logger.accept(String.format("local override = %s", result))
            }
        }
        return result
    }

    private fun provideByRule(targetName: String, `object`: Any, type: PayloadEntryType): FileInformation {
        val result = BuilderContext.defaultProvider<Any>().provide(targetName, `object`, type)
        if (!ruleId.isNullOrEmpty()) {
            logger.accept(String.format("run ruleset: '%s'", ruleId))
            rulesetEval.eval(ruleId, `object`, type, targetName, result)
        }
        logger.accept(String.format("fileInformation = %s", result))
        return result
    }

}