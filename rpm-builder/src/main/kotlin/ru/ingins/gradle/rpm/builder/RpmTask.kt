package ru.ingins.gradle.rpm.builder

import org.codehaus.plexus.util.DirectoryScanner
import org.eclipse.packager.rpm.*
import org.eclipse.packager.rpm.build.BuilderContext
import org.eclipse.packager.rpm.build.RpmBuilder
import org.eclipse.packager.rpm.build.RpmFileNameProvider
import org.eclipse.packager.rpm.deps.RpmDependencyFlags
import org.eclipse.packager.rpm.header.Header
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import ru.ingins.gradle.rpm.builder.Naming.Case
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Path
import java.rmi.UnknownHostException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.properties.Delegates
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KFunction3

infix fun <F : (T1) -> Unit, T1> F.andThen(n: KFunction1<T1, Unit>): (T1) -> Unit = { this(it); n(it) }

@Suppress("UnstableApiUsage")
open class RpmTask : DefaultTask() {
    private val snapshotSuffix = "-SNAPSHOT"

    @Input
    val version: Property<String> = project.objects.property()

    @Input
    val packageName: Property<String> = project.objects.property(String::class.java).convention(project.name)

    @Input
    val architecture: Property<String> = project.objects.property()

    @Input
    @Optional
    val url: Property<String> = project.objects.property()

    @Input
    val operatingSystem: Property<String> = project.objects.property()

    @Input
    @Optional
    val leadOverrideArchitecture: Property<Architecture> = project.objects.property()

    @Input
    @Optional
    val leadOverrideOperatingSystem: Property<OperatingSystem> = project.objects.property()

    @Input
    @Optional
    val sourcePackage: Property<String> = project.objects.property()

    @Input
    val generateDefaultSourcePackage: Property<Boolean> = project.objects.property()

    @Input
    val snapshotReleasePrefix: Property<String> = project.objects.property()

    @Input
    @Optional
    val snapshotBuildId: Property<String> = project.objects.property()

    @Input
    val release: Property<String> = project.objects.property()

    @Input
    val forceRelease: Property<Boolean> = project.objects.property()

    @Input
    val classifier: Property<String> = project.objects.property()

    @Input
    val attach: Property<Boolean> = project.objects.property()

    @Input
    @Optional
    val epoch: Property<Int> = project.objects.property()

    @Input
    val summary: Property<String> = project.objects.property()

    @Input
    @Optional
    val description: Property<String> = project.objects.property()

    @Input
    val group: Property<String> = project.objects.property()

    @Input
    @Optional
    val distribution: Property<String> = project.objects.property()

    @Input
    val evalHostname: Property<Boolean> = project.objects.property()

    @Input
    @Optional
    val license: Property<String> = project.objects.property()

    @Input
    @Optional
    val vendor: Property<String> = project.objects.property()

    @Input
    @Optional
    val packager: Property<String> = project.objects.property()

    @Input
    val prefixes: ListProperty<String> = project.objects.listProperty()

    @Input
    val entries: ListProperty<PackageEntry> = project.objects.listProperty()

    @Input
    val rulesets: ListProperty<Ruleset> = project.objects.listProperty()

    @Input
    @Optional
    val defaultRuleset: Property<String> = project.objects.property()

    @Input
    @Optional
    val beforeInstallation: Property<Script> = project.objects.property()

    @Input
    @Optional
    val afterInstallation: Property<Script> = project.objects.property()

    @Input
    @Optional
    val beforeRemoval: Property<Script> = project.objects.property()

    @Input
    @Optional
    val afterRemoval: Property<Script> = project.objects.property()

    @Input
    @Optional
    val beforeTransaction: Property<Script> = project.objects.property()

    @Input
    @Optional
    val afterTransaction: Property<Script> = project.objects.property()

    @Input
    val defaultScriptInterpreter: Property<String> = project.objects.property()

    @Input
    val requires: ListProperty<Dependency> = project.objects.listProperty()

    @Input
    val provides: ListProperty<SimpleDependency> = project.objects.listProperty()

    @Input
    val conflicts: ListProperty<SimpleDependency> = project.objects.listProperty()

    @Input
    val obsoletes: ListProperty<SimpleDependency> = project.objects.listProperty()

    @Input
    val prerequisites: ListProperty<SimpleDependency> = project.objects.listProperty()

    @Input
    val suggests: ListProperty<SimpleDependency> = project.objects.listProperty()

    @Input
    val enhances: ListProperty<SimpleDependency> = project.objects.listProperty()

    @Input
    val supplements: ListProperty<SimpleDependency> = project.objects.listProperty()

    @Input
    val recommends: ListProperty<SimpleDependency> = project.objects.listProperty()

    @Input
    @Optional
    val signature: Property<Signature> = project.objects.property()

    @Input
    val skip: Property<Boolean> = project.objects.property()

    @Input
    val skipSigning: Property<Boolean> = project.objects.property()

    @Input
    @Optional
    val naming: Property<Naming> = project.objects.property()

    @Input
    val targetDir: DirectoryProperty = project.objects.directoryProperty()

    @Input
    @Optional
    val outputFileName: Property<String> = project.objects.property()

    @Input
    @Optional
    val maximumSupportedRpmVersion: Property<RpmBuilder.Version> = project.objects.property()

    @Input
    @Optional
    val signatureConfiguration: Property<String> = project.objects.property()

    private var eval: RulesetEvaluator by Delegates.notNull()

    private fun isSnapshotVersion(): Boolean {
        return project.version.toString().endsWith(snapshotSuffix)
    }

    private fun makeSnapshotReleaseString(): String {
        return if (snapshotBuildId.isPresent && snapshotBuildId.get().isNotEmpty()) {
            snapshotReleasePrefix.get() + snapshotBuildId
        } else {
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm", Locale.ROOT)
            snapshotReleasePrefix.get() + formatter.format(Instant.now().atOffset(ZoneOffset.UTC))
        }
    }

    private fun makeVersion(): RpmVersion {
        if (!forceRelease.get() && isSnapshotVersion()) {
            logger.info("Building with SNAPSHOT version")
            val baseVersion: String = project.version.toString().substring(0,
                    project.version.toString().length - snapshotSuffix.length)
            return RpmVersion(epoch.orNull, baseVersion, makeSnapshotReleaseString())
        }
        return RpmVersion(epoch.orNull, version.get(), release.get())
    }

    private fun makePackageName(): String {
        val packageName = packageName.get()
        val nameCase: Case = if (!naming.isPresent) {
            if (packageName.toLowerCase() != packageName) {
                logger.warn("Since version 0.9.0 of the RPM builder gradle the default behavior of forcing a lower case " +
                        "package name was removed. This package name seems to contain non-lowercase characters. " +
                        "It is possible to restore the previous behavior by setting the 'case' value in the 'naming' element.")
            }
            Case.UNMODIFIED
        } else {
            naming.get().case
        }

        return when (nameCase) {
            Case.LOWERCASE -> packageName.trim().toLowerCase()
            Case.UNMODIFIED -> packageName.trim()
        }
    }

    private fun makeTargetFilename(): String {
        return if (outputFileName.isPresent && outputFileName.get().isNotEmpty()) {
            outputFileName.get()
        } else {
            val outputFileName = when (naming.orNull?.defaultFormat) {
                Naming.DefaultFormat.LEGACY -> RpmFileNameProvider.LEGACY_FILENAME_PROVIDER.getRpmFileName(
                        makePackageName(), makeVersion(), architecture.get())
                else -> RpmFileNameProvider.DEFAULT_FILENAME_PROVIDER.getRpmFileName(makePackageName(),
                        makeVersion(), architecture.get())
            }
            logger.debug("Using generated file name - %s", outputFileName)
            outputFileName
        }
    }

    private fun makeTargetFile(targetDir: File): File {
        val outputFileName: String = makeTargetFilename()
        val targetFile = File(targetDir.path, outputFileName)
        project.logger.debug("Resolved output file name - fileName: %s, fullName: %s", outputFileName, targetFile)
        return targetFile
    }

    private fun testLeadFlags() {
        if (!leadOverrideArchitecture.isPresent) {
            val arch = Architecture.fromAlias(architecture.get())
            if (!arch.isPresent) {
                logger.warn("Architecture '%s' cannot be mapped to lead information. Consider using setting 'leadOverrideArchitecture'.", architecture.get())
            }
        }
        if (!leadOverrideOperatingSystem.isPresent) {
            val os = OperatingSystem.fromAlias(operatingSystem.get())
            if (!os.isPresent) {
                logger.warn("OperatingSystem '%s' cannot be mapped to lead information. Consider using setting 'leadOverrideOperatingSystem'.", operatingSystem.get())
            }
        }
    }

    private fun generateDefaultSourcePackageName(): String {
        return RpmLead.toLeadName(makePackageName(), makeVersion()) + ".src.rpm"
    }

    private interface StringSupplier : Supplier<String>

    private fun ifSet(setter: Consumer<String>, value: String?, vararg suppliers: StringSupplier) {
        if (!value.isNullOrEmpty()) {
            setter.accept(value)
            return
        }
        for (sup in suppliers) {
            val v: String = sup.get()
            if (v.isNotEmpty()) {
                setter.accept(v)
                return
            }
        }
    }

    private fun ifSet(setter: Consumer<String>, value: Property<String>, vararg suppliers: StringSupplier) {
        return ifSet(setter, value.orNull, *suppliers)
    }

    private fun makeHostname(): String {
        var hostname: String
        try {
            hostname = File("/etc/hostname").bufferedReader().use { br ->
                br.readLine()
            }
            if (hostname.isNotEmpty()) {
                logger.debug("Hostname: from /etc/hostname -> '%s'", hostname)
                return hostname
            }
        } catch (e: IOException) {
        }
        hostname = System.getenv("COMPUTERNAME")
        if (hostname != null && hostname.isNotEmpty()) {
            logger.debug("Hostname: from COMPUTERNAME -> '%s'", hostname)
            return hostname.toLowerCase()
        }
        hostname = System.getenv("hostname")
        if (hostname != null && hostname.isNotEmpty()) {
            logger.debug("Hostname: from hostname -> '%s'", hostname)
            return hostname
        }
        return try {
            hostname = InetAddress.getLocalHost().hostName
            logger.debug("Hostname: from lookup -> '%s'", hostname)
            hostname
        } catch (e: UnknownHostException) {
            logger.debug("Hostname: Falling back to 'localhost'")
            "localhost"
        }
    }

    private fun fillPackageInformation(builder: RpmBuilder) {
        val pinfo = builder.information
        val sourcePackage = if ((!sourcePackage.isPresent) || sourcePackage.get().isEmpty()) {
            if (generateDefaultSourcePackage.get()) {
                val sourcePackage: String = generateDefaultSourcePackageName()
                logger.debug("Using generated source package name of '%s'. You can disable this by setting 'generateDefaultSourcePackage' to false.", sourcePackage)
                sourcePackage
            } else {
                null
            }
        } else {
            sourcePackage.get()
        }
        ifSet({ description: String -> pinfo.description = description }, this.description)
        ifSet({ summary: String -> pinfo.summary = summary }, summary)
        ifSet({ group: String -> pinfo.group = group }, this.group)
        ifSet({ distribution: String -> pinfo.distribution = distribution }, distribution)
        ifSet({ operatingSystem: String -> pinfo.operatingSystem = operatingSystem }, operatingSystem)
        ifSet({ sourcePackage: String -> pinfo.sourcePackage = sourcePackage }, sourcePackage)
        if (evalHostname.isPresent && evalHostname.get()) {
            ifSet({ buildHost: String -> pinfo.buildHost = buildHost }, makeHostname())
        }
        ifSet({ url: String -> pinfo.url = url }, url)
        ifSet({ vendor: String -> pinfo.vendor = vendor }, vendor)
        ifSet({ packager: String -> pinfo.packager = packager }, packager)
        ifSet({ license: String -> pinfo.license = license }, license)
    }

    private fun fillScripts(builder: RpmBuilder) {
        setScript("prein", beforeInstallation, builder::setPreInstallationScript)
        setScript("postin", afterInstallation, builder::setPostInstallationScript)
        setScript("prerm", beforeRemoval, builder::setPreRemoveScript)
        setScript("postrm", afterRemoval, builder::setPostRemoveScript)
        setScript("pretrans", beforeTransaction, builder::setPreTransactionScript)
        setScript("posttrans", afterTransaction, builder::setPostTransactionScript)
    }

    private fun setScript(scriptName: String, script: Property<Script>, setter: KFunction2<String, String, Unit>) {
        if (!script.isPresent) {
            return
        }
        val scriptContent: String = script.get().makeScriptContent()
        if (scriptContent.isEmpty()) {
            return
        }
        var interpreter: String? = script.get().interpreter.orNull
        logger.debug("[script %s:]: explicit interpreter: %s", scriptName, interpreter)
        if (interpreter.isNullOrEmpty()) {
            interpreter = detectInterpreter(scriptContent)
            logger.debug("[script %s:]: detected interpreter: %s", scriptName, interpreter)
        }
        if (interpreter.isNullOrEmpty()) {
            interpreter = defaultScriptInterpreter.get()
            logger.debug("[script %s:]: default interpreter: %s", scriptName, interpreter)
        }
        logger.info("[script %s]: Using script interpreter: %s", scriptName, interpreter)
        logger.debug("[script %s]: %s", scriptName, scriptContent)
        setter.invoke(interpreter, scriptContent)
    }

    private fun detectInterpreter(scriptContent: String): String? {
        val firstLine: String = scriptContent.lineSequence().first()
        if (firstLine.isEmpty()) {
            return null
        }
        return if (firstLine.startsWith("#!") && firstLine.length > 2) {
            firstLine.substring(2)
        } else null
    }

    private fun fillDependencies(builder: RpmBuilder) {
        addAllDependencies("require", requires.get(), builder::addRequirement, ::validateName, null)
        addAllDependencies("prerequire", prerequisites.get(), builder::addRequirement, ::validateName) { flags ->
            flags.add(RpmDependencyFlags.PREREQ)
        }

        addAllDependencies("provide", provides.get(), builder::addProvides,
                (::validateName andThen this::validateNoVersion), null)
        addAllDependencies("conflict", conflicts.get(), builder::addConflicts, ::validateName, null)
        addAllDependencies("obsolete", obsoletes.get(), builder::addObsoletes, ::validateName, null)
        addAllDependencies("suggest", suggests.get(), builder::addSuggests, ::validateName, null)
        addAllDependencies("enhance", enhances.get(), builder::addEnhances, ::validateName, null)
        addAllDependencies("supplement", supplements.get(), builder::addSupplements, ::validateName, null)
        addAllDependencies("recommends", recommends.get(), builder::addRecommends, ::validateName, null)
    }

    private fun validateNoVersion(dep: SimpleDependency) {
        if (dep.version.isPresent && dep.version.get().isNotEmpty()) {
            logger.warn("Provides should not have a version: {} : {}. Use at your own risk!", dep.name, dep.version)
        }
    }

    private fun validateName(dep: SimpleDependency) {
        check(!dep.name.isPresent || dep.name.get().isEmpty()) { "'name' of dependency must be set" }
    }

    private fun <T : SimpleDependency> addAllDependencies(depName: String, deps: List<T>, adder: KFunction3<String, String?, Array<RpmDependencyFlags>, Unit>,
                                                          validator: Consumer<T>, flagsCustomizer: Consumer<MutableSet<RpmDependencyFlags>>?) {
        deps.forEach { dep ->
            validator.accept(dep)
            val name: String = dep.name.get()
            val version: String? = dep.version.orNull
            val flags: MutableSet<RpmDependencyFlags> = dep.flags.getOrElse(mutableSetOf())
            flagsCustomizer?.accept(flags)
            logger.info("Adding dependency [%s]: name = %s, version = %s, flags = %s", depName, name, version, flags)
            adder.invoke(name, version, flags.toTypedArray())
        }
    }

    private fun fillPayload(builder: RpmBuilder) {
        if (!entries.isPresent) {
            return
        }
        val ctx = builder.newContext()
        logger.debug("Building payload:")
        entries.get().forEach { entry ->
            if (!entry.skip.get()) {
                try {
                    entry.validate()
                } catch (e: IllegalStateException) {
                    throw GradleException(e.message ?: "")
                }
                fillFromEntry(ctx, entry)
            }
        }
    }

    private fun fillFromEntry(ctx: BuilderContext, entry: PackageEntry) {
        logger.debug("  %s:", entry.name)
        when {
            entry.directory.isPresent -> {
                fillFromEntryDirectory(ctx, entry)
            }
            entry.file.isPresent -> {
                fillFromEntryFile(ctx, entry)
            }
            entry.linkTo.isPresent -> {
                fillFromEntryLinkTo(ctx, entry)
            }
            entry.collect.isPresent -> {
                fillFromEntryCollect(ctx, entry)
            }
        }
    }

    private fun makeProvider(entry: PackageEntry, padding: String): GradleFileInformationProvider {
        var ruleset: String? = defaultRuleset.orNull
        if (entry.ruleset.isPresent && entry.ruleset.get().isNotEmpty()) {
            logger.debug("Using specified ruleset: '%s'", entry.ruleset)
            ruleset = entry.ruleset.get()
        } else if (!defaultRuleset.orNull.isNullOrEmpty()) {
            logger.debug("Using default ruleset: '%s'", defaultRuleset)
        }
        return GradleFileInformationProvider(this.eval, ruleset, entry, Consumer { l: String? -> logger.debug("%s%s", padding, l) })
    }

    private fun fillFromEntryDirectory(ctx: BuilderContext, entry: PackageEntry) {
        logger.debug("    as directory:")
        ctx.addDirectory(entry.name.get(), makeProvider(entry, "      - "))
    }

    private fun fillFromEntryFile(ctx: BuilderContext, entry: PackageEntry) {
        logger.debug("    as file:")
        val source: Path = entry.file.asFile.get().toPath().toAbsolutePath()
        logger.debug("      - source: %s", source)
        ctx.addFile(entry.name.get(), source, makeProvider(entry, "      - "))
    }

    private fun fillFromEntryLinkTo(ctx: BuilderContext, entry: PackageEntry) {
        logger.debug("    as symbolic link:")
        logger.debug("      - linkTo: %s", entry.linkTo.get())
        ctx.addSymbolicLink(entry.name.get(), entry.linkTo.get(), makeProvider(entry, "      - "))
    }

    private fun makeUnix(path: String): String {
        return path.replace("\\", "/")
    }

    private fun fillFromEntryCollect(ctx: BuilderContext, entry: PackageEntry) {
        logger.debug("    as collector:")
        val collector = entry.collect.get()
        logger.debug("      - configuration: %s", collector)
        val padding = "          "
        val from: Path = collector.from.asFile.get().toPath()

        val entryName = entry.name.get()
        val targetPrefix = if (entryName.endsWith("/")) entryName else "$entryName/"
        logger.debug("      - files:")
        val provider: GradleFileInformationProvider = makeProvider(entry, "            - ")
        val scanner = DirectoryScanner()
        scanner.basedir = from.toFile()
        scanner.setCaseSensitive(true)
        scanner.setFollowSymlinks(true)
        scanner.setIncludes(collector.includes.get().toTypedArray())
        scanner.setExcludes(collector.excludes.get().toTypedArray())
        scanner.scan()
        if (collector.isDirectories.get()) {
            for (directory in scanner.includedDirectories) {
                val dir: Path = from.resolve(directory)
                if (dir == from) {
                    continue
                }
                logger.debug("%s%s (dir)", padding, dir)
                val relative: Path = from.relativize(dir)
                val targetName: String = makeUnix(targetPrefix + relative.toString())
                logger.debug("%s  - target: %s", padding, targetName)
                ctx.addDirectory(targetName, provider)
            }
        }
        for (relative in scanner.includedFiles) {
            val file: Path = from.resolve(relative)
            val targetName: String = makeUnix(targetPrefix + relative)
            if (Files.isSymbolicLink(file)) {
                logger.debug("%s%s (symlink)", padding, file)
                if (collector.isSymbolicLinks.get()) {
                    val sym: Path = Files.readSymbolicLink(file)
                    logger.debug("%s%s (symlink)", padding, file)
                    logger.debug("%s  - target: %s", padding, targetName)
                    logger.debug("%s  - linkTo: %s", padding, sym.toString())
                } else {
                    logger.debug("%s%s (symlink) - ignoring symbolic links", padding, file)
                }
            } else {
                logger.debug("%s%s (file)", padding, file)
                logger.debug("%s  - target: %s", padding, targetName)
                ctx.addFile(targetName, file, provider)
            }
        }
    }

    private fun fillPrefixes(builder: RpmBuilder) {
        if (!prefixes.isPresent || prefixes.get().isEmpty()) {
            return
        }
        logger.debug("Building relocatable package: {}", prefixes)
        builder.setHeaderCustomizer { rpmTagHeader: Header<RpmTag?> ->
            // TODO: migrate to flags once https://github.com/eclipse/packagedrone/issues/130 is fixed
            val rpmTagPrefixes = 1098 // see http://ftp.rpm.org/max-rpm/s1-rpm-file-format-rpm-file-format.html
            rpmTagHeader.putStringArray(rpmTagPrefixes, *prefixes.get().toTypedArray())
        }
    }

    private fun checkVersion(builder: RpmBuilder) {
        val version: RpmBuilder.Version = builder.requiredRpmVersion!!
        logger.info("Required RPM version: %s", version)
        if (!maximumSupportedRpmVersion.isPresent) {
            return
        }
        if (version > maximumSupportedRpmVersion.get()) {
            throw GradleException("Generated RPM file: ${builder.targetFile} not compatible with version $maximumSupportedRpmVersion"
                    + "The generated RPM package would require at least version $version, " +
                    "however the build limits the supported RPM version to $maximumSupportedRpmVersion. " +
                    "Either raise the support RPM version or remove features requiring a " +
                    "more recent version of RPM.")
        }
    }


    @TaskAction
    fun execute() {
        if (skip.get()) {
            logger.debug("Skipping execution")
        }

        eval = RulesetEvaluator(rulesets.get())
        val targetDir = targetDir.asFile.getOrElse(project.buildDir)

        if (!targetDir.exists()) {
            try {
                Files.createDirectory(targetDir.toPath())
            } catch (e: FileAlreadyExistsException) {

            } catch (ioe: IOException) {
                logger.debug("Unable to create target directory {}", targetDir.path)
                throw GradleException("RPM build failed.", ioe)
            }
        }

        val targetFile = makeTargetFile(targetDir)

        logger.debug("Max supported RPM version: %s", maximumSupportedRpmVersion)
        logger.info("Writing to target to: %s", targetFile.path)
        logger.debug("Default script interpreter: %s", defaultScriptInterpreter)
        logger.debug("Default ruleset: %s", defaultRuleset)

        val packageName = makePackageName()
        val version = makeVersion()

        logger.info("RPM base information - name: %s, version: %s, arch: %s", packageName, version, architecture)
        testLeadFlags()

        RpmBuilder(packageName, version, architecture.get(), targetFile.toPath()).use { builder ->
            logger.info("Writing target file: %s", builder.targetFile)
            if (leadOverrideArchitecture.isPresent) {
                logger.info("Override RPM lead architecture: %s", leadOverrideArchitecture)
                builder.setLeadOverrideArchitecture(leadOverrideArchitecture.get())
            }
            if (leadOverrideOperatingSystem.isPresent) {
                logger.info("Override RPM lead operating system: %s", leadOverrideOperatingSystem)
                builder.setLeadOverrideOperatingSystem(leadOverrideOperatingSystem.get())
            }

            fillPackageInformation(builder)
            fillScripts(builder)
            fillDependencies(builder)
            fillPayload(builder)
            fillPrefixes(builder)

            builder.build()
            checkVersion(builder)
        }

    }
}