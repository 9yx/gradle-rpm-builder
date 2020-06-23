import ru.ingins.gradle.rpm.builder.*
import ru.ingins.gradle.rpm.builder.Rule

plugins {
    idea
    java
    `kotlin-dsl`
    id("ru.ingins.gradle.rpm.builder") version "0.0.0-SNAPSHOT"
}

group "ru.ingins"
version "0.0.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

rpm {
    attach.set(false)
    group.set("Application/Misc")
    rulesets.addAll(
            Ruleset(project.objects).apply {
                id.set("my-default")
                rules.addAll(
                        Rule(project.objects).apply {
                            `when`.set(When(project.objects).apply {
                                type.set("directory")
                            })
                            mode.set(750)
                        },
                        Rule(project.objects).apply {
                            `when`.set(When(project.objects).apply {
                                prefix.set("/etc/")
                            })
                            configuration.set(true)
                        }
                )
            }
    )
    entries.addAll(
            PackageEntry(project.objects).apply {
                name.set("/etc/foo")
                directory.set(true)
                user.set("root")
                group.set("root")
                mode.set(755)
            },
            PackageEntry(project.objects).apply {
                name.set("/etc/foo/bar.txt")
                file.set(File("src/main/resources/bar.txt"))
                user.set("root")
                group.set("root")
                mode.set(755)
            }
//            PackageEntry(project.objects).apply {
//                name.set("/usr/lib/foobar")
//                collect.set(Collector(project.objects).apply {
//                    from.set(File("target/classes"))
//                })
//                ruleset.set("my-default")
//            }
    )

}

