package io.web3j.deploy;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.util.List;

public class Deploy {

    @Predeploy(network = "network-1",
            credentialKeys = {"6e7e35018bfcb52cce3be032c466099e8ae46b2a066e1f8cc5a09c80e2106e63",},
            serviceType = Web3jServiceType.HttpService)
    public Deployer deployable2() {
        Credentials credentials = Credentials.create("0x6e7e35018bfcb52cce3be032c466099e8ae46b2a066e1f8cc5a09c80e2106e63");

        Web3j web3j = Web3j.build(new HttpService("http://127.0.0.1:7545"));

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "network-2");
    }

    @Deployable(order = 3)
    public void deploy3(Deployer deployer) {
        try {
            List<String> accounts = deployer.getWeb3j().ethAccounts().send().getAccounts();
            System.out.println("3. Total number of accounts found: " + accounts.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deployable(order = 2)
    public void deploy2(Deployer deployer) {
        try {
            List<String> accounts = deployer.getWeb3j().ethAccounts().send().getAccounts();
            System.out.println("2. Total number of accounts found: " + accounts.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deployable(order = 1)
    public void deploy1(Deployer deployer) {
        try {
            List<String> accounts = deployer.getWeb3j().ethAccounts().send().getAccounts();
            System.out.println("1. Total number of accounts found: " + accounts.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
