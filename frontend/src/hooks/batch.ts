import {useApi} from '../client/hooks';
import {useQuery} from 'react-query';

export function useBatches(size: number, from: number) {
  const api = useApi();
  return useQuery(['batches', size, from], () => api.fetchBatches(size, from));
}

export function useBatch(id: string) {
  const api = useApi();
  return useQuery(['batch', id], () => api.fetchBatch(id));
}

export function useApplicationLog(id: string) {
  const api = useApi();
  return useQuery(['logs', id], () => api.fetchApplicationLog(id));
}
