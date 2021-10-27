package org.web3j.deploy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import java.net.URL
import java.net.URLClassLoader

class DeployPlugin: Plugin<Project> {

    override fun apply(project: Project) {
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
            var profileName : String? = null
            var packageName : String? = null
            if (project.properties.contains("profile")) {
                profileName = project.properties.getValue("profile").toString()
            }
            if (project.properties.contains("package")) {
                packageName = project.properties.getValue("package").toString()
            }
            task.doLast {
                println("Hello from Deploy Plugin")
                if (profileName != null && packageName != null)
                    deploy(profileName, packageName, URLClassLoader(urls.toTypedArray()))
            }
            task.group = "web3j"
        }
    }
}