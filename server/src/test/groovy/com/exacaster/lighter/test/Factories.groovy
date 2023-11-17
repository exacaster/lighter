package com.exacaster.lighter.test

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationType
import com.exacaster.lighter.application.sessions.Statement
import com.exacaster.lighter.application.sessions.processors.Output
import com.exacaster.lighter.backend.kubernetes.KubernetesProperties
import com.exacaster.lighter.configuration.AppConfiguration
import com.exacaster.lighter.application.SubmitParams

import java.time.Duration
import java.time.LocalDateTime

class Factories {
    static submitParams() {
        new SubmitParams(
                "application1",
                "",
                "",
                "",
                0,
                null, null, 1, null, false, null, null, null, null, null,
                ["lighter.local.env.FOO": "bar"]
        )
    }

    static newApplication(appId = "application_123_0123") {
        ApplicationBuilder.builder().setAppId(appId)
                .setType(ApplicationType.BATCH)
                .setState(ApplicationState.NOT_STARTED)
                .setAppInfo("info")
                .setCreatedAt(LocalDateTime.MAX)
                .setId("1")
                .setSubmitParams(submitParams())
                .build()
    }

    static newSession(state = ApplicationState.NOT_STARTED) {
        ApplicationBuilder.builder().setAppId("application_123_0124")
                .setType(ApplicationType.SESSION)
                .setState(state)
                .setAppInfo("info")
                .setCreatedAt(LocalDateTime.now())
                .setId("2")
                .setSubmitParams(null)
                .build()
    }

    static newPermanentSession(appId = "application_1") {
        ApplicationBuilder.builder().setAppId(appId)
                .setType(ApplicationType.PERMANENT_SESSION)
                .setState(ApplicationState.NOT_STARTED)
                .setAppInfo("info")
                .setCreatedAt(LocalDateTime.now())
                .setId("-1")
                .setSubmitParams(submitParams())
                .build()
    }

    static statement() {
        new Statement("id", "code", new Output("error", 1, [:], "evalue", "traceback"), "ok", null)
    }

    static kubernetesProperties() {
        new KubernetesProperties("spark", 500, "k8s://kubernetes.default.svc.cluster.local:443",
                "spark")
    }

    static appConfiguration() {
        new AppConfiguration(
                3,
                2,
                "http://history",
                null,
                5432,
                "http://lighter:8080",
                new AppConfiguration.SessionConfiguration(Duration.ofMinutes(20), false,
                        [new AppConfiguration.PermanentSession("permanentSessionId", submitParams())]
                        , Duration.ofMinutes(1), Duration.ofMinutes(2)),
                ["spark.kubernetes.driverEnv.TEST": "test"],
                ["spark.kubernetes.driverEnv.TEST": "test"]
        )
    }

    static newStatement() {
        new Statement(null, "code", null, null, LocalDateTime.now())
    }
}
