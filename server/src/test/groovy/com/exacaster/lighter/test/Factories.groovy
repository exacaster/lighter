package com.exacaster.lighter.test

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationInfo
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationType
import com.exacaster.lighter.log.Log
import com.exacaster.lighter.spark.SubmitParams

import java.time.LocalDateTime

class Factories {
    static submitParams() {
        new SubmitParams(
                "application1",
                "",
                "",
                "",
                0,
                null, null, null, null, null, null, null, null, null
        )
    }

    static newApplication() {
        ApplicationBuilder.builder().setAppId("app123")
                .setType(ApplicationType.BATCH)
                .setState(ApplicationState.NOT_STARTED)
                .setAppInfo("info")
                .setCreatedAt(LocalDateTime.MAX)
                .setId("1")
                .setSubmitParams(null)
                .build()
    }

    static newSession() {
        ApplicationBuilder.builder().setAppId("app123")
                .setType(ApplicationType.SESSION)
                .setState(ApplicationState.NOT_STARTED)
                .setAppInfo("info")
                .setCreatedAt(LocalDateTime.MAX)
                .setId("1")
                .setSubmitParams(null)
                .build()
    }

    static applicationInfo(id) {
        new ApplicationInfo(ApplicationState.ERROR, id)
    }

    static logs(id) {
        new Log(id, "Error")
    }
}
