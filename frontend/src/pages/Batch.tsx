import React from 'react';
import {useParams} from 'react-router';
import {useBatchLog, useBatch} from '../hooks/batch';
import {Box} from '@chakra-ui/react';
import AppInfo from '../components/AppInfo';
import AppLogs from '../components/AppLogs';
import AppSubmit from '../components/AppSubmit';
import AppTitle from '../components/AppTitle';

const Batch: React.FC = () => {
  const {id} = useParams<{id: string}>();
  const {data: logs} = useBatchLog(id);
  const {data: batch} = useBatch(id);

  if (!batch) {
    return null;
  }

  return (
    <div>
      <AppTitle app={batch}>Batch {id}</AppTitle>
      <Box textStyle="caption" mt="5">
        Logs:
      </Box>
      <Box mt="1">
        <AppLogs logs={logs} />
      </Box>

      <AppInfo app={batch} />
      <Box textStyle="caption" mt="5">
        Spark submit command:
      </Box>
      <Box mt="1">
        <AppSubmit app={batch} />
      </Box>
    </div>
  );
};

export default Batch;
