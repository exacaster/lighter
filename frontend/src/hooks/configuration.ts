import {useQuery} from '@tanstack/react-query';
import {useApi} from '../client/hooks';

export function useConfiguration() {
  const api = useApi();
  return useQuery(['configuration'], () => api.fetchConfiguration());
}
