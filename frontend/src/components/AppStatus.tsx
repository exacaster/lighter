import {Badge, BadgeProps} from '@chakra-ui/react';
import React from 'react';

const statusMap: {[key: string]: BadgeProps['colorScheme']} = {
  NOT_STARTED: 'purple',
  // STARTING: '',
  // IDLE: '',
  // BUSY: '',
  // SHUTTING_DOWN: '',
  ERROR: 'red',
  DEAD: 'red',
  KILLED: 'red',
  SUCCESS: 'green',
};

const AppStatus: React.FC<{status: string}> = ({status}) => {
  return <Badge colorScheme={statusMap[status]}>{status}</Badge>;
};

export default AppStatus;
