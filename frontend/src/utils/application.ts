import {Application} from '../client/types';

export function getSparkSubmitArg(key: string, value: string) {
  return value ? ` ${key} ${value}` : null;
}

export function formatLink(template: string, app: Application) {
  return template
    .replaceAll('{{id}}', app.id)
    .replaceAll('{{appId}}', app.appId || '')
    .replace('{{createdTs}}', new Date(app.createdAt).getTime().toString());
}
