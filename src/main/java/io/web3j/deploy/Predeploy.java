package io.web3j.deploy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Predeploy {
    String profile();
    String[] credentialKeys() default {};
    Web3jServiceType serviceType() default Web3jServiceType.HttpService;
    int ethFunds() default 1;
}
