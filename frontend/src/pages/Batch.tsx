import {Code} from '@chakra-ui/layout';
import React, {useMemo} from 'react';
import {useParams} from 'react-router';
import PageHeading from '../components/PageHeading';
import {useBatchLog, useBatch} from '../hooks/batch';
import styles from './Batch.module.scss';
import {Table, Thead, Tbody, Tr, Th, Td, Box} from '@chakra-ui/react';

const Batch: React.FC = () => {
  const {id} = useParams<{id: string}>();
  const {data: logs} = useBatchLog(id);
  const {data: batch} = useBatch(id);

  const appInfo = useMemo(() => {
    if (!batch) {
      return null;
    }

    return (
      <Box mt="5">
        <Table variant="simple" size="sm">
          <Thead>
            <Tr>
              <Th>Property</Th>
              <Th>Value</Th>
            </Tr>
          </Thead>
          <Tbody>
            <Tr>
              <Td>Name (--name)</Td>
              <Td>{batch.submitParams.name}</Td>
            </Tr>
            <Tr>
              <Td>File</Td>
              <Td>{batch.submitParams.file}</Td>
            </Tr>
            <Tr>
              <Td>Driver Cores (--driver-cores)</Td>
              <Td>{batch.submitParams.driverCores}</Td>
            </Tr>
            <Tr>
              <Td>Driver Memory (--driver-memory)</Td>
              <Td>{batch.submitParams.driverMemory}</Td>
            </Tr>
            <Tr>
              <Td>Number Of Executors (--num-executors)</Td>
              <Td>{batch.submitParams.numExecutors}</Td>
            </Tr>
            <Tr>
              <Td>Executor Cores (--executor-cores)</Td>
              <Td>{batch.submitParams.executorCores}</Td>
            </Tr>
            <Tr>
              <Td>Executor Memory (--executor-memory)</Td>
              <Td>{batch.submitParams.executorMemory}</Td>
            </Tr>
            <Tr>
              <Td>Args</Td>
              <Td>{batch.submitParams.args.join(', ')}</Td>
            </Tr>
            <Tr>
              <Td>Python files (--py-files)</Td>
              <Td>{batch.submitParams.pyFiles.join(', ')}</Td>
            </Tr>
            <Tr>
              <Td>Archives (--archives)</Td>
              <Td>{batch.submitParams.archives.join(', ')}</Td>
            </Tr>
            <Tr>
              <Td>Additional files (--files)</Td>
              <Td>{batch.submitParams.files.join(', ')}</Td>
            </Tr>
            <Tr>
              <Td>Additional JARs (--jars)</Td>
              <Td>{batch.submitParams.jars.join(', ')}</Td>
            </Tr>
            <Tr>
              <Th>Config (--conf)</Th>
              <Th>Value</Th>
            </Tr>
            {Object.entries(batch.submitParams.conf).map(([name, val]) => (
              <Tr key={name}>
                <Td>{name}</Td>
                <Td>{val}</Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
      </Box>
    );
  }, [batch]);

  return (
    <div className={styles.batch}>
      <PageHeading>Batch {id}</PageHeading>
      <Code className={styles.logs}>{logs?.log}</Code>
      {appInfo}
    </div>
  );
};

export default Batch;
