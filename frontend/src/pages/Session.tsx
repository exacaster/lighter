import React, {lazy} from 'react';
import {useParams} from 'react-router';
import {useSession, useSessionDelete, useSessionLog} from '../hooks/session';
import AppInfo from '../components/AppInfo';
import {Box, Spinner, Tabs} from '@chakra-ui/react';
import AppLogs from '../components/AppLogs';
import AppTitle from '../components/AppTitle';
import {useNavigate} from 'react-router-dom';
import {RoutePath} from '../configuration/consts';

const Statements = lazy(() => import('../components/Statements'));
const Session: React.FC = () => {
  const {id} = useParams();
  const {data: logs} = useSessionLog(id!);
  const {data: session} = useSession(id!);

  const {mutateAsync: doDelete, isPending: isDeleting} = useSessionDelete();
  const navigate = useNavigate();

  const onDelete = () => {
    doDelete(id!).then(() => navigate(RoutePath.SESSIONS));
  };

  if (isDeleting) {
    return <Spinner />;
  }

  if (!session) {
    return null;
  }

  return (
    <div>
      <AppTitle app={session} onDelete={onDelete} />
      <Tabs.Root isLazy lazyMount unmountOnExit defaultValue="info">
        <Tabs.List>
          {/* @ts-expect-error - TS integration problems */}
          <Tabs.Trigger value="info">Info</Tabs.Trigger>
          {/* @ts-expect-error - TS integration problems */}
          <Tabs.Trigger value="statements">Statements</Tabs.Trigger>
        </Tabs.List>

        {/* @ts-expect-error - TS integration problems */}
        <Tabs.Content value="info">
          <Box textStyle="caption" mt="5">
            Logs:
          </Box>
          <Box mt="1">
            <AppLogs logs={logs} />
          </Box>
          <AppInfo app={session} />
        </Tabs.Content>
        {/* @ts-expect-error - TS integration problems */}
        <Tabs.Content value="statements">
          <Statements session={session} />
        </Tabs.Content>
      </Tabs.Root>
    </div>
  );
};

export default Session;
