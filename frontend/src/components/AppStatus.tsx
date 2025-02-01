import {Badge, BadgeProps} from '@chakra-ui/react';
import React, {ReactNode} from 'react';

export const statusMap: {[key: string]: BadgeProps['colorPalette']} = {
  NOT_STARTED: 'gray',
  STARTING: 'gray',
  IDLE: 'blue',
  BUSY: 'blue',
  SHUTTING_DOWN: 'purple',
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
    <Badge colorPalette={statusMap[status.toUpperCase()] || 'white'}>
      {prefix}
      {status}
    </Badge>
  );
};

export default AppStatus;
