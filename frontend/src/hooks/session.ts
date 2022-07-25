import {useApi} from '../client/hooks';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';

export function useSessions(size: number, from: number) {
  const api = useApi();
  return useQuery(['sessions', size, from], () => api.fetchSessions(size, from));
}

export function useSession(id: string) {
  const api = useApi();
  return useQuery(['session_by_id', id], () => api.fetchSession(id));
}

export function useSessionDelete() {
  const api = useApi();
  const client = useQueryClient();
  return useMutation((id: string) => api.deleteSession(id), {onSuccess: (_) => client.refetchQueries(['sessions'])});
}

export function useSessionLog(id: string) {
  const api = useApi();
  return useQuery(['logs', id], () => api.fetchSessionLog(id));
}
