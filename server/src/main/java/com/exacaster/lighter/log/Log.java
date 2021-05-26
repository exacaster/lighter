package com.exacaster.lighter.log;

import com.exacaster.lighter.storage.Entity;

public record Log(String id, String log) implements Entity {

}
