import {useQuery} from '@tanstack/react-query';
import {useApi} from '../client/hooks';

export function useConfiguration() {
  const api = useApi();
  return useQuery({queryKey: ['configuration'], queryFn: () => api.fetchConfiguration()});
}
