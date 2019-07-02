package core.proxy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Bortnyk Roman on 25.11.2017.
 */

public class ProxiesImporter {

    private static final Logger logger = LoggerFactory.getLogger(ProxiesImporter.class);

    public static void main(String[] args) {
        int numberOfThreads = 50;

        List<ProxyAddress> proxiesListFromFile = importProxiesFromFile("proxiesToTest.txt");

        CopyOnWriteArraySet<ProxyAddress> passedTestProxies = new CopyOnWriteArraySet<>();

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (ProxyAddress proxyAddress : proxiesListFromFile) {
            ProxyTestThread proxyTestThread = new ProxyTestThread(proxyAddress, passedTestProxies, "TestThread " + proxyAddress.toString());

            executorService.execute(proxyTestThread);
        }

        executorService.shutdown();

        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {

            }
        }

        System.out.println();
        System.out.println("Tested proxies: " + proxiesListFromFile.size());

        int passedTestProxiesNumber = passedTestProxies.size();

        System.out.println("Passed test proxies / Failed test proxies : \n" + passedTestProxies.size() + " / " + (proxiesListFromFile.size() - passedTestProxiesNumber) + "\n");
        System.out.println("Passed test proxies");

        for (ProxyAddress passedTestProxyAddress : passedTestProxies) {
            System.out.println(passedTestProxyAddress.toString());
        }

    }

    public static List<ProxyAddress> importProxiesFromFile(String resourcePath) {
        Set<ProxyAddress> result = new HashSet<>();

        List<String> separators = Arrays.asList(":", "\t");

        try {
            Path path = Paths.get(ClassLoader.getSystemResource(resourcePath).getFile());
            Files.lines(path).forEach(lineWithProxy -> {

                String separator = separators.stream()
                        .filter(lineWithProxy::contains)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("File contains line with unknown separator"));

                String address = StringUtils.substringBefore(lineWithProxy, separator);
                int port = Integer.parseInt(StringUtils.substringAfter(lineWithProxy, separator).replaceAll("\\D+", ""));

                result.add(new ProxyAddress(address, port));
            });

        } catch (Exception e) {
            logger.error("Can not read proxies from file due to exception: " + e.getMessage());
        }

        logger.info(result.size() + " proxies were extracted from file");

        return new ArrayList<>(result);
    }
}
