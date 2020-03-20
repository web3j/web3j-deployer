package io.epirus.deploy.test;

import io.epirus.deploy.DeployToolsKt;
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

public class DeployTest1 {
    @Predeploy(network = "deploy-test-1")
    public Deployer deployable() {
        Credentials credentials = Credentials.create("0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");

        Configuration configuration = new Configuration(new Address(credentials.getAddress()), 10);
        Web3j web3j = Web3j.build(new EmbeddedWeb3jService(configuration));

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "deploy-test-1");
    }

    @Deployable(order = 0)
    public void deploy1() {}

    @Deployable(order = 1)
    public void deploy2() {}

    @Deployable(order = 2)
    public void deploy3() {}

    @Test
    public void findDeployerTest1() {
        Deployer deployer = DeployToolsKt.findDeployer("deploy-test-1", "io.epirus.deploy.test");

        Assert.assertEquals("deploy-test-1", deployer.getNetwork());
    }
}
