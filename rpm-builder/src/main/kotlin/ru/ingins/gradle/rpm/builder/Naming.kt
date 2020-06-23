package ru.ingins.gradle.rpm.builder


open class Naming {
    enum class Case {
        UNMODIFIED,
        LOWERCASE
    }

    enum class DefaultFormat {
        DEFAULT,
        LEGACY
    }

    var case = Case.UNMODIFIED
    var defaultFormat = DefaultFormat.DEFAULT
}