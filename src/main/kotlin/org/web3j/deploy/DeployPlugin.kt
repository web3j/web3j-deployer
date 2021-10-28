package org.web3j.deploy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import java.net.URL
import java.net.URLClassLoader

class DeployPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        val urls : MutableList<URL> = mutableListOf()

        // Adding runtime classpath of project
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
                val loader = URLClassLoader(urls.toTypedArray())
                val deployToolsClass = loader.loadClass(DeployTools().javaClass.name)
                deployToolsClass.declaredMethods.forEach { method ->
                    println(method.name)
                    method.parameters.forEach { param -> println(param.type) }
                }

                if (profileName != null && packageName != null) {
                    val deployMethod = deployToolsClass.getDeclaredMethod(
                        "deploy",
                        String::class.java,
                        String::class.java
                    )

                    deployMethod.invoke(deployToolsClass.newInstance(), profileName, packageName)
                }
                else {
                    println("Parameter profile/package not passed correctly")
                }
            }
            task.group = "web3j"
        }
    }
}