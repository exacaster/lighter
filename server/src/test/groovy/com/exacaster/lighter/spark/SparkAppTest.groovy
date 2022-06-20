package com.exacaster.lighter.spark

import org.apache.spark.launcher.SparkLauncher
import spock.lang.Specification

import static com.exacaster.lighter.test.Factories.submitParams

class SparkAppTest extends Specification {

    def "spark app"() {
        given:
        def params = submitParams()
        ConfigModifier modifier = {it -> it + ["spark.kubernetes.driverEnv.TEST1": "test1"]}
        def app = Spy(new SparkApp(params, this::error, [
                modifier
        ]))

        SparkLauncher launcherMock = Mock()
        app.prepareLauncher() >> launcherMock
        def result = [:]
        launcherMock.setConf(*_) >> {key, val -> result.put(key, val)}

        when:
        app.launch()

        then:
        result == [
                "spark.executor.memory":"1000M",
                "spark.driver.memory":"1000M",
                "spark.executor.cores":"1",
                "spark.driver.cores":"1",
                "spark.executor.instances":"0",
                "spark.kubernetes.driverEnv.TEST1":"test1"
        ]

    }

    def error(Throwable err) {

    }
}
