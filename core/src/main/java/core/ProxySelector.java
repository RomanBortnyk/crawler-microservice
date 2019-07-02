package core;

import core.proxy.ProxyAddress;
import core.proxy.ProxiesImporter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ProxySelector {

    // TODO make dynamically update
    // TODO move proxies to db
    private final List<ProxyAddress> proxies;

    public ProxySelector() {
        this.proxies = ProxiesImporter.importProxiesFromFile("proxies.txt");
    }

    public ProxyAddress getRandomProxy() {
        ThreadLocalRandom localRandom = ThreadLocalRandom.current();
        int index = localRandom.nextInt(proxies.size());
        return proxies.get(index);
    }
}
