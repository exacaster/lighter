package com.exacaster.lighter.application.sessions.processors

import spock.lang.Ignore
import spock.lang.Specification

import static com.exacaster.lighter.application.sessions.processors.Result.TEXT_PLAIN

@Ignore
class PythonProcessorTest extends Specification {
    def process
    def processor

    def setup() {
        def builder = new ProcessBuilder("python3", "/home/paulius/projects/lighter/server/src/main/resources/shell_wrapper.py")
        builder.redirectErrorStream(true)
        def env = builder.environment()
        env.put("PYTHONINSPECT", "true")
        env.put("PYTHONUNBUFFERED", "true")
        env.put("LIGHTER_TEST", "true")
        process = builder.start()
        processor = new PythonProcessor(process)
    }

    def cleanup() {
        process.destroy()
        process.waitFor()
    }

    def "evaluates 1 + 2"() {
        when:
        def result = processor.process("1 + 2")

        then:
        result.content[TEXT_PLAIN] == "3"
    }

    def "execute multiple statements"() {
        when:
        def result = processor.process("x = 1")

        then:
        result.content[TEXT_PLAIN] == ""

        when:
        result = processor.process("y = 2")

        then:
        result.content[TEXT_PLAIN] == ""

        when:
        result = processor.process("x + y")

        then:
        result.content[TEXT_PLAIN] == "3"
    }

    def "execute multiple statements in one block"() {
        when:
        def result = processor.process("""x = 1
            |
            |y = 2
            |
            |x + y""".stripMargin().stripIndent())
        then:
        result.content[TEXT_PLAIN] == "3"
    }

    def "get multiple outputs in one block"() {
        when:
        def result = processor.process("""
            |print("1")
            |print("2")
        """.stripMargin().stripIndent())
        then:
        result.content[TEXT_PLAIN] == "1\n2"
    }

    def "parse a class"() {
        when:
        def result = processor.process("""
        |class Counter(object):
        |   def __init__(self):
        |       self.count = 0
        |
        |   def add_one(self):
        |       self.count += 1
        |
        |   def add_two(self):
        |       self.count += 2
        |
        |counter = Counter()
        |counter.add_one()
        |counter.add_two()
        |counter.count
        """.stripMargin().stripIndent())
        then:
        result.content[TEXT_PLAIN] == "3"
    }

    def "report an error if accessing an unknown variable"() {
        when:
        def result = processor.process("x")
        then:
        result.error == "NameError"
        result.message == "name 'x' is not defined"
        result.traceback.size() > 0
    }

}
