package ru.ingins.gradle.rpm.builder

import org.eclipse.packager.rpm.Architecture
import org.eclipse.packager.rpm.OperatingSystem
import org.eclipse.packager.rpm.build.RpmBuilder
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class RpmExtension @Inject constructor(objectFactory: ObjectFactory, projectLayout: ProjectLayout) {
    val version: Property<String> = objectFactory.property()
    val packageName: Property<String> = objectFactory.property(String::class.java)
    val url: Property<String> = objectFactory.property()
    val architecture: Property<String> = objectFactory.property(String::class.java).convention("noarch")
    val operatingSystem: Property<String> = objectFactory.property(String::class.java).convention("linux")
    val leadOverrideArchitecture: Property<Architecture> = objectFactory.property()
    val leadOverrideOperatingSystem: Property<OperatingSystem> = objectFactory.property()
    val sourcePackage: Property<String> = objectFactory.property()
    val generateDefaultSourcePackage: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(true)
    val snapshotReleasePrefix: Property<String> = objectFactory.property(String::class.java).convention("0.")
    val snapshotBuildId: Property<String> = objectFactory.property()
    val release: Property<String> = objectFactory.property(String::class.java).convention("1")
    val forceRelease: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(false)
    val classifier: Property<String> = objectFactory.property(String::class.java).convention("rpm")
    val attach: Property<Boolean> = objectFactory.property()
    val epoch: Property<Int> = objectFactory.property()
    val summary: Property<String> = objectFactory.property()
    val description: Property<String> = objectFactory.property()
    val group: Property<String> = objectFactory.property()
    val distribution: Property<String> = objectFactory.property()
    val evalHostname: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(true)
    val license: Property<String> = objectFactory.property()
    val vendor: Property<String> = objectFactory.property()
    val packager: Property<String> = objectFactory.property()
    val prefixes: ListProperty<String> = objectFactory.listProperty()
    val entries: ListProperty<PackageEntry> = objectFactory.listProperty()
    val rulesets: ListProperty<Ruleset> = objectFactory.listProperty()
    val defaultRuleset: Property<String> = objectFactory.property()
    val beforeInstallation: Property<Script> = objectFactory.property()
    val afterInstallation: Property<Script> = objectFactory.property()
    val beforeRemoval: Property<Script> = objectFactory.property()
    val afterRemoval: Property<Script> = objectFactory.property()
    val beforeTransaction: Property<Script> = objectFactory.property()
    val afterTransaction: Property<Script> = objectFactory.property()
    val defaultScriptInterpreter: Property<String> = objectFactory.property(String::class.java).convention("/bin/sh")
    val requires: ListProperty<Dependency> = objectFactory.listProperty(Dependency::class.java)
            .convention(emptyList<Dependency>())
    val provides: ListProperty<SimpleDependency> = objectFactory.listProperty(SimpleDependency::class.java)
            .convention(emptyList<SimpleDependency>())
    val conflicts: ListProperty<SimpleDependency> = objectFactory.listProperty(SimpleDependency::class.java)
            .convention(emptyList<SimpleDependency>())
    val obsoletes: ListProperty<SimpleDependency> = objectFactory.listProperty(SimpleDependency::class.java)
            .convention(emptyList<SimpleDependency>())
    val prerequisites: ListProperty<SimpleDependency> = objectFactory.listProperty(SimpleDependency::class.java)
            .convention(emptyList<SimpleDependency>())
    val suggests: ListProperty<SimpleDependency> = objectFactory.listProperty(SimpleDependency::class.java)
            .convention(emptyList<SimpleDependency>())
    val enhances: ListProperty<SimpleDependency> = objectFactory.listProperty(SimpleDependency::class.java)
            .convention(emptyList<SimpleDependency>())
    val supplements: ListProperty<SimpleDependency> = objectFactory.listProperty(SimpleDependency::class.java)
            .convention(emptyList<SimpleDependency>())
    val recommends: ListProperty<SimpleDependency> = objectFactory.listProperty(SimpleDependency::class.java)
            .convention(emptyList<SimpleDependency>())
    val signature: Property<Signature> = objectFactory.property()
    val skip: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(false)
    val skipSigning: Property<Boolean> = objectFactory.property(Boolean::class.java).convention(false)
    val naming: Property<Naming> = objectFactory.property()
    val targetDir: DirectoryProperty = objectFactory.directoryProperty().convention(projectLayout.buildDirectory)
    val outputFileName: Property<String> = objectFactory.property()
    val maximumSupportedRpmVersion: Property<RpmBuilder.Version> = objectFactory.property()
    val signatureConfiguration: Property<String> = objectFactory.property()
}