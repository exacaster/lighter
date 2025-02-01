import {Box, Table} from '@chakra-ui/react';
import React from 'react';
import {Application} from '../client/types';
import styles from './AppInfo.module.scss';

interface Props {
  app: Application;
}

const AppInfo: React.FC<Props> = ({app}) => {
  return (
    <Box mt="5" className={styles.appInfo}>
      <Table.Root className={styles.noWrapTable} size="sm">
        <Table.Header>
          <Table.Row>
            <Table.ColumnHeader>Property</Table.ColumnHeader>
            <Table.ColumnHeader>Value</Table.ColumnHeader>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          <Table.Row>
            <Table.Cell>File</Table.Cell>
            <Table.Cell>{app.submitParams.file}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Args</Table.Cell>
            <Table.Cell>{app.submitParams.args.join(', ')}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Name (--name)</Table.Cell>
            <Table.Cell>{app.submitParams.name}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Driver Cores (--driver-cores)</Table.Cell>
            <Table.Cell>{app.submitParams.driverCores}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Driver Memory (--driver-memory)</Table.Cell>
            <Table.Cell>{app.submitParams.driverMemory}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Number Of Executors (--num-executors)</Table.Cell>
            <Table.Cell>{app.submitParams.numExecutors}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Executor Cores (--executor-cores)</Table.Cell>
            <Table.Cell>{app.submitParams.executorCores}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Executor Memory (--executor-memory)</Table.Cell>
            <Table.Cell>{app.submitParams.executorMemory}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Python files (--py-files)</Table.Cell>
            <Table.Cell>{app.submitParams.pyFiles.join(', ')}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Archives (--archives)</Table.Cell>
            <Table.Cell>{app.submitParams.archives.join(', ')}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Additional files (--files)</Table.Cell>
            <Table.Cell>{app.submitParams.files.join(', ')}</Table.Cell>
          </Table.Row>
          <Table.Row>
            <Table.Cell>Additional JARs (--jars)</Table.Cell>
            <Table.Cell>{app.submitParams.jars.join(', ')}</Table.Cell>
          </Table.Row>
          {Object.entries(app.submitParams.conf).map(([name, val]) => (
            <Table.Row key={name}>
              <Table.Cell>--conf {name}</Table.Cell>
              <Table.Cell>{val}</Table.Cell>
            </Table.Row>
          ))}
        </Table.Body>
      </Table.Root>
    </Box>
  );
};

export default AppInfo;
