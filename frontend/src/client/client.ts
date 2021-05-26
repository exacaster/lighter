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

  private post<T>(url: string, data: T) {
    return this.client.post(url, data).then((resp) => resp.data);
  }

  // TODO: PAgination
  fetchBatches(): Promise<BatchPage> {
    return this.get('/api/batches');
  }

  fetchApplicationLog(id: string): Promise<ApplicationLog> {
    return this.get(`/api/batches/${id}/log`);
  }
}
