package com.exacaster.lighter.storage;

import com.exacaster.lighter.log.Log;
import java.util.Optional;

public interface LogStorage {
    Optional<Log> findApplicationLog(String internalApplicationId);
    Log saveApplicationLog(Log log);
}
