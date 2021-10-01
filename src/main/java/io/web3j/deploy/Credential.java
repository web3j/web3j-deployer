package io.web3j.deploy;

public class Credential {
    private final String privateKey;
    public final String publicKey;

    public Credential(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
}
