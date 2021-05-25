
export type Batch = {
  id: string;
  state: string;
};

export type BatchPage = {
  batches?: Batch[];
  from: number;
  total: number;
};

