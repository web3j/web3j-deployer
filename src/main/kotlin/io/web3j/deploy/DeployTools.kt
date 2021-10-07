@file:JvmName("DeployTools")

package io.web3j.deploy

import io.github.classgraph.ClassGraph
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider
import java.lang.IllegalArgumentException
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class Deployer(
    val web3j: Web3j,
    val transactionManager: TransactionManager,
    val gasProvider: ContractGasProvider,
    val network: String
)

fun main(args: Array<String>)  {
    val networkName: String;
    val packageName: String;
    if (args.size == 1 && args[0].split(',').size == 2) {
        networkName = args[0].split(',')[0]
        packageName = args[0].split(',')[1]
    } else  {
        throw IllegalArgumentException("Network name or package name is unknown")
    }
    println("Hello from Kotlin ${networkName} ${packageName}")
    val deployer = findDeployer(networkName, packageName)
    runDeployer(deployer, packageName)
}

fun findDeployer(network: String, pkg: String): Deployer {
    val predeployAnnotation = Predeploy::class.java.name

    val predeployMethods = mutableListOf<Method>()

    ClassGraph()
//        .verbose()
        .enableAllInfo()
        .whitelistPackages(pkg)
        .scan().use { scanResult ->
            for (classInfo in scanResult.allClasses) {
//                println("Class names are: " + classInfo.name)
                classInfo
                    .declaredMethodInfo
                    .filter {
                        it.hasAnnotation(predeployAnnotation) &&
                        it.isPublic &&
                        it.parameterInfo.isEmpty() &&
                        it.annotationInfo
                            .filter { it.name.equals(predeployAnnotation) }
                            .map { it.parameterValues.getValue("network") }
                            .contains(network)
                    }
                    .map {
                        it.loadClassAndGetMethod()
                    }
                    .filter {
                        Deployer::class.java.equals(it.returnType)
                    }
                    .forEach {
                        predeployMethods.add(it)
                    }
            }
        }

    if (predeployMethods.size != 1) throw IllegalArgumentException("Invalid number of deployer candidates found for network $network within $pkg: ${predeployMethods.size}")

    val predeployMethod = predeployMethods.first()

    val instance = if (Modifier.isStatic(predeployMethod.modifiers)) null else predeployMethod.declaringClass.getDeclaredConstructor().newInstance()

    return predeployMethod.invoke(instance) as Deployer
}

private fun runDeployer(deployer: Deployer, method: Method, instance: Any?) {
    method.invoke(instance, deployer)
}

fun runDeployer(deployer: Deployer, pkg: String) {
    val deployableAnnotation = Deployable::class.java.name

    val deployableMethods = mutableListOf<Method>()

    ClassGraph()
        //.verbose()
        .enableAllInfo()
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
                        Deployer::class.java.equals(it.first.parameterTypes.first()) && it.second.isNotEmpty()
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
            if (Modifier.isStatic(method.modifiers)) null else method.declaringClass.getDeclaredConstructor().newInstance()
        })
    }
}