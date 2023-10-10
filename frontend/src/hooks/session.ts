import {useApi} from '../client/hooks';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {SessionStatementCode} from '../client/types';

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

export function useStatements(sessionId: string, size: number, from: number) {
  const api = useApi();
  return useQuery(['sessions', sessionId, 'statements', size, from], () => api.fetchSessionStatements(sessionId, size, from), {
    refetchInterval: (data) => (data?.statements?.some((stmt) => stmt.state === 'waiting') ? 1000 : false),
  });
}

export function useSessionStatementSubmit(sessionId: string) {
  const api = useApi();
  const client = useQueryClient();

  return useMutation((code: SessionStatementCode) => api.postSessionStatement(sessionId, code), {
    onSuccess: () => client.refetchQueries(['sessions', sessionId, 'statements']),
  });
}

export function useSessionStatementCancel(sessionId: string, statementId: string) {
  const api = useApi();
  const client = useQueryClient();

  return useMutation(() => api.cancelSessionStatement(sessionId, statementId), {
    onSuccess: () => client.refetchQueries(['sessions', sessionId, 'statements']),
  });
}
