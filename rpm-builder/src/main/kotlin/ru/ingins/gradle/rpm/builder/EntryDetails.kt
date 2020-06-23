package ru.ingins.gradle.rpm.builder

import org.eclipse.packager.rpm.FileFlags
import org.eclipse.packager.rpm.build.FileInformation
import org.gradle.api.model.ObjectFactory
import java.util.*


open class EntryDetails(objectFactory: ObjectFactory) {
    var mode = objectFactory.property(Short::class.java)
    var configuration = objectFactory.property(Boolean::class.java)
    var documentation = objectFactory.property(Boolean::class.java)
    var license = objectFactory.property(Boolean::class.java)
    var readme = objectFactory.property(Boolean::class.java)
    var ghost = objectFactory.property(Boolean::class.java)
    var missingOk = objectFactory.property(Boolean::class.java)
    var noreplace = objectFactory.property(Boolean::class.java)
    var user = objectFactory.property(String::class.java)
    var group = objectFactory.property(String::class.java)
    var skip = objectFactory.property(Boolean::class.java).convention(false)
    var verify = objectFactory.property(VerifyDetails::class.java)

    private fun setFlag(info: FileInformation, flag: FileFlags) {
        val flags = info.fileFlags
        flags?.add(flag) ?: {
            info.fileFlags = EnumSet.of(flag)
        }()
    }

    open fun validate() {
    }

    fun apply(info: FileInformation): Boolean {
        var didApply = false

        if (configuration.isPresent) {
            setFlag(info, FileFlags.CONFIGURATION)
            didApply = true
        }
        if (documentation.isPresent) {
            setFlag(info, FileFlags.DOC)
            didApply = true
        }
        if (license.isPresent) {
            setFlag(info, FileFlags.LICENSE)
            didApply = true
        }
        if (readme.isPresent) {
            setFlag(info, FileFlags.README)
            didApply = true
        }
        if (ghost.isPresent) {
            setFlag(info, FileFlags.GHOST)
            didApply = true
        }
        if (missingOk.isPresent) {
            setFlag(info, FileFlags.MISSINGOK)
            didApply = true
        }
        if (noreplace.isPresent) {
            setFlag(info, FileFlags.NOREPLACE)
            didApply = true
        }
        if (user.isPresent && user.get().isNotEmpty()) {
            info.user = user.get()
            didApply = true
        }
        if (group.isPresent && group.get().isNotEmpty()) {
            info.group = group.get()
            didApply = true
        }
        if (mode.isPresent) {
            info.mode = mode.get()
            didApply = true
        }
        if (verify.isPresent) {
            verify.get().apply(info)
            didApply = true
        }
        return didApply
    }
}