package ru.ingins.gradle.rpm.builder

import org.gradle.api.model.ObjectFactory
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class Script @Inject constructor(objectFactory: ObjectFactory) {
    var interpreter = objectFactory.property(String::class.java)
    var file = objectFactory.fileProperty()
    var script = objectFactory.property(String::class.java)

    fun makeScriptContent(): String {
        check(!(file.isPresent && script.isPresent)) { "Script must not have 'file' and 'script' set at the same time." }
        if (file.isPresent) {
            InputStreamReader(FileInputStream(file.get().asFile), StandardCharsets.UTF_8).use { reader ->
                return reader.readText()
            }
        }
        return script.get()
    }
}