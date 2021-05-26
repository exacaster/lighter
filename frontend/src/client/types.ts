
export type Application = {
  id: string;
  state: string;
};

export type ApplicationLog = {
  log: string;
};

export type BatchPage = {
  applications?: Application[];
  from: number;
  total: number;
};

