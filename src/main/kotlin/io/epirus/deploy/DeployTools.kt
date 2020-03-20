@file:JvmName("DeployTools")

package io.epirus.deploy

import io.github.classgraph.ClassGraph
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

fun findDeployer(network: String, pkg: String): Deployer {
    val predeployAnnotation = Predeploy::class.java.name

    val predeployMethods = mutableListOf<Method>()

    ClassGraph()
        //.verbose()
        .enableAllInfo()
        .whitelistPackages(pkg)
        .scan().use { scanResult ->
            for (classInfo in scanResult.allClasses) {
                classInfo
                    .declaredMethodInfo
                    .filter {
                        it.hasAnnotation(predeployAnnotation) &&
                        it.isPublic &&
                        it.parameterInfo.isEmpty()
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

fun runDeployer(deployer: Deployer, pkg: String) {
    TODO()
}