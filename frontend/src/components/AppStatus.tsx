import {Badge, BadgeProps} from '@chakra-ui/react';
import React, {ReactNode} from 'react';

export const statusMap: {[key: string]: BadgeProps['colorScheme']} = {
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

interface Props {
  status: string;
  prefix?: ReactNode;
}

const AppStatus: React.FC<Props> = ({status, prefix}) => {
  return (
    <Badge colorScheme={statusMap[status.toUpperCase()]}>
      {prefix}
      {status}
    </Badge>
  );
};

export default AppStatus;
