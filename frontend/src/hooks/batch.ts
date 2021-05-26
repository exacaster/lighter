import {useApi} from '../client/hooks';
import {useQuery} from 'react-query';

export function useBatches() {
  const api = useApi();
  return useQuery("batches", () => api.fetchBatches());
}

export function useApplicationLog(id: string) {
  const api = useApi();
  return useQuery(["logs", id], () => api.fetchApplicationLog(id));
}