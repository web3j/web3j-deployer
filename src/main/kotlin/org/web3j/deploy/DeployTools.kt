@file:JvmName("DeployTools")

package org.web3j.deploy

import io.github.classgraph.ClassGraph
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class DeployTools {
    fun deploy(profile: String, pkg: String) {
        println("Starting deploy command with profile=$profile, package=$pkg")
        val deployer = findDeployer(profile, pkg)
        runDeployer(deployer, pkg)
    }

    fun findDeployer(profile: String, pkg: String): Deployer {
        val predeployAnnotation = Predeploy::class.java.name
        val predeployMethods = mutableListOf<Method>()

        ClassGraph()
            .enableAllInfo()
            .acceptPackages(pkg)
            .scan().use { scanResult ->
                for (classInfo in scanResult.allClasses) {
                    classInfo
                        .declaredMethodInfo
                        .filter { it ->
                            it.hasAnnotation(predeployAnnotation) &&
                                    it.isPublic &&
                                    it.parameterInfo.isEmpty() &&
                                    it.annotationInfo
                                        .filter { it.name.equals(predeployAnnotation) }
                                        .map { it.parameterValues.getValue("profile") }
                                        .contains(profile)
                        }.map {
                            it.loadClassAndGetMethod()
                        }.filter {
                            Deployer::class.java == it.returnType
                        }.forEach {
                            predeployMethods.add(it)
                        }
                }
            }

        if (predeployMethods.size != 1) throw IllegalArgumentException("Invalid number of deployer candidates found for profile $profile within $pkg: ${predeployMethods.size}")

        val predeployMethod = predeployMethods.first()

        println("Obtaining deployer from ${predeployMethod.declaringClass.name}::${predeployMethod.name}")

        val instance = (if (Modifier.isStatic(predeployMethod.modifiers)) null else predeployMethod.declaringClass.getDeclaredConstructor().newInstance())

        return predeployMethod.invoke(instance) as Deployer
    }

    private fun runDeployer(deployer: Deployer, method: Method, instance: Any?) {
        method.invoke(instance, deployer)
    }

    fun runDeployer(deployer: Deployer, pkg: String) {
        val deployableAnnotation = Deployable::class.java.name

        val deployableMethods = mutableListOf<Method>()

        ClassGraph()
            .enableAllInfo()
            .acceptPackages(pkg)
            .scan().use { scanResult ->
                for (classInfo in scanResult.allClasses) {
                    classInfo
                        .declaredMethodInfo
                        .filter {
                            it.hasAnnotation(deployableAnnotation) &&
                                    it.isPublic &&
                                    it.parameterInfo.size == 1
                        }
                        .map {
                            val methodClass = this.javaClass.classLoader.loadClass(it.className)
                            val method = methodClass.getMethod(it.name, Deployer::class.java)
                            Pair(method, it.annotationInfo
                                .filter { it.name.equals(deployableAnnotation) }
                                .map { it.parameterValues.getValue("order") }
                                .filterIsInstance<Int>())
                        }
                        .filter {
                            Deployer::class.java == it.first.parameterTypes.first() && it.second.isNotEmpty()
                        }
                        .map {
                            Pair(it.first, it.second.min())
                        }
                        .sortedBy {
                            it.second
                        }
                        .forEach {
                            deployableMethods.add(it.first)
                        }
                }
            }

        println("Found " + deployableMethods.size + " methods with @Deployable annotation")

        val methodInstance = mutableMapOf<Class<*>, Any?>()

        deployableMethods.forEach { method ->
            println("Running deployer on ${method.declaringClass.name}::${method.name}")
            runDeployer(deployer, method, methodInstance.getOrPut(method.declaringClass) {
                if (Modifier.isStatic(method.modifiers)) null else method.declaringClass.getDeclaredConstructor().newInstance()
            })
        }
    }
}