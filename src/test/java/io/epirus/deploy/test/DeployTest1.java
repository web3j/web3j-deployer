package io.epirus.deploy.test;

import io.epirus.deploy.DeployTools;
import io.epirus.deploy.Deployable;
import io.epirus.deploy.Deployer;
import io.epirus.deploy.Predeploy;
import org.junit.Assert;
import org.junit.Test;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.evm.Configuration;
import org.web3j.evm.EmbeddedWeb3jService;
import org.web3j.protocol.Web3j;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.util.LinkedList;
import java.util.List;

public class DeployTest1 {
    private final static List<String> EVENTS = new LinkedList<>();

    public DeployTest1() {
        EVENTS.add("constructor");
    }

    @Predeploy(network = "network-1")
    public Deployer deployable1() {
        Credentials credentials = Credentials.create("0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");

        Configuration configuration = new Configuration(new Address(credentials.getAddress()), 10);
        Web3j web3j = Web3j.build(new EmbeddedWeb3jService(configuration));

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "network-1");
    }

    @Predeploy(network = "network-2")
    public Deployer deployable2() {
        Credentials credentials = Credentials.create("0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");

        Configuration configuration = new Configuration(new Address(credentials.getAddress()), 10);
        Web3j web3j = Web3j.build(new EmbeddedWeb3jService(configuration));

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "network-2");
    }

    @Deployable(order = 3)
    public void deploy1(Deployer deployer) {
        EVENTS.add("deploy1: " + deployer.getNetwork());
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
        Deployer deployer = DeployTools.findDeployer("network-1", "io.epirus.deploy.test");
        Assert.assertEquals("network-1", deployer.getNetwork());
    }

    @Test
    public void findDeployerTest2() {
        Deployer deployer = DeployTools.findDeployer("network-2", "io.epirus.deploy.test");
        Assert.assertEquals("network-2", deployer.getNetwork());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDeployerTest3() {
        DeployTools.findDeployer("network-2", "foo.bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void findDeployerTest4() {
        DeployTools.findDeployer("network-3", "io.epirus.deploy.test");
    }

    @Test
    public void runDeploy1() {
        Deployer deployer = DeployTools.findDeployer("network-1", "io.epirus.deploy.test");

        EVENTS.clear();
        DeployTools.runDeployer(deployer, "io.epirus.deploy.test");
        Assert.assertEquals("[constructor, deploy3: network-1, deploy2: network-1, deploy1: network-1]", EVENTS.toString());
    }

    @Test
    public void runDeploy2() {
        Deployer deployer = DeployTools.findDeployer("network-2", "io.epirus.deploy.test");

        EVENTS.clear();
        DeployTools.runDeployer(deployer, "io.epirus.deploy.test");
        Assert.assertEquals("[constructor, deploy3: network-2, deploy2: network-2, deploy1: network-2]", EVENTS.toString());
    }

    @Test
    public void runDeploy3() {
        Deployer deployer = DeployTools.findDeployer("network-1", "io.epirus.deploy.test");

        EVENTS.clear();
        DeployTools.runDeployer(deployer, "foo.bar");
        Assert.assertEquals("[]", EVENTS.toString());
    }
}
