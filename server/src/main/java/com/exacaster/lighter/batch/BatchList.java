package com.exacaster.lighter.batch;

import com.exacaster.lighter.backend.Application;
import java.util.List;

public record BatchList(Integer from, Integer total, List<Application> applications) {

}
