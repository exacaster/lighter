import {AxiosInstance} from 'axios';
import {Application, ApplicationLog, BatchPage, Configuration} from './types';

export class Api {
  client: AxiosInstance;

  constructor(client: AxiosInstance) {
    this.client = client;
  }

  private get(url: string) {
    return this.client.get(url).then((resp) => resp.data);
  }

  private delete(url: string) {
    return this.client.delete(url);
  }

  fetchBatches(size: number, from: number): Promise<BatchPage> {
    return this.get(`/api/batches?size=${size}&from=${from}`);
  }

  fetchBatch(id: string): Promise<Application> {
    return this.get(`/api/batches/${id}`);
  }

  deleteBatch(id: string) {
    return this.delete(`/api/batches/${id}`);
  }

  fetchApplicationLog(id: string): Promise<ApplicationLog> {
    return this.get(`/api/batches/${id}/log`);
  }

  fetchConfiguration(): Promise<Configuration> {
    return this.get('/api/configuration');
  }
}
