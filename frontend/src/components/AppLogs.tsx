import {Code, Link} from '@chakra-ui/react';
import React from 'react';
import {ApplicationLog} from '../client/types';
import styles from './AppLogs.module.scss';
import {FaExternalLinkAlt} from 'react-icons/fa';

interface Props {
  logs: ApplicationLog | undefined;
}
const AppLogs: React.FC<Props> = ({logs}) => {
  if (!logs?.log) {
    return null;
  }

  if (logs.log.startsWith('http')) {
    return (
      <Link href={logs.log} target="_blank" rel="noopener noreferrer">
        {logs?.log} <FaExternalLinkAlt />
      </Link>
    );
  }

  return <Code className={styles.logs}>{logs.log}</Code>;
};

export default AppLogs;
