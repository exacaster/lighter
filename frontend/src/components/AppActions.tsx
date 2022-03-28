import {ExternalLinkIcon, CloseIcon, CalendarIcon} from '@chakra-ui/icons';
import {HStack, IconButton, Link as ExLink} from '@chakra-ui/react';
import React from 'react';
import {Application} from '../client/types';
import {useConfiguration} from '../hooks/configuration';
import {formatLink} from '../utils/application';

interface Props {
  app: Application;
  onDelete?: () => void;
}

const AppActions: React.FC<Props> = ({app, onDelete}) => {
  const {data: conf} = useConfiguration();

  return (
    <HStack>
      {!!conf?.sparkHistoryServerUrl && !!app.appId && (
        <IconButton
          size="sm"
          icon={<ExternalLinkIcon />}
          title="History"
          aria-label="History"
          as={ExLink}
          target="_blank"
          href={`${conf.sparkHistoryServerUrl}/history/${app.appId}`}
        />
      )}
      {!!conf?.externalLogsUrlTemplate && !!app.appId && (
        <IconButton
          size="sm"
          icon={<CalendarIcon />}
          title="External logs"
          aria-label="External logs"
          as={ExLink}
          target="_blank"
          href={formatLink(conf.externalLogsUrlTemplate, app)}
        />
      )}
      {onDelete && <IconButton size="sm" title="Delete" aria-label="Delete" onClick={() => onDelete()} icon={<CloseIcon />} />}
    </HStack>
  );
};

export default AppActions;
