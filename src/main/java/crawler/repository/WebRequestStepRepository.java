package crawler.repository;

import crawler.step.model.MongoWebRequestStep;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WebRequestStepRepository extends MongoRepository<MongoWebRequestStep, String> {

    void deleteByIdIn(List<String> completedStepsIds);
}
