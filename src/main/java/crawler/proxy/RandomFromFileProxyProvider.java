package crawler.proxy;

import core.proxy.ProxyAddress;
import core.proxy.ProxyProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RandomFromFileProxyProvider implements ProxyProvider{

    private final List<ProxyAddress> proxies;

    public RandomFromFileProxyProvider() {
        this.proxies = ProxiesImporter.importProxiesFromFile("proxies.txt");
    }

    @Override
    public ProxyAddress getNextProxy() {
        return getRandomProxy();
    }

    public ProxyAddress getRandomProxy() {
        ThreadLocalRandom localRandom = ThreadLocalRandom.current();
        int index = localRandom.nextInt(proxies.size());
        return proxies.get(index);
    }


}
