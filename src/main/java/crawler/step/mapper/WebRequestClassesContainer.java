package crawler.step.mapper;


import core.step.WebRequestStep;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WebRequestClassesContainer {

    private final Map<String, Class<?>> webRequestStepClasses;

    /* TODO finish classes container
    move it maybe to core
    add regex matcher to the name of this class e.g *RouterStep
    if class does not match regex throw exception
     */
    public WebRequestClassesContainer(String path) {

        webRequestStepClasses = new HashMap<>();

        Reflections reflections = new Reflections(path);

        Set<Class<? extends WebRequestStep>> webRequestClasses = reflections.getSubTypesOf(WebRequestStep.class);

        for (Class<? extends WebRequestStep> aClass : webRequestClasses) {
            System.out.println(aClass.getName());
//            if(aClass == ArrayList.class) {
//                List list = aClass.newInstance();
//                list.add("test");
//                System.out.println(list.getClass().getName() + ": " + list.size());
//            }
        }


    }
}
