package crawler;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@ComponentScan(basePackages = {"crawler"})
@ComponentScan
@EnableScheduling
public class SpringBootApp {

    public static void main(String[] args) {

        ApplicationContext ctx = SpringApplication.run(SpringBootApp.class, args);

//        QueryProvider bean = ctx.getBean(QueryProvider.class);
//
//        Map<String, String> mapToMongoWebRequestStep = new HashMap<>();
//        mapToMongoWebRequestStep.put("url", "https://spring.io/projects");
//        Query query = new Query("spring", Collections.<String>emptyList(), mapToMongoWebRequestStep);
//
//        bean.addToQueue(query);

    }

}
