package com.exacaster.lighter.storage;

import java.time.LocalDateTime;

public interface Entity {
    String getId();
    LocalDateTime getCreatedAt();
}
