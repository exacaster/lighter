package com.exacaster.lighter.spark;

import static java.util.Optional.ofNullable;

import io.micronaut.core.annotation.Introspected;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.validation.constraints.NotBlank;

@Introspected
public class SubmitParams {

    private final String name;
    private final String file;
    private final String master;
    private final String mainClass;
    private final Integer numExecutors;
    private final Integer executorCores;
    private final String executorMemory;
    private final Integer driverCores;
    private final String driverMemory;
    private final List<String> args;
    private final List<String> pyFiles;
    private final List<String> files;
    private final List<String> jars;
    private final Map<String, String> conf;

    public SubmitParams(@NotBlank String name,
            @NotBlank String file,
            String master,
            String mainClass,
            Integer numExecutors,
            Integer executorCores,
            String executorMemory,
            Integer driverCores,
            String driverMemory,
            List<String> args,
            List<String> pyFiles,
            List<String> files,
            List<String> jars,
            Map<String, String> conf) {
        this.name = name;
        this.file = file;
        this.master = master;
        this.mainClass = mainClass;
        this.numExecutors = numExecutors;
        this.executorCores = executorCores;
        this.executorMemory = executorMemory;
        this.driverCores = driverCores;
        this.driverMemory = driverMemory;
        this.args = ofNullable(args).orElse(List.of());
        this.pyFiles = ofNullable(pyFiles).orElse(List.of());
        this.files = ofNullable(files).orElse(List.of());
        this.jars = ofNullable(jars).orElse(List.of());
        this.conf = ofNullable(conf).orElse(Map.of());
    }

    public SubmitParams withNameAndFile(String name, String file) {
        return new SubmitParams(name, file, master, mainClass, numExecutors, executorCores, executorMemory, driverCores,
                driverMemory, args, pyFiles, files, jars, conf);
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public String getMaster() {
        return master;
    }

    public String getMainClass() {
        return mainClass;
    }

    public Integer getNumExecutors() {
        return ofNullable(numExecutors).orElse(1);
    }

    public Integer getExecutorCores() {
        return ofNullable(executorCores).orElse(2);
    }

    public String getExecutorMemory() {
        return ofNullable(executorMemory).orElse("1000M");
    }

    public Integer getDriverCores() {
        return ofNullable(driverCores).orElse(2);
    }

    public String getDriverMemory() {
        return ofNullable(driverMemory).orElse("1000M");
    }

    public List<String> getArgs() {
        return args;
    }

    public List<String> getPyFiles() {
        return pyFiles;
    }

    public List<String> getFiles() {
        return files;
    }

    public List<String> getJars() {
        return jars;
    }

    public Map<String, String> getConf() {
        return conf;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SubmitParams.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("file='" + file + "'")
                .add("master='" + master + "'")
                .add("mainClass='" + mainClass + "'")
                .add("numExecutors=" + numExecutors)
                .add("executorCores=" + executorCores)
                .add("executorMemory='" + executorMemory + "'")
                .add("driverCores=" + driverCores)
                .add("driverMemory='" + driverMemory + "'")
                .add("args=" + args)
                .add("pyFiles=" + pyFiles)
                .add("files=" + files)
                .add("jars=" + jars)
                .add("conf=" + conf)
                .toString();
    }
}
