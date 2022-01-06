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
              <Td>File</Td>
              <Td>{batch.submitParams.file}</Td>
            </Tr>
            <Tr>
              <Td>Args</Td>
              <Td>{batch.submitParams.args.join(', ')}</Td>
            </Tr>
            <Tr>
              <Td>Name (--name)</Td>
              <Td>{batch.submitParams.name}</Td>
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
            {Object.entries(batch.submitParams.conf).map(([name, val]) => (
              <Tr key={name}>
                <Td>--conf {name}</Td>
                <Td>{val}</Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
      </Box>
    );
  }, [batch]);

  function getSparkSubmitArg(key: string, value: string) {
    if (value) {
      return ' ' + key + ' ' + value;
    } else {
      return '';
    }
  }

  const sparkSubmitStr = useMemo(() => {
    if (!batch) {
      return null;
    }

    return (
      <Code className={styles.logs}>
        spark-submit
        {getSparkSubmitArg('--name', batch.submitParams.name)}
        {getSparkSubmitArg('--driver-cores', batch.submitParams.driverCores.toString())}
        {getSparkSubmitArg('--driver-memory', batch.submitParams.driverMemory)}
        {getSparkSubmitArg('--num-executors', batch.submitParams.numExecutors.toString())}
        {getSparkSubmitArg('--executor-cores', batch.submitParams.executorCores.toString())}
        {getSparkSubmitArg('--executor-memory', batch.submitParams.executorMemory)}
        {getSparkSubmitArg('--py-files', batch.submitParams.pyFiles.join(','))}
        {getSparkSubmitArg('--archives', batch.submitParams.archives.join(','))}
        {getSparkSubmitArg('--files', batch.submitParams.files.join(','))}
        {getSparkSubmitArg('--jars', batch.submitParams.jars.join(','))}
        {Object.entries(batch.submitParams.conf).map(([name, val]) => (
          <span key={name}>
            --conf {name} {val + ' '}
          </span>
        ))}
        {' ' + batch.submitParams.file}
        {' ' + batch.submitParams.args.join(' ')}
      </Code>
    );
  }, [batch]);

  const logsStr = useMemo(() => {
    if (!logs) {
      return null;
    }

    if (logs.log.startsWith('http')) {
      return <a href={logs.log}>{logs?.log}</a>;
    } else {
      return <Code className={styles.logs}>{logs.log}</Code>;
    }
  }, [logs]);

  return (
    <div className={styles.batch}>
      <PageHeading>Batch {id}</PageHeading>
      <Box mt="5">
        <b>Logs:</b>
        <br />
        {logsStr}
      </Box>

      {appInfo}
      <Box mt="5">
        <b>Spark submit command:</b>
        <br />
        {sparkSubmitStr}
      </Box>
    </div>
  );
};

export default Batch;
