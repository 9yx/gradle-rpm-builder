package ru.ingins.gradle.rpm.builder

import org.eclipse.packager.rpm.deps.RpmDependencyFlags
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject


open class SimpleDependency @Inject constructor(objectFactory: ObjectFactory) {
    companion object {
        protected var OP_MAP: MutableMap<String, Array<RpmDependencyFlags>> = mutableMapOf()

        init {
            OP_MAP["="] = arrayOf(RpmDependencyFlags.EQUAL)
            OP_MAP[">="] = arrayOf(RpmDependencyFlags.EQUAL, RpmDependencyFlags.GREATER)
            OP_MAP["<="] = arrayOf(RpmDependencyFlags.EQUAL, RpmDependencyFlags.LESS)
            OP_MAP[">"] = arrayOf(RpmDependencyFlags.GREATER)
            OP_MAP["<"] = arrayOf(RpmDependencyFlags.LESS)
            OP_MAP["eq"] = arrayOf(RpmDependencyFlags.EQUAL)
            OP_MAP["ge"] = arrayOf(RpmDependencyFlags.EQUAL, RpmDependencyFlags.GREATER)
            OP_MAP["le"] = arrayOf(RpmDependencyFlags.EQUAL, RpmDependencyFlags.LESS)
            OP_MAP["gt"] = arrayOf(RpmDependencyFlags.GREATER)
            OP_MAP["lt"] = arrayOf(RpmDependencyFlags.LESS)
        }
    }

    var name = objectFactory.property(String::class.java)
    var version = objectFactory.property(String::class.java)
    var flags = objectFactory.setProperty(RpmDependencyFlags::class.java)
//    fun set(string: String) {
//        val toks = string.split("\\s+").toTypedArray()
//        when (toks.size) {
//            1 -> {
//                setAll(toks[0], null)
//            }
//            3 -> {
//                val flags = OP_MAP[toks[1]]
//                        ?: throw IllegalArgumentException(String.format("Operator '%s' is unknown", toks[1]))
//                setAll(toks[0], toks[2], *flags)
//            }
//            else -> {
//                throw IllegalArgumentException(String.format("Invalid short format: '%s'", string))
//            }
//        }
//    }
//
//    private fun setAll(name: String, version: String?, vararg flags: RpmDependencyFlags) {
//        this.name = name
//        this.version = version
//        this.flags.clear()
//        this.flags.addAll(flags)
//    }
}