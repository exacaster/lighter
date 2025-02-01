import {HStack, IconButton, Link as ExLink} from '@chakra-ui/react';
import React from 'react';
import {Application} from '../client/types';
import {useConfiguration} from '../hooks/configuration';
import {formatLink} from '../utils/application';
import {FaCalendar, FaExternalLinkAlt, FaTrash} from 'react-icons/fa';

interface Props {
  app: Application;
  onDelete?: () => void;
}

const AppActions: React.FC<Props> = ({app, onDelete}) => {
  const {data: conf} = useConfiguration();

  return (
    <HStack>
      {!!conf?.sparkHistoryServerUrl && !!app.appId && (
        <IconButton size="sm" aria-label="History" variant="ghost" asChild>
          <ExLink title="History" target="_blank" href={`${conf.sparkHistoryServerUrl}/history/${app.appId}`}>
            <FaExternalLinkAlt />
          </ExLink>
        </IconButton>
      )}
      {!!conf?.externalLogsUrlTemplate && !!app.appId && (
        <IconButton size="sm" aria-label="External logs" variant="ghost" asChild>
          <ExLink title="External logs" target="_blank" href={formatLink(conf.externalLogsUrlTemplate, app)}>
            <FaCalendar />
          </ExLink>
        </IconButton>
      )}
      {onDelete && (
        <IconButton size="sm" aria-label="Delete" variant="ghost" onClick={() => onDelete()}>
          <FaTrash title="Delete" />
        </IconButton>
      )}
    </HStack>
  );
};

export default AppActions;
