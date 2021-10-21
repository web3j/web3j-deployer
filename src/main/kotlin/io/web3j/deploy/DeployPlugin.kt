package io.web3j.deploy

import org.gradle.api.Plugin
import org.gradle.api.Project

class DeployPlugin: Plugin<Project> {
    override fun apply(project: Project) {

        val extension = project.extensions.create("deploy", DeployExtension::class.java)

        val task = project.tasks.create("deploy") {
            it.doLast {
                println("${extension.profileName} from ${extension.networkName}")
                println("Hello world")
                findDeployer("network-1", "org.web3j")
                println("Hello world")
            }
        }

        task.group = "mahima-deploy"
    }
}