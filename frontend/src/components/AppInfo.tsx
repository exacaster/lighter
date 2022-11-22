import {Box, Table, Tbody, Td, Th, Thead, Tr} from '@chakra-ui/react';
import React from 'react';
import {Application} from '../client/types';
import styles from './AppInfo.module.scss';

interface Props {
  app: Application;
}

const AppInfo: React.FC<Props> = ({app}) => {
  return (
    <Box mt="5" className={styles.appInfo}>
      <Table className={styles.noWrapTable} variant="simple" size="sm">
        <Thead>
          <Tr>
            <Th>Property</Th>
            <Th>Value</Th>
          </Tr>
        </Thead>
        <Tbody>
          <Tr>
            <Td>File</Td>
            <Td>{app.submitParams.file}</Td>
          </Tr>
          <Tr>
            <Td>Args</Td>
            <Td>{app.submitParams.args.join(', ')}</Td>
          </Tr>
          <Tr>
            <Td>Name (--name)</Td>
            <Td>{app.submitParams.name}</Td>
          </Tr>
          <Tr>
            <Td>Driver Cores (--driver-cores)</Td>
            <Td>{app.submitParams.driverCores}</Td>
          </Tr>
          <Tr>
            <Td>Driver Memory (--driver-memory)</Td>
            <Td>{app.submitParams.driverMemory}</Td>
          </Tr>
          <Tr>
            <Td>Number Of Executors (--num-executors)</Td>
            <Td>{app.submitParams.numExecutors}</Td>
          </Tr>
          <Tr>
            <Td>Executor Cores (--executor-cores)</Td>
            <Td>{app.submitParams.executorCores}</Td>
          </Tr>
          <Tr>
            <Td>Executor Memory (--executor-memory)</Td>
            <Td>{app.submitParams.executorMemory}</Td>
          </Tr>
          <Tr>
            <Td>Python files (--py-files)</Td>
            <Td>{app.submitParams.pyFiles.join(', ')}</Td>
          </Tr>
          <Tr>
            <Td>Archives (--archives)</Td>
            <Td>{app.submitParams.archives.join(', ')}</Td>
          </Tr>
          <Tr>
            <Td>Additional files (--files)</Td>
            <Td>{app.submitParams.files.join(', ')}</Td>
          </Tr>
          <Tr>
            <Td>Additional JARs (--jars)</Td>
            <Td>{app.submitParams.jars.join(', ')}</Td>
          </Tr>
          {Object.entries(app.submitParams.conf).map(([name, val]) => (
            <Tr key={name}>
              <Td>--conf {name}</Td>
              <Td>{val}</Td>
            </Tr>
          ))}
        </Tbody>
      </Table>
    </Box>
  );
};

export default AppInfo;
