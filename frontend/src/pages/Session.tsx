import {Code} from '@chakra-ui/layout';
import React, {useMemo} from 'react';
import {useParams} from 'react-router';
import PageHeading from '../components/PageHeading';
import styles from './Batch.module.scss';
import {Table, Thead, Tbody, Tr, Th, Td, Box} from '@chakra-ui/react';
import {useSession, useSessionLog} from '../hooks/session';

const Session: React.FC = () => {
  const {id} = useParams<{id: string}>();
  const {data: logs} = useSessionLog(id);
  const {data: session} = useSession(id);

  const appInfo = useMemo(() => {
    if (!session) {
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
              <Td>Name</Td>
              <Td>{session.submitParams.name}</Td>
            </Tr>
            <Tr>
              <Td>File</Td>
              <Td>{session.submitParams.file}</Td>
            </Tr>
            <Tr>
              <Td>Driver Cores</Td>
              <Td>{session.submitParams.driverCores}</Td>
            </Tr>
            <Tr>
              <Td>Driver Memory</Td>
              <Td>{session.submitParams.driverMemory}</Td>
            </Tr>
            <Tr>
              <Td>Number Of Executors</Td>
              <Td>{session.submitParams.numExecutors}</Td>
            </Tr>
            <Tr>
              <Td>Executor Cores</Td>
              <Td>{session.submitParams.executorCores}</Td>
            </Tr>
            <Tr>
              <Td>Executor Memory</Td>
              <Td>{session.submitParams.executorMemory}</Td>
            </Tr>
            <Tr>
              <Td>Args</Td>
              <Td>{session.submitParams.args.join(', ')}</Td>
            </Tr>
            <Tr>
              <Th>Config</Th>
              <Th>Value</Th>
            </Tr>
            {Object.entries(session.submitParams.conf).map(([name, val]) => (
              <Tr key={name}>
                <Td>{name}</Td>
                <Td>{val}</Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
      </Box>
    );
  }, [session]);

  return (
    <div className={styles.batch}>
      <PageHeading>Session {id}</PageHeading>
      <Code className={styles.logs}>{logs?.log}</Code>
      {appInfo}
    </div>
  );
};

export default Session;
