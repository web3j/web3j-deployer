package org.web3j.deploy.test;

import org.web3j.deploy.DeployTools;
import org.web3j.deploy.Deployable;
import org.web3j.deploy.Deployer;
import org.web3j.deploy.Predeploy;
import org.web3j.deploy.Web3jServiceType;
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
    private final static String ADMIN_PRIVATE_KEY = "0x6e7e35018bfcb52cce3be032c466099e8ae46b2a066e1f8cc5a09c80e2106e63";
    public DeployTest1() {
        EVENTS.add("constructor");
    }

    @Predeploy(profile = "network-1")
    public Deployer deployable1() {
        Method[] methods = DeployTest1.class.getMethods();
        Predeploy preDeployAnnotation = methods[0].getAnnotation(Predeploy.class);

        String network = preDeployAnnotation.profile();
        final Credentials credentials = Credentials.create(ADMIN_PRIVATE_KEY);
        final Web3j web3j = Web3j.build(new HttpService("http://127.0.0.1:7545"));

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "network-1");
    }

    // Deployment script
    @Predeploy(profile = "network-2")
    public Deployer deployable2() {
        Credentials credentials = Credentials.create(ADMIN_PRIVATE_KEY);

        Configuration configuration = new Configuration(new Address(credentials.getAddress()), 10);
        Web3j web3j = Web3j.build(new EmbeddedWeb3jService(configuration));

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "network-2");
    }

    @Deployable(order = 3)
    public void deploy1(Deployer deployer) {
        EVENTS.add("deploy1: " + deployer.getProfile());
//        deployer.getWeb3j().ethAccounts().send();
    }

    @Deployable(order = 2)
    public void deploy2(Deployer deployer) {
        EVENTS.add("deploy2: " + deployer.getProfile());
    }

    @Deployable(order = 1)
    public void deploy3(Deployer deployer) {
        EVENTS.add("deploy3: " + deployer.getProfile());
    }

    @Test
    public void findDeployerTest1() {
        Deployer deployer = DeployTools.findDeployer("network-1","org.web3j.deploy.test", classLoader);
        Assert.assertEquals("network-1", deployer.getProfile());
    }

    @Test
    public void findDeployerTest2() {
        Deployer deployer = DeployTools.findDeployer("network-2","org.web3j.deploy.test", classLoader);
        Assert.assertEquals("network-2", deployer.getProfile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDeployerTest3() {
        DeployTools.findDeployer("network-2", "foo.bar", classLoader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDeployerTest4() {
        DeployTools.findDeployer("network-3", "org.web3j.deploy.test", classLoader);
    }

    @Test
    public void runDeploy1() {
        Deployer deployer = DeployTools.findDeployer("network-1", "org.web3j.deploy.test", classLoader);

        EVENTS.clear();
        DeployTools.runDeployer(deployer, "org.web3j.deploy.test");
        Assert.assertEquals("[constructor, deploy3: network-1, deploy2: network-1, deploy1: network-1]", EVENTS.toString());
    }

    @Test
    public void runDeploy2() {
        Deployer deployer = DeployTools.findDeployer("network-2", "org.web3j.deploy.test", classLoader);

        EVENTS.clear();
        DeployTools.runDeployer(deployer, "org.web3j.deploy.test");
        Assert.assertEquals("[constructor, deploy3: network-2, deploy2: network-2, deploy1: network-2]", EVENTS.toString());
    }

    @Test
    public void runDeploy3() {
        Deployer deployer = DeployTools.findDeployer("network-1", "org.web3j.deploy.test", classLoader);

        EVENTS.clear();
        DeployTools.runDeployer(deployer, "foo.bar");
        Assert.assertEquals("[]", EVENTS.toString());
    }
}
