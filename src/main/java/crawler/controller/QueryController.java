package crawler.controller;

import core.Query;
import core.QueryProvider;
import crawler.dto.QueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController(value = "/api")
@RequiredArgsConstructor
public class QueryController {

    private final QueryProvider queryProvider;

    @PostMapping(path = "/query")
    public boolean createQuery(@Valid @RequestBody QueryDto queryDto){

        Query query = new Query(queryDto.getParserName(), queryDto.getModes(), queryDto.getParameters());

        return queryProvider.addToQueue(query);
    }

    @PostMapping(path = "/test")
    public void createQueryTest(){

    }



}
