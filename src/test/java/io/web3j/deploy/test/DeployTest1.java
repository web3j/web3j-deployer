package io.web3j.deploy.test;

import io.web3j.deploy.DeployTools;
import io.web3j.deploy.Deployable;
import io.web3j.deploy.Deployer;
import io.web3j.deploy.Predeploy;
import io.web3j.deploy.Web3jServiceType;
import org.junit.Assert;
import org.junit.Test;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.evm.Configuration;
import org.web3j.evm.EmbeddedWeb3jService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class DeployTest1 {
    private final static List<String> EVENTS = new LinkedList<>();

    public DeployTest1() {
        EVENTS.add("constructor");
    }

    @Predeploy(profile = "network-1",
            credentialKeys = {"6e7e35018bfcb52cce3be032c466099e8ae46b2a066e1f8cc5a09c80e2106e63",},
            serviceType = Web3jServiceType.HttpService)
    public Deployer deployable1() {
        Method[] methods = DeployTest1.class.getMethods();
        Predeploy preDeployAnnotation = methods[0].getAnnotation(Predeploy.class);

        String network = preDeployAnnotation.profile();
        String[] credentialKeys = preDeployAnnotation.credentialKeys();
        System.out.println("My network: " + network);
        final Credentials credentials;
        if (credentialKeys.length == 1)  {
            credentials = Credentials.create(credentialKeys[0]);
        }
        else if (credentialKeys.length == 2) {
            credentials = Credentials.create(credentialKeys[0], credentialKeys[1]);
        } else  {
            throw new IllegalArgumentException("No credentials provided for the credentials");
        }

        final Web3j web3j;
        // Type of service also needs to be passed as a parameter?
        Configuration configuration = new Configuration(new Address(credentials.getAddress()), preDeployAnnotation.ethFunds());
        switch (preDeployAnnotation.serviceType())  {
            case HttpService:
                web3j = Web3j.build(new HttpService("http://127.0.0.1:7545"));
                break;
            case EmbeddedWeb3jService:
                web3j = Web3j.build(new EmbeddedWeb3jService(configuration));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + preDeployAnnotation.serviceType());
        }

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "network-1");
    }

    // Deployment script
    @Predeploy(profile = "network-2",
            credentialKeys = {"6e7e35018bfcb52cce3be032c466099e8ae46b2a066e1f8cc5a09c80e2106e63",},
            serviceType = Web3jServiceType.HttpService)
    public Deployer deployable2() {
        Credentials credentials = Credentials.create("0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");

        Configuration configuration = new Configuration(new Address(credentials.getAddress()), 10);
        Web3j web3j = Web3j.build(new EmbeddedWeb3jService(configuration));

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "network-2");
    }

    @Deployable(order = 3)
    public void deploy1(Deployer deployer) {
        EVENTS.add("deploy1: " + deployer.getNetwork());
//        deployer.getWeb3j().ethAccounts().send();
    }

    @Deployable(order = 2)
    public void deploy2(Deployer deployer) {
        EVENTS.add("deploy2: " + deployer.getNetwork());
    }

    @Deployable(order = 1)
    public void deploy3(Deployer deployer) {
        EVENTS.add("deploy3: " + deployer.getNetwork());
    }

    @Test
    public void findDeployerTest1() {
        Deployer deployer = DeployTools.findDeployer("network-1","io.web3j.deploy.test");
        Assert.assertEquals("network-1", deployer.getNetwork());
    }

    @Test
    public void findDeployerTest2() {
        Deployer deployer = DeployTools.findDeployer("network-2","io.web3j.deploy.test");
        Assert.assertEquals("network-2", deployer.getNetwork());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDeployerTest3() {
        DeployTools.findDeployer("network-2", "foo.bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDeployerTest4() {
        DeployTools.findDeployer("network-3", "io.web3j.deploy.test");
    }

    @Test
    public void runDeploy1() {
        Deployer deployer = DeployTools.findDeployer("network-1", "io.web3j.deploy.test");

        EVENTS.clear();
        DeployTools.runDeployer(deployer, "io.web3j.deploy.test");
        Assert.assertEquals("[constructor, deploy3: network-1, deploy2: network-1, deploy1: network-1]", EVENTS.toString());
    }

    @Test
    public void runDeploy2() {
        Deployer deployer = DeployTools.findDeployer("network-2", "io.web3j.deploy.test");

        EVENTS.clear();
        DeployTools.runDeployer(deployer, "io.web3j.deploy.test");
        Assert.assertEquals("[constructor, deploy3: network-2, deploy2: network-2, deploy1: network-2]", EVENTS.toString());
    }

    @Test
    public void runDeploy3() {
        Deployer deployer = DeployTools.findDeployer("network-1", "io.web3j.deploy.test");

        EVENTS.clear();
        DeployTools.runDeployer(deployer, "foo.bar");
        Assert.assertEquals("[]", EVENTS.toString());
    }
}
