export function getSparkSubmitArg(key: string, value: string) {
  return value ? ` ${key} ${value}` : null;
}
