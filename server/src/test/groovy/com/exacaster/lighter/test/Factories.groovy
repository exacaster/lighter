package com.exacaster.lighter.test

import com.exacaster.lighter.application.ApplicationBuilder
import com.exacaster.lighter.application.ApplicationInfo
import com.exacaster.lighter.application.ApplicationState
import com.exacaster.lighter.application.ApplicationType
import com.exacaster.lighter.application.sessions.Statement
import com.exacaster.lighter.application.sessions.processors.Output
import com.exacaster.lighter.backend.kubernetes.KubernetesProperties
import com.exacaster.lighter.configuration.AppConfiguration
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

    static newSession(state=ApplicationState.NOT_STARTED) {
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

    static statement() {
        new Statement("id", "code", new Output("error", 1, [:]), "ok")
    }

    static kubernetesProperties() {
        new KubernetesProperties("spark", 500, "k8s://kubernetes.default.svc.cluster.local:443",
                ["spark.kubernetes.namespace": "spark"])
    }

    static appConfiguration() {
        new AppConfiguration(10, "http://history", 5432, "http://lighter:8080", new AppConfiguration.SessionConfiguration(20))
    }
}
