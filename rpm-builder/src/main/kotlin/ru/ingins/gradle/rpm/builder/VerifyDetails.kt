package ru.ingins.gradle.rpm.builder

import org.eclipse.packager.rpm.VerifyFlags
import org.eclipse.packager.rpm.build.FileInformation
import org.gradle.api.model.ObjectFactory
import java.util.*
import javax.inject.Inject


open class VerifyDetails @Inject constructor(objectFactory: ObjectFactory) {
    var fileDigest = objectFactory.property(Boolean::class.java).convention(false)
    var size = objectFactory.property(Boolean::class.java).convention(false)
    var linkto = objectFactory.property(Boolean::class.java).convention(false)
    var user = objectFactory.property(Boolean::class.java).convention(false)
    var group = objectFactory.property(Boolean::class.java).convention(false)
    var mtime = objectFactory.property(Boolean::class.java).convention(false)
    var mode = objectFactory.property(Boolean::class.java).convention(false)
    var rdev = objectFactory.property(Boolean::class.java).convention(false)
    var caps = objectFactory.property(Boolean::class.java).convention(false)

    private fun transfer(target: MutableSet<VerifyFlags>, `val`: Boolean, flag: VerifyFlags) {
        if (`val`) {
            target.add(flag)
        }
    }

    fun apply(info: FileInformation) {
        val verifyFlags: MutableSet<VerifyFlags> = EnumSet.noneOf(VerifyFlags::class.java)
        transfer(verifyFlags, fileDigest.get(), VerifyFlags.MD5)
        transfer(verifyFlags, size.get(), VerifyFlags.SIZE)
        transfer(verifyFlags, linkto.get(), VerifyFlags.LINKTO)
        transfer(verifyFlags, user.get(), VerifyFlags.USER)
        transfer(verifyFlags, group.get(), VerifyFlags.GROUP)
        transfer(verifyFlags, mtime.get(), VerifyFlags.MTIME)
        transfer(verifyFlags, mode.get(), VerifyFlags.MODE)
        transfer(verifyFlags, caps.get(), VerifyFlags.CAPS)
        info.verifyFlags = verifyFlags
    }


}