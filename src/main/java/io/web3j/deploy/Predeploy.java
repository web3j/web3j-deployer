package io.web3j.deploy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Predeploy {
    String network();
    String[] credentialKeys();
    Web3jServiceType serviceType();
    int ethFunds() default 1;
}
