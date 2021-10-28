package org.web3j.deploy.test;

import org.junit.Assert;
import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.deploy.DeployTools;
import org.web3j.deploy.Deployable;
import org.web3j.deploy.Deployer;
import org.web3j.deploy.Predeploy;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.util.LinkedList;
import java.util.List;

public class DeployTest1 {
    private final static List<String> EVENTS = new LinkedList<>();

    public DeployTest1() {
        EVENTS.add("constructor");
    }

    @Predeploy(profile = "profile-1")
    public Deployer deployable1() {
        Credentials credentials = Credentials.create("0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");

        Web3j web3j = Web3j.build(new HttpService(""));

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "profile-1");
    }

    @Predeploy(profile = "profile-2")
    public Deployer deployable2() {
        Credentials credentials = Credentials.create("0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");

        Web3j web3j = Web3j.build(new HttpService(""));

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "profile-2");
    }

    @Deployable(order = 3)
    public void deploy1(Deployer deployer) {
        EVENTS.add("deploy1: " + deployer.getProfile());
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
        Deployer deployer = new DeployTools().findDeployer("profile-1", "org.web3j.deploy.test");
        Assert.assertEquals("profile-1", deployer.getProfile());
    }

    @Test
    public void findDeployerTest2() {
        Deployer deployer = new DeployTools().findDeployer("profile-2", "org.web3j.deploy.test");
        Assert.assertEquals("profile-2", deployer.getProfile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDeployerTest3() {
        new DeployTools().findDeployer("profile-2", "foo.bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDeployerTest4() {
        new DeployTools().findDeployer("profile-3", "org.web3j.deploy.test");
    }

    @Test
    public void runDeploy1() {
        Deployer deployer = new DeployTools().findDeployer("profile-1", "org.web3j.deploy.test");

        EVENTS.clear();
        new DeployTools().runDeployer(deployer, "org.web3j.deploy.test");
        Assert.assertEquals("[constructor, deploy3: profile-1, deploy2: profile-1, deploy1: profile-1]", EVENTS.toString());
    }

    @Test
    public void runDeploy2() {
        Deployer deployer = new DeployTools().findDeployer("profile-2", "org.web3j.deploy.test");

        EVENTS.clear();
        new DeployTools().runDeployer(deployer, "org.web3j.deploy.test");
        Assert.assertEquals("[constructor, deploy3: profile-2, deploy2: profile-2, deploy1: profile-2]", EVENTS.toString());
    }

    @Test
    public void runDeploy3() {
        Deployer deployer = new DeployTools().findDeployer("profile-1", "org.web3j.deploy.test");

        EVENTS.clear();
        new DeployTools().runDeployer(deployer, "foo.bar");
        Assert.assertEquals("[]", EVENTS.toString());
    }
}