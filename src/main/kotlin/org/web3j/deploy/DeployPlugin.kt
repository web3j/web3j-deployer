package org.web3j.deploy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import java.net.URLClassLoader

class DeployPlugin: Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create("deploy", DeployExtension::class.java)

        project.afterEvaluate {
            val plugin = it.convention.getPlugin(JavaPluginConvention::class.java)

            plugin.sourceSets.forEach { sourceSet ->
                processAnnotations(sourceSet)
            }

//            val task = project.tasks.create("deploy") { task ->
//                task.doLast {
//                    println("${extension.profileName} from ${extension.networkName}")
//                    println("Hello from Deploy Plugin")
//                    deploy("network-1", "org.web3j")
//                    println("Deployer found")
//                }
//                task.group = "web3j"
//            }
        }
    }

    private fun processAnnotations(sourceSet: SourceSet) {

        val urls = sourceSet.output.classesDirs.map {
            it.toURI().toURL()
        }.toTypedArray()

        val deployer = findDeployer("network-1", "org.testing", URLClassLoader(urls))
    }
}