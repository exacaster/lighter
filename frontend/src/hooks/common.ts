import queryString from 'query-string';

export function toQueryString(params: Record<string, unknown>) {
  return '?' + queryString.stringify(params);
}
