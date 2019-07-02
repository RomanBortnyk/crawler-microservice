package core.proxy;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by Bortnyk Roman on 28.11.2017.
 */
@Getter
public class ProxyAddress {

    private final String ipAddress;
    private final Integer port;

    public ProxyAddress(String ipAddress, Integer port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public String toString() {
        return ipAddress +"\t"+port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyAddress)) return false;
        ProxyAddress proxyAddress = (ProxyAddress) o;
        return Objects.equals(getIpAddress(), proxyAddress.getIpAddress()) &&
                Objects.equals(getPort(), proxyAddress.getPort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIpAddress(), getPort());
    }
}
