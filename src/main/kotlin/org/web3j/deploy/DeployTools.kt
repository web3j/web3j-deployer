@file:JvmName("DeployTools")

package org.web3j.deploy

import io.github.classgraph.ClassGraph
import org.web3j.protocol.Web3j
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider
import java.lang.IllegalArgumentException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.net.URLClassLoader

class Deployer(
    val web3j: Web3j,
    val transactionManager: TransactionManager,
    val gasProvider: ContractGasProvider,
    val profile: String
)

fun deploy(profile: String, pkg: String, classLoader: ClassLoader) {
    findDeployer(profile, pkg, classLoader)?.let {
        runDeployer(it, pkg)
    }
}

fun findDeployer(profile: String, pkg: String, classLoader: ClassLoader): Deployer? {
    val predeployAnnotation = Predeploy::class.java.name
    val predeployMethods = mutableListOf<Method>()

    ClassGraph()
//        .verbose()
//        .enableAllInfo()
        .enableAnnotationInfo()
        .whitelistPackages(pkg)
        .overrideClassLoaders(classLoader)
        .scan().use { scanResult ->
            for (classInfo in scanResult.allClasses) {
                //TODO: Remove this print line
                println("Class name: " + classInfo.name + " and package info: " + classInfo.packageInfo)
                classInfo
                    .declaredMethodInfo
                    .filter {
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

    return if (predeployMethods.size > 1) { //throw IllegalArgumentException("Invalid number of deployer candidates found for profile $profile within $pkg: ${predeployMethods.size}")

        val predeployMethod = predeployMethods.first()

        val instance = if (Modifier.isStatic(predeployMethod.modifiers)) null
        else predeployMethod.declaringClass.getDeclaredConstructor().newInstance()

        predeployMethod.invoke(instance) as Deployer
    } else {
        null
    }
}

private fun runDeployer(deployer: Deployer, method: Method, instance: Any?) {
    method.invoke(instance, deployer)
}

fun runDeployer(deployer: Deployer, pkg: String) {
    val deployableAnnotation = Deployable::class.java.name

    val deployableMethods = mutableListOf<Method>()

    ClassGraph()
        //.verbose()
//        .enableAllInfo()
        .enableAnnotationInfo()
        .whitelistPackages(pkg)
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
                        Pair(it.loadClassAndGetMethod(), it.annotationInfo
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

    val methodInstance = mutableMapOf<Class<*>, Any?>()

    // List with orders in ascending order.
    deployableMethods.forEach { method ->
        runDeployer(deployer, method, methodInstance.getOrPut(method.declaringClass) {
            if (Modifier.isStatic(method.modifiers)) null else method.declaringClass.getDeclaredConstructor()
                .newInstance()
        })
    }
}