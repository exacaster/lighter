import axios from 'axios';
import {Api} from './client';

let api: Api | undefined = undefined;

export function useApi() {
  if (!api) {
    const client = axios.create({
      baseURL: import.meta.env.APP_BASE_URL !== undefined ? import.meta.env.APP_BASE_URL : '/lighter',
      headers: {Accept: 'application/json'},
    });
    api = new Api(client);
  }

  return api;
}
