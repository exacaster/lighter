import {ExternalLinkIcon} from '@chakra-ui/icons';
import {Code, Link} from '@chakra-ui/react';
import React from 'react';
import {ApplicationLog} from '../client/types';
import styles from './AppLogs.module.scss';

interface Props {
  logs: ApplicationLog | undefined;
}
const AppLogs: React.FC<Props> = ({logs}) => {
  if (!logs?.log) {
    return null;
  }

  if (logs.log.startsWith('http')) {
    return (
      <Link href={logs.log} isExternal>
        {logs?.log} <ExternalLinkIcon mx="2px" marginBottom="5px" />
      </Link>
    );
  }

  return <Code className={styles.logs}>{logs.log}</Code>;
};

export default AppLogs;
