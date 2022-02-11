import {Code} from '@chakra-ui/react';
import React from 'react';
import {Application} from '../client/types';
import {getSparkSubmitArg} from '../utils/application';
import styles from './AppSubmit.module.scss';

interface Props {
  app: Application;
}

const AppSubmit: React.FC<Props> = ({app}) => {
  return (
    <Code className={styles.command}>
      spark-submit
      {getSparkSubmitArg('--name', app.submitParams.name)}
      {getSparkSubmitArg('--driver-cores', app.submitParams.driverCores.toString())}
      {getSparkSubmitArg('--driver-memory', app.submitParams.driverMemory)}
      {getSparkSubmitArg('--num-executors', app.submitParams.numExecutors.toString())}
      {getSparkSubmitArg('--executor-cores', app.submitParams.executorCores.toString())}
      {getSparkSubmitArg('--executor-memory', app.submitParams.executorMemory)}
      {getSparkSubmitArg('--py-files', app.submitParams.pyFiles.join(','))}
      {getSparkSubmitArg('--archives', app.submitParams.archives.join(','))}
      {getSparkSubmitArg('--files', app.submitParams.files.join(','))}
      {getSparkSubmitArg('--jars', app.submitParams.jars.join(','))}
      {Object.entries(app.submitParams.conf).map(([name, val]) => (
        <span key={name}>
          {' '}
          --conf {name} {val}
        </span>
      ))}
      {' ' + app.submitParams.file}
      {' ' + app.submitParams.args.join(' ')}
    </Code>
  );
};

export default AppSubmit;
