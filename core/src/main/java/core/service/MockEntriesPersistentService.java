package core.service;

import core.model.BaseEntry;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Log
@Getter
public class MockEntriesPersistentService implements EntriesPersistenceService{

    @Override
    public void persistResults(Collection<BaseEntry> entries) {
        log.info("persisted " + entries.size() + " results");
    }
}
