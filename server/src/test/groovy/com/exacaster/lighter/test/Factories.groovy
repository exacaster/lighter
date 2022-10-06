package com.exacaster.lighter.test

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationType
import com.exacaster.lighter.application.sessions.Statement
import com.exacaster.lighter.application.sessions.processors.Output
import com.exacaster.lighter.backend.kubernetes.KubernetesProperties
import com.exacaster.lighter.configuration.AppConfiguration
import com.exacaster.lighter.application.SubmitParams

import java.time.LocalDateTime

class Factories {
    static submitParams() {
        new SubmitParams(
                "application1",
                "",
                "",
                "",
                0,
                null, null, 1, null, null, null, null, null, null, null
        )
    }

    static newApplication(appId = "application_123_0123") {
        ApplicationBuilder.builder().setAppId(appId)
                .setType(ApplicationType.BATCH)
                .setState(ApplicationState.NOT_STARTED)
                .setAppInfo("info")
                .setCreatedAt(LocalDateTime.MAX)
                .setId("1")
                .setSubmitParams(null)
                .build()
    }

    static newSession(state = ApplicationState.NOT_STARTED) {
        ApplicationBuilder.builder().setAppId("application_123_0124")
                .setType(ApplicationType.SESSION)
                .setState(ApplicationState.NOT_STARTED)
                .setAppInfo("info")
                .setCreatedAt(LocalDateTime.MAX)
                .setId("2")
                .setSubmitParams(null)
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
                10,
                "http://history",
                null,
                5432,
                "http://lighter:8080",
                new AppConfiguration.SessionConfiguration(20, [new AppConfiguration.PermanentSession("permanentSessionId", submitParams())]),
                ["spark.kubernetes.driverEnv.TEST": "test"],
                ["spark.kubernetes.driverEnv.TEST": "test"]
        )
    }
}
