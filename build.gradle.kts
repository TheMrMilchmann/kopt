import org.gradle.java.*
import org.gradle.jvm.tasks.*
import org.jetbrains.dokka.gradle.*
import org.jetbrains.kotlin.gradle.dsl.*

buildscript {
    repositories {
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }

    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.16-eap-3")
    }
}

plugins {
    maven
    signing
    kotlin("jvm") version "1.2.21"
    id("org.gradle.java.experimental-jigsaw").version("0.1.1") apply false
}

apply {
    plugin("org.jetbrains.dokka")
}

val nextVersion = "0.5.0"

group = "com.github.themrmilchmann.kopt"
version = when (deployment.type) {
    BuildType.SNAPSHOT -> "$nextVersion-SNAPSHOT"
    else -> nextVersion
}

artifacts {
    fun artifactNotation(artifact: String, classifier: String? = null) =
        if (classifier == null) {
            mapOf(
                "file" to File(buildDir, "libs/$artifact-$version.jar"),
                "name" to artifact,
                "type" to "jar"
            )
        } else {
            mapOf(
                "file" to File(buildDir, "libs/$artifact-$version-$classifier.jar"),
                "name" to artifact,
                "type" to "jar",
                "classifier" to classifier
            )
        }

    add("archives", artifactNotation(project.name))
    add("archives", artifactNotation(project.name, "sources"))
    add("archives", artifactNotation(project.name, "javadoc"))
}

signing {
    isRequired = deployment.type == BuildType.RELEASE
    sign(configurations["archives"])
}

val projectJdk9 = project("jdk9") {
    apply {
        plugin("java")
        plugin("org.gradle.java.experimental-jigsaw")
    }

    the<JavaModule>().setName("com.github.themrmilchmann.kopt")

    java.sourceSets["main"].java.setSrcDirs(mutableListOf("java"))

    java {
        sourceCompatibility = JavaVersion.VERSION_1_9
        targetCompatibility = JavaVersion.VERSION_1_9
    }

    tasks {
        "compileJava"(JavaCompile::class) {
            onlyIf { JavaVersion.toVersion(toolChain.version) >= JavaVersion.VERSION_1_9 }
            options.compilerArgs.addAll(arrayOf("--release", "9"))
        }
    }
}

tasks {
    "test"(Test::class) {
        useTestNG()
    }

    "compileJava"(JavaCompile::class) {
        onlyIf { JavaVersion.toVersion(toolChain.version) == JavaVersion.VERSION_1_9 }
        options.compilerArgs.addAll(arrayOf("--release", "8"))
    }

    "jar"(Jar::class) {
        dependsOn(projectJdk9.tasks["compileJava"])

        baseName = project.name

        into("META-INF/versions/9") {
            from(projectJdk9.tasks["compileJava"].outputs.files) {
                include("module-info.class")
            }
        }

        manifest {
            attributes(mapOf(
                "Name" to "kopt",
                "Specification-Version" to project.version,
                "Specification-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                "Multi-Release" to "true"
            ))
        }
    }

    val sourcesJar = "sourcesJar"(Jar::class) {
        baseName = project.name
        classifier = "sources"
        from(java.sourceSets["main"].allSource)
    }

    val dokka = "dokka"(DokkaTask::class) {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/javadoc"
    }

    val javadocJar = "javadocJar"(Jar::class) {
        dependsOn(dokka)

        baseName = project.name
        classifier = "javadoc"
        from(File(buildDir, "javadoc"))
    }

    "signArchives" {
        dependsOn(sourcesJar, javadocJar)
    }

    "uploadArchives"(Upload::class) {
        repositories {
            withConvention(MavenRepositoryHandlerConvention::class) {
                mavenDeployer {
                    withGroovyBuilder {
                        "repository"("url" to deployment.repo) {
                            "authentication"(
                                "userName" to deployment.user,
                                "password" to deployment.password
                            )
                        }
                    }

                    if (deployment.type === BuildType.RELEASE) beforeDeployment { signing.signPom(this) }

                    pom.project {
                        withGroovyBuilder {
                            "artifactId"(project.name)

                            "name"(project.name)
                            "description"("Simple CLI argument parser for the JVM")
                            "packaging"("jar")
                            "url"("https://github.com/TheMrMilchmann/kopt")

                            "licenses" {
                                "license" {
                                    "name"("BSD-3-Clause")
                                    "url"("https://github.com/TheMrMilchmann/kopt/blob/master/LICENSE")
                                    "distribution"("repo")
                                }
                            }

                            "developers" {
                                "developer" {
                                    "id"("TheMrMilchmann")
                                    "name"("Leon Linhart")
                                    "email"("themrmilchmann@gmail.com")
                                    "url"("https://github.com/TheMrMilchmann")
                                }
                            }

                            "scm" {
                                "connection"("scm:git:git://github.com/TheMrMilchmann/kopt.git")
                                "developerConnection"("scm:git:git://github.com/TheMrMilchmann/kopt.git")
                                "url"("https://github.com/TheMrMilchmann/kopt.git")
                            }
                        }
                    }
                }
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions.languageVersion = "1.2"
    kotlinOptions.apiVersion = "1.1"
    kotlinOptions.jvmTarget = "1.8"

    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
}

val Project.deployment: Deployment
    get() = when {
        hasProperty("release") -> Deployment(
            BuildType.RELEASE,
            "https://oss.sonatype.org/service/local/staging/deploy/maven2/",
            getProperty("sonatypeUsername"),
            getProperty("sonatypePassword")
        )
        hasProperty("snapshot") -> Deployment(
            BuildType.SNAPSHOT,
            "https://oss.sonatype.org/content/repositories/snapshots/",
            getProperty("sonatypeUsername"),
            getProperty("sonatypePassword")
        )
        else -> Deployment(BuildType.LOCAL, repositories.mavenLocal().url.toString())
    }

fun Project.getProperty(k: String) =
    if (extra.has(k))
        extra[k] as String
    else
        System.getenv(k)

enum class BuildType {
    LOCAL,
    SNAPSHOT,
    RELEASE
}

data class Deployment(
    val type: BuildType,
    val repo: String,
    val user: String? = null,
    val password: String? = null
)

allprojects {
    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8", "1.2.21"))
        compileOnly("com.google.code.findbugs:jsr305:3.0.2")

        runtime(kotlin("stdlib-jdk8", "[1.1.0,)"))

        testCompile("org.testng:testng:6.13.1")
    }
}