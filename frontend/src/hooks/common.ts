import {useLocation} from 'react-router';
import queryString from 'query-string';

export function useQueryString() {
  const {search} = useLocation();
  return queryString.parse(search);
}

export function toQueryString(params: Record<string, unknown>) {
  return '?' + queryString.stringify(params);
}
