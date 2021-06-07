import {Button, ButtonProps} from '@chakra-ui/react';
import React from 'react';
import {Link} from 'react-router-dom';

interface ButtonLinkProps extends ButtonProps {
  to: string;
}

const ButtonLink: React.FC<ButtonLinkProps> = ({to, children, ...props}) => {
  return <Link to={to}>
    <Button variant="ghost" colorScheme="purple" {...props}>
      {children}
    </Button>
  </Link>;
};

export default ButtonLink;
