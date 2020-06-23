package ru.ingins.gradle.rpm.builder

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

class Dependency @Inject constructor(objectFactory: ObjectFactory) : SimpleDependency(objectFactory) {
}