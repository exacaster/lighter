export type Application = {
  id: string;
  state: string;
  createdAt: string;
  contactedAt: string;
  appId?: string;
  submitParams: {
    name: string;
    file: string;
    numExecutors: number;
    executorCores: number;
    executorMemory: string;
    driverCores: number;
    driverMemory: string;
    args: string[];
    pyFiles: string[];
    archives: string[];
    files: string[];
    jars: string[];
    conf: Record<string, string>;
  };
};

export type ApplicationLog = {
  log: string;
};

export type BatchPage = {
  applications?: Application[];
  from: number;
  total: number;
};

export type Configuration = {
  sparkHistoryServerUrl?: string;
  externalLogsUrlTemplate?: string;
};
