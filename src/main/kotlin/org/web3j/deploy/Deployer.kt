@file:JvmName("Deployer")

package org.web3j.deploy

import org.web3j.protocol.Web3j
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider

class Deployer(
    val web3j: Web3j,
    val transactionManager: TransactionManager,
    val gasProvider: ContractGasProvider,
    val profile: String
)