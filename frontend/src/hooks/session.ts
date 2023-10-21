import {useApi} from '../client/hooks';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {SessionStatementCode} from '../client/types';

export function useSessions(size: number, from: number) {
  const api = useApi();
  return useQuery({
    queryKey: ['sessions', size, from],
    queryFn: () => api.fetchSessions(size, from),
  });
}

export function useSession(id: string) {
  const api = useApi();
  return useQuery({queryKey: ['session_by_id', id], queryFn: () => api.fetchSession(id)});
}

export function useSessionDelete() {
  const api = useApi();
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.deleteSession(id),
    onSuccess: () =>
      client.refetchQueries({
        queryKey: ['sessions'],
      }),
  });
}

export function useSessionLog(id: string) {
  const api = useApi();
  return useQuery({
    queryFn: () => api.fetchSessionLog(id),
    queryKey: ['logs', id],
  });
}

export function useStatements(sessionId: string, size: number, from: number) {
  const api = useApi();
  return useQuery({
    queryKey: ['sessions', sessionId, 'statements', size, from],
    queryFn: () => api.fetchSessionStatements(sessionId, size, from),
    refetchInterval: ({state}) => (state.data?.statements?.some((stmt) => stmt.state === 'waiting') ? 1000 : false),
  });
}

export function useSessionStatementSubmit(sessionId: string) {
  const api = useApi();
  const client = useQueryClient();

  return useMutation({
    mutationFn: (code: SessionStatementCode) => api.postSessionStatement(sessionId, code),
    onSuccess: () =>
      client.refetchQueries({
        queryKey: ['sessions', sessionId, 'statements'],
      }),
  });
}

export function useSessionStatementCancel(sessionId: string, statementId: string) {
  const api = useApi();
  const client = useQueryClient();

  return useMutation({
    mutationFn: () => api.cancelSessionStatement(sessionId, statementId),
    onSuccess: () =>
      client.refetchQueries({
        queryKey: ['sessions', sessionId, 'statements'],
      }),
  });
}
