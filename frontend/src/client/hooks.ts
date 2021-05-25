import axios from 'axios';
import {Api} from './client';

let api: Api | undefined = undefined;

export function useApi() {
  if (!api) {
    const client = axios.create({
      baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080',
      headers: {Accept: 'application/json'},
    });
    api = new Api(client);
  }

  return api;
}
