import {useApi} from '../client/hooks';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';

export function useBatches(size: number, from: number, status?: string | null) {
  const api = useApi();
  return useQuery(['batches', size, from, status], () => api.fetchBatches(size, from, status));
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

export function useBatchLog(id: string) {
  const api = useApi();
  return useQuery(['logs', id], () => api.fetchBatchLog(id));
}
