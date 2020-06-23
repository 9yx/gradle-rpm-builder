package ru.ingins.gradle.rpm.builder

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class RpmBuilderPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val rpmExtension = project.extensions.create("rpm", RpmExtension::class.java)

        project.afterEvaluate {
            tasks.register<RpmTask>("rpm") {
                version.set(rpmExtension.version.convention(project.version.toString()))
                packageName.set(rpmExtension.packageName.convention(project.name))
                url.set(rpmExtension.url)
                architecture.set(rpmExtension.architecture)
                operatingSystem.set(rpmExtension.operatingSystem)
                leadOverrideArchitecture.set(rpmExtension.leadOverrideArchitecture)
                leadOverrideOperatingSystem.set(rpmExtension.leadOverrideOperatingSystem)
                sourcePackage.set(rpmExtension.sourcePackage)
                generateDefaultSourcePackage.set(rpmExtension.generateDefaultSourcePackage)
                snapshotReleasePrefix.set(rpmExtension.snapshotReleasePrefix)
                snapshotBuildId.set(rpmExtension.snapshotBuildId)
                release.set(rpmExtension.release)
                forceRelease.set(rpmExtension.forceRelease)
                classifier.set(rpmExtension.classifier)
                attach.set(rpmExtension.attach)
                epoch.set(rpmExtension.epoch)
                summary.set(rpmExtension.summary.convention(project.name))

                if (rpmExtension.description.isPresent) {
                    description.set(rpmExtension.description)
                } else if (!project.description.isNullOrEmpty()) {
                    description.set(project.description)
                }

                group.set(rpmExtension.group)
                distribution.set(rpmExtension.distribution)
                evalHostname.set(rpmExtension.evalHostname)
                license.set(rpmExtension.license)
                vendor.set(rpmExtension.vendor)
                packager.set(rpmExtension.packager)
                prefixes.set(rpmExtension.prefixes)
                entries.set(rpmExtension.entries)
                rulesets.set(rpmExtension.rulesets)
                defaultRuleset.set(rpmExtension.defaultRuleset)
                beforeInstallation.set(rpmExtension.beforeInstallation)
                afterInstallation.set(rpmExtension.afterInstallation)
                beforeRemoval.set(rpmExtension.beforeRemoval)
                afterRemoval.set(rpmExtension.afterRemoval)
                beforeTransaction.set(rpmExtension.beforeTransaction)
                afterTransaction.set(rpmExtension.afterTransaction)
                defaultScriptInterpreter.set(rpmExtension.defaultScriptInterpreter)
                requires.set(rpmExtension.requires)
                provides.set(rpmExtension.provides)
                conflicts.set(rpmExtension.conflicts)
                obsoletes.set(rpmExtension.obsoletes)
                prerequisites.set(rpmExtension.prerequisites)
                suggests.set(rpmExtension.suggests)
                enhances.set(rpmExtension.enhances)
                supplements.set(rpmExtension.supplements)
                recommends.set(rpmExtension.recommends)
                signature.set(rpmExtension.signature)
                skip.set(rpmExtension.skip)
                skipSigning.set(rpmExtension.skipSigning)
                naming.set(rpmExtension.naming)
                targetDir.set(rpmExtension.targetDir)
                outputFileName.set(rpmExtension.outputFileName)
                maximumSupportedRpmVersion.set(rpmExtension.maximumSupportedRpmVersion)
                signatureConfiguration.set(rpmExtension.signatureConfiguration)
            }
        }
    }
}
