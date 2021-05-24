import {Button, ButtonProps, Link} from '@chakra-ui/react';
import React from 'react';

interface ButtonLinkProps extends ButtonProps {
  href: string;
}

const ButtonLink: React.FC<ButtonLinkProps> = ({href, children, ...props}) => {
  return <Link href={href}>
    <Button colorScheme="teal" variant="ghost" {...props}>
      {children}
    </Button>
  </Link>;
};

export default ButtonLink;
