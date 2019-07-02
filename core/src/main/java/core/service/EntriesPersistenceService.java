package core.service;

import core.model.BaseEntry;

import java.util.Collection;

public interface EntriesPersistenceService {

    void persistResults(Collection<BaseEntry> entries);

}
