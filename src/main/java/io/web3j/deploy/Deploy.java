package io.web3j.deploy;

import io.web3j.deploy.contracts.Food;
import io.web3j.deploy.contracts.SimpleStorage;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.util.ArrayList;
import java.util.List;

public class Deploy {

    List<String> deployedContractAddresses = new ArrayList<>();

    @Predeploy(profile = "network-1")
    public Deployer deployable2() {
        // Look up for the configuration from somewhere.
        // We can also use WalletUtils.load for creating credentials
        Credentials credentials = Credentials.create("0x6e7e35018bfcb52cce3be032c466099e8ae46b2a066e1f8cc5a09c80e2106e63");

        Web3j web3j = Web3j.build(new HttpService("http://127.0.0.1:7545"));

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "network-1");
    }

    @Deployable(order = 3)
    public void deploy3(Deployer deployer) {
        try {
            List<String> accounts = deployer.getWeb3j().ethAccounts().send().getAccounts();
            Food food = Food.deploy(deployer.getWeb3j(), deployer.getTransactionManager(), deployer.getGasProvider()).send();
            System.out.println("3. Contract deployed at: " + food.getContractAddress());
            System.out.println("3. Total number of accounts found: " + accounts.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deployable(order = 2)
    public void deploy2(Deployer deployer) {
        try {
            List<String> accounts = deployer.getWeb3j().ethAccounts().send().getAccounts();
            SimpleStorage simpleStorage = SimpleStorage.load(deployedContractAddresses.get(0), deployer.getWeb3j(),
                    deployer.getTransactionManager(), deployer.getGasProvider());
//            SimpleStorage simpleStorage = SimpleStorage.deploy(deployer.getWeb3j(), deployer.getTransactionManager(), deployer.getGasProvider()).send();
            System.out.println("2. Contract deployed at: " + simpleStorage.getContractAddress());
            System.out.println("2. Total number of accounts found: " + accounts.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deployable(order = 1)
    public void deploy1(Deployer deployer) {
        try {
            List<String> accounts = deployer.getWeb3j().ethAccounts().send().getAccounts();
            SimpleStorage simpleStorage = SimpleStorage.deploy(deployer.getWeb3j(),
                    deployer.getTransactionManager(), deployer.getGasProvider()).send();
            String contractAddress = simpleStorage.getContractAddress();
            System.out.println("1. Contract deployed at: " + contractAddress);
            deployedContractAddresses.add(contractAddress);
            System.out.println("1. Total number of accounts found: " + accounts.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
