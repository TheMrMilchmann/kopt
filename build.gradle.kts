plugins {
    maven
    signing
    kotlin("jvm") version "1.2.0"
}

val nextVersion = "0.1.0"

group = "com.github.themrmilchmann.kopt"
version = when (deployment.type) {
    BuildType.SNAPSHOT -> "$nextVersion-SNAPSHOT"
    else -> nextVersion
}

tasks {
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
                            "description"("Kotlin CLI argument parser")
                            "packaging"("jar")
                            "url"("https://github.com/TheMrMilchmann/kopt")

                            "licenses" {
                                "license" {
                                    "name"("BSD-3-Clause")
                                    "url"("https://github.com/TheMrMilchmann/kopt/blob/master/LICENSE.md")
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

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    testCompile("org.testng:testng:6.13.1")
}