package com.exacaster.lighter.application;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.MapFormat;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static io.micronaut.core.convert.format.MapFormat.MapTransformation.FLAT;
import static io.micronaut.core.naming.conventions.StringConvention.RAW;
import static java.util.Optional.ofNullable;

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
    private final List<String> archives;
    private final Map<String, String> conf;

    public SubmitParams(@Nullable String name,
            @Nullable String file,
            @Nullable String master,
            @Nullable String mainClass,
            @Nullable Integer numExecutors,
            @Nullable Integer executorCores,
            @Nullable String executorMemory,
            @Nullable Integer driverCores,
            @Nullable String driverMemory,
            @Nullable List<String> args,
            @Nullable List<String> pyFiles,
            @Nullable List<String> files,
            @Nullable List<String> jars,
            @Nullable List<String> archives,
            @MapFormat(transformation = FLAT, keyFormat = RAW)
            @Nullable Map<String, String> conf) {
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
        this.archives = ofNullable(archives).orElse(List.of());
        this.conf = ofNullable(conf).orElse(Map.of());
    }

    public SubmitParams withNameAndFile(String name, String file) {
        return new SubmitParams(name, file, master, mainClass, numExecutors, executorCores, executorMemory, driverCores,
                driverMemory, args, pyFiles, files, jars, archives, conf);
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
        return ofNullable(executorCores).orElse(1);
    }

    public String getExecutorMemory() {
        return ofNullable(executorMemory).orElse("1000M");
    }

    public Integer getDriverCores() {
        return ofNullable(driverCores).orElse(1);
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

    public List<String> getArchives() {
        return archives;
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
                .add("archives=" + archives)
                .add("conf=" + conf)
                .toString();
    }

}
