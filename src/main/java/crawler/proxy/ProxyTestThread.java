package crawler.proxy;

import core.proxy.ProxyAddress;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Bortnyk Roman on 28.11.2017.
 */
public class ProxyTestThread implements Runnable {

    private String name;
    private CopyOnWriteArraySet<ProxyAddress> passedTestProxies;
    private ProxyAddress proxyAddressToTest;

    private static AtomicInteger counter = new AtomicInteger(0);

    public ProxyTestThread(ProxyAddress proxyAddressToTest, CopyOnWriteArraySet<ProxyAddress> passedTestProxies, String name) {
        this.proxyAddressToTest = proxyAddressToTest;
        this.passedTestProxies = passedTestProxies;
        this.name = name;
    }

    @Override
    public void run() {

        final int timeout = 8000;

            String url = "https://www.amazon.com/GE-RPWFE-Refrigerator-Water-Filter/dp/B009PCI2JU/ref=zg_bs_appliances_1?_encoding=UTF8&psc=1&refRID=QJSZDA9S1RPX36J68THY";
            String referer = "https://www.amazon.com/Best-Sellers-Appliances/zgbs/appliances/ref=zg_bs_nav_0";
            String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36";

            Connection.Response response = null;

            try {
                response = Jsoup.connect(url)
                        .proxy(proxyAddressToTest.getIpAddress(), proxyAddressToTest.getPort())
                        .userAgent(userAgent)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Encoding", "gzip, deflate, sdch, br")
                        .header("Accept-Language", "en-US,en;q=0.8,ru;q=0.6")
                        .header("Connection", "keep-alive")
                        .header("Host", "www.amazon.com")
                        .header("Referer", referer)
                        .timeout(timeout)
                        .execute();

                if (response != null) {
                    int responseCode = response.statusCode();

                    if (responseCode == 200) {
                        System.out.println(counter.addAndGet(1) + ". ProxyAddress: " + proxyAddressToTest.toString() + " passed test " + "with code " + responseCode);
                        passedTestProxies.add(proxyAddressToTest);

                    } else {
                        System.out.println(counter.addAndGet(1) + ". ProxyAddress: " + proxyAddressToTest.toString() + " failed test " + "with code " + responseCode);
                    }
                } else {
                    System.out.println(counter.addAndGet(1) + ". ProxyAddress: " + proxyAddressToTest.toString() + " failed test " + "with null response");
                }

            } catch (Exception e) {
                System.out.println(counter.addAndGet(1) + ". ProxyAddress: " + proxyAddressToTest.toString() + " failed test " + "with exception");
            }
        }
}

