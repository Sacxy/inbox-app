package io.sacxy.inbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;
@Configuration
@ConfigurationProperties(prefix = "datastax.astra")
@Primary
public class DataStaxAstraProperties {

    private File secureConnectBundle;


    public File getSecureConnectBundle() {
        return secureConnectBundle;
    }

    public void setSecureConnectBundle(File secureConnectBundle) {
        this.secureConnectBundle = secureConnectBundle;
    }
}
