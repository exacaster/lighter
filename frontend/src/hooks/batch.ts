import {useApi} from '../client/hooks';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';

export function useBatches(size: number, from: number, status?: string | null) {
  const api = useApi();
  return useQuery({queryKey: ['batches', size, from, status], queryFn: () => api.fetchBatches(size, from, status)});
}

export function useBatch(id: string) {
  const api = useApi();
  return useQuery({queryKey: ['batch_by_id', id], queryFn: () => api.fetchBatch(id)});
}

export function useBatchDelete() {
  const api = useApi();
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.deleteBatch(id),
    onSuccess: (_) =>
      client.refetchQueries({
        queryKey: ['batches'],
      }),
  });
}

export function useBatchLog(id: string) {
  const api = useApi();
  return useQuery({queryKey: ['logs', id], queryFn: () => api.fetchBatchLog(id)});
}
