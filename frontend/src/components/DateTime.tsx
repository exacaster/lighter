import React from 'react';
import Moment from 'react-moment';

const DateTime: React.FC<{children: string}> = ({children}) => {
  return (
    <span title={children}>
      <Moment utc local format="YYYY-MM-DD HH:mm:ss">
        {children}
      </Moment>
    </span>
  );
};

export default DateTime;
