# Epirus Deployer

A simple and lightweight library for managing deployments of Ethereum contracts and transactions.

Library is intended used by the Epirus CLI, but may also be used on it's own. It is still somewhat WIP, and not yet released.

Through the use of two annotations, `@Predeploy` and `@Deployable`, developers can define deploy configurations and deployment methods.

Let's start by looking at `@Predeploy` and how this is used to define a Deployer instance for a given network

```java
    @Predeploy(network = "rinkeby")
    public Deployer deployRinkeby() {
        Credentials credentials = ...;

        Web3j web3j = Web3j.build(...);

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "rinkeby");
    }
```

The network name can be defined to whatever makes sense to you. Here we use `rinkeby` as an example label.

Assuming you defined this within a package called `com.example.deployers`, you could obtain a reference to this deployer using the `DeployTools`:

```java
	String network = "rinkeby";
	String pkgFilter = "com.example"
	Deployer deployer = DeployTools.findDeployer(network, pkgFilter);
```

Next you want to define methods that allows you to use the deployer. Often, for complex contract setups, you have multiple steps during a deployment process. Using the `@Deployable` annotation you can define the order you want them to execute, starting low:

```java
    @Deployable(order = 3)
    public void deployA(Deployer deployer) {
    	...
    }

    @Deployable(order = 2)
    public void deployB(Deployer deployer) {
    	...
    }

    @Deployable(order = 1)
    public void deployC(Deployer deployer) {
    	...
    }
```

When using `runDeployer` from `DeployTools`, it will execute these as `deployC`, `deployB`, and finally `deployA` due to the `order` value on each of the `@Deployable` annotations. Execute them like so:

```java
	public static void main(String... args) {
		String network = "rinkeby";
		String pkgFilter = "com.example"
		Deployer deployer = DeployTools.findDeployer(network, pkgFilter);
	    DeployTools.runDeployer(deployer, pkgFilter);
	}
```
