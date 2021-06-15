import {useApi} from '../client/hooks';
import {useMutation, useQuery, useQueryClient} from 'react-query';

export function useBatches(size: number, from: number) {
  const api = useApi();
  return useQuery(['batches', size, from], () => api.fetchBatches(size, from));
}

export function useBatch(id: string) {
  const api = useApi();
  return useQuery(['batch_by_id', id], () => api.fetchBatch(id));
}

export function useBatchDelete() {
  const api = useApi();
  const client = useQueryClient();
  return useMutation((id: string) => api.deleteBatch(id), {onSuccess: (_) => client.refetchQueries(['batches'])});
}

export function useApplicationLog(id: string) {
  const api = useApi();
  return useQuery(['logs', id], () => api.fetchApplicationLog(id));
}
