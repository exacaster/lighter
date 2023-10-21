import React from 'react';
import {useNavigate, useParams} from 'react-router';
import {useBatchLog, useBatch, useBatchDelete} from '../hooks/batch';
import {Box, Spinner} from '@chakra-ui/react';
import AppInfo from '../components/AppInfo';
import AppLogs from '../components/AppLogs';
import AppSubmit from '../components/AppSubmit';
import AppTitle from '../components/AppTitle';
import {RoutePath} from '../configuration/consts';

const Batch: React.FC = () => {
  const {id} = useParams();
  const {data: logs} = useBatchLog(id!);
  const {data: batch} = useBatch(id!);
  const {mutateAsync: doDelete, isPending: isDeleting} = useBatchDelete();
  const navigate = useNavigate();

  const onDelete = () => {
    doDelete(id!).then(() => navigate(RoutePath.BATCHES));
  };

  if (isDeleting) {
    return <Spinner />;
  }

  if (!batch) {
    return null;
  }

  return (
    <div>
      <AppTitle app={batch} onDelete={onDelete} />
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
