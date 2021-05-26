import {AxiosInstance} from 'axios';
import {ApplicationLog, BatchPage} from './types';

export class Api {
  client: AxiosInstance;

  constructor(client: AxiosInstance) {
    this.client = client;
  }

  private get(url: string) {
    return this.client.get(url).then((resp) => resp.data);
  }

  fetchBatches(size: number, from: number): Promise<BatchPage> {
    return this.get(`/api/batches?size=${size}&from=${from}`);
  }

  fetchApplicationLog(id: string): Promise<ApplicationLog> {
    return this.get(`/api/batches/${id}/log`);
  }
}
