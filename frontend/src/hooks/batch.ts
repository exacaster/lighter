import {useApi} from '../client/hooks';
import {useQuery} from 'react-query';

export function useBatches() {
  const api = useApi();
  return useQuery("batches", () => api.fetchBatches());
}
