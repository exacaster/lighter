import React from 'react';
import {useParams} from 'react-router';
import {useSession, useSessionLog} from '../hooks/session';
import AppInfo from '../components/AppInfo';
import {Box} from '@chakra-ui/react';
import AppLogs from '../components/AppLogs';
import AppTitle from '../components/AppTitle';

const Session: React.FC = () => {
  const {id} = useParams();
  const {data: logs} = useSessionLog(id!);
  const {data: session} = useSession(id!);

  if (!session) {
    return null;
  }

  return (
    <div>
      <AppTitle app={session}>Session {id}</AppTitle>
      <Box textStyle="caption" mt="5">
        Logs:
      </Box>
      <Box mt="1">
        <AppLogs logs={logs} />
      </Box>

      <AppInfo app={session} />
    </div>
  );
};

export default Session;
