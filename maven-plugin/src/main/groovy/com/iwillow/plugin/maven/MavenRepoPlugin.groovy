package com.iwillow.plugin.maven

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.BaseExtension
import org.gradle.api.tasks.bundling.Jar

public class MavenRepoPlugin implements Plugin<Project> {
    private Project project
    private RootExtension extension

    void apply(Project p) {
        project = p
        applyMavenPlugin()
        createExtension()
        project.afterEvaluate {
            createTask()
        }
    }

    def BaseExtension getAndroid() {
        return project.android
    }

    def applyMavenPlugin() {
        project.apply plugin: 'maven'
    }

    def createExtension() {
        project.extensions.create("LibUploader", RootExtension)
    }

    def createTask() {
        upload()

        project.task('upload',
                group: 'com.iwillow.plugin',
                description: 'upload artifact to maven repository',
                dependsOn: ['uploadArchives']).doLast {

            println 'uploaded done.'
        }

        if (extension.isUploadJar) {
            project.with {
                task('androidSourcesJar', type: Jar) {
                    classifier = 'sources'
                    from getAndroid().sourceSets.main.java.sourceFiles
                }

                artifacts {
                    archives androidSourcesJar
                }
            }
        }
    }

    def upload() {
        extension = project.LibUploader

        if (extension == null) {
            throw new IllegalArgumentException('extension is null')
        }

        if (!extension.libVersion?.trim() || !extension.libArtifactId?.trim()) {
            throw new IllegalArgumentException('library version or artifactId is null')
        }

        if (!extension.libGroup?.trim()) {
            extension.libGroup = MAVEN_DEFAULT_GROUP
        }

        boolean isRemote = extension.isRemote

        if (!isRemote) {
            extension.libVersion += '-local'
        }

        println "group: ${extension.libGroup}, " +
                "artifactId: ${extension.libArtifactId}, " +
                "ver: ${extension.libVersion}, " +
                "isRemote: ${extension.isRemote}, " +
                "isUploadJar: ${extension.isUploadJar}"

        project.with {
            uploadArchives {
                repositories {
                    mavenDeployer {
                        //repository(url: uri('../repo'))
                        def localUrl = mavenLocal().url
                         repository(url: (isRemote ? MAVEN_REMOTE_URL_RELEASE : localUrl)) {
                             authentication(userName: MAVEN_RELEASE_USER, password: MAVEN_RELEASE_PASSWORD)
                         }

                         snapshotRepository(url: (isRemote ? MAVEN_REMOTE_URL_SNAPSHOT : localUrl)) {
                             authentication(userName: MAVEN_SNAPSHOT_USER, password: MAVEN_SNAPSHOT_PASSWORD)
                         }

                        pom {
                            artifactId = extension.libArtifactId
                            group = extension.libGroup
                            version = extension.libVersion
                        }
                    }
                }
            }
        }
    }

}
