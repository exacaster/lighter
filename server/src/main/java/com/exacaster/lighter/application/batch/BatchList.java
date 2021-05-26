package com.exacaster.lighter.application.batch;

import com.exacaster.lighter.application.Application;
import java.util.List;

public record BatchList(Integer from, Integer total, List<Application> applications) {

}
