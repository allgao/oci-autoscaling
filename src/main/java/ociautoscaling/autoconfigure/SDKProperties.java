package ociautoscaling.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = {"classpath:application.properties"})
@ConfigurationProperties
public class SDKProperties {
    private String user;
    private String fingerprint;
    private String key_file;
    private String tenancy;
    private String region;
    private int connectiontimeout;
    private int readtimeout;
    private String compartment;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getKey_file() {
        return key_file;
    }

    public void setKey_file(String key_file) {
        this.key_file = key_file;
    }

    public String getTenancy() {
        return tenancy;
    }

    public void setTenancy(String tenancy) {
        this.tenancy = tenancy;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getConnectiontimeout() {
        return connectiontimeout;
    }

    public void setConnectiontimeout(int connectiontimeout) {
        this.connectiontimeout = connectiontimeout;
    }

    public int getReadtimeout() {
        return readtimeout;
    }

    public void setReadtimeout(int readtimeout) {
        this.readtimeout = readtimeout;
    }

    public String getCompartment() {
        return compartment;
    }

    public void setCompartment(String compartment) {
        this.compartment = compartment;
    }
}
