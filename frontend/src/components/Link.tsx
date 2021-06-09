import {Link as UILink} from '@chakra-ui/react';
import React from 'react';
import {Link as ReactLink, LinkProps} from 'react-router-dom';

const Link: React.FC<LinkProps> = ({children, ...props}) => {
  return (
    <UILink as={ReactLink} {...props}>
      {children}
    </UILink>
  );
};

export default Link;
