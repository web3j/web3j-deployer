package org.web3j.deploy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import java.net.URL
import java.net.URLClassLoader

class DeployPlugin: Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create("deploy", DeployExtension::class.java)

        val urls : MutableList<URL> = mutableListOf()

        project.afterEvaluate {
            val plugin = it.convention.getPlugin(JavaPluginConvention::class.java)
            plugin.sourceSets.forEach { sourceSet ->
                sourceSet.runtimeClasspath.forEach{path ->
                    urls.add(path.toURI().toURL())
                }
            }
        }

        project.tasks.create("deploy") { task ->
            task.doLast {
                println("${extension.profileName} from ${extension.networkName}")
                println("Hello from Deploy Plugin")
                deploy("network-1", "org.testing", URLClassLoader(urls.toTypedArray()))
            }
            task.group = "web3j"
        }
    }
}