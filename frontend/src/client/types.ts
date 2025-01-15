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

export type SessionStatementCode = {
  code: string;
};

export type SessionStatement = SessionStatementCode & {
  id: string;
  state: 'available' | 'error' | 'waiting' | 'to_cancel' | 'canceled';
  output?: {
    status: 'ok' | 'error';
    traceback?: string;
    data: Record<string, unknown>;
  };
};

export type SessionStatementPage = {
  statements: SessionStatement[];
};
