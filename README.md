# Archive Notice

> This project is no longer under active development and has been archived. The repository is kept for historical purposes and read-only access. No further updates or pull requests will be considered.

# Web3j Deployer

A simple and lightweight library for managing deployments of Ethereum contracts and transactions.

Library is intended used by the Web3j CLI, but may also be used on its own.

Through the use of two annotations, `@Predeploy` and `@Deployable`, developers can define deploy configurations and
deployment methods.

Let's start by looking at `@Predeploy` and how this is used to define a Deployer instance for a given network

```java
package com.example.deployers

@Predeploy(profile = "my-deploy-profile")
public Deployer deployMyContract(){
    Credentials credentials = ...;
    Web3j web3j = Web3j.build(...);
    return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "my-deploy-profile");
}
```

The profile name can be defined to whatever makes sense to you. Here we use `my-deploy-profile` as an example label.

Assuming you defined this within a package called `com.example.deployers`, you could obtain a reference to this deployer
using the `DeployTools`:

```java
String profile = "my-deploy-profile";
String pkgFilter = "com.example";
Deployer deployer = new DeployTools().findDeployer(profile, pkgFilter);
```

Next you want to define methods that allows you to use the deployer. Often, for complex contract setups, you have
multiple steps during a deployment process. Using the `@Deployable` annotation you can define the order you want them to
execute, starting low:

```java
package com.example.deploy

@Deployable(order = 1)
public void deployA(Deployer deployer){
    ...
}

@Deployable(order = 2)
public void deployB(Deployer deployer){
    ...
}

@Deployable(order = 3)
public void deployC(Deployer deployer){
    ...
}
```

When using `runDeployer` from `DeployTools`, it will execute these as `deployA`, `deployB`, and finally `deployC` due to
the `order` value on each of the `@Deployable` annotations. Execute them like so:

```java
public static void main(String...args){
    String profile = "my-deploy-profile";
    String pkgFilter = "com.example";
    DeployTools deployTools = new DeployTools();
    Deployer deployer = deployTools.findDeployer(network, pkgFilter);
    deployTools.runDeployer(deployer, pkgFilter);
}
```

## Gradle deploy task

If you include the [web3j-deployer-plugin](https://github.com/web3j/web3j-deployer-plugin) in your project as a plugin,
you can use the deploy task to deploy your contracts like so:

```bash
./gradlew deploy -Pprofile=my-profile -Ppackage=org.testing
```

To add this as a plugin to your gradle project, in build.gradle include, the below replaing x.y.z with the current
version:

```gradle
plugins {
    id 'org.web3j.deploy' version 'x.y.z'
}
```

## Example repository

A simple demo example is made available within [web3j-deployer-demo](https://github.com/web3j/web3j-deployer-demo)
showing how the annotations can be used to deploy and configure an actual contract.
