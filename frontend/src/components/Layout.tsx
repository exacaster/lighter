import React, {ReactNode} from 'react';
import {Box, Divider, Flex, Stack, Image, Link} from '@chakra-ui/react';
import ButtonLink from './ButtonLink';
import {useLocation} from 'react-router';

type Props = {
  children?: ReactNode;
  active?: string;
};

const Layout: React.FC = ({children}: Props) => {
  const match = useLocation();

  return <>
    <Flex p="10" direction="column" alignItems="stretch" minH="100vh">
    <header>
      <Stack direction="row" spacing={4} align="center">
        <Image src="https://upload.wikimedia.org/wikipedia/commons/f/f3/Apache_Spark_logo.svg" width="100px"/>
        <ButtonLink to="/" isActive={match.pathname === '/'}>
          Batches
        </ButtonLink>
        <ButtonLink to="/sessions" isActive={match.pathname === '/sessions'}>
          Sessions
        </ButtonLink>
      </Stack>
    </header>
    <Box flex="1" pt="10" pb="10">{children}</Box>
    <footer>
      <Divider mb="5" />
      © <Link href="https://exacaster.com">Exacaster</Link>
    </footer>
  </Flex>

  </>
};

export default Layout;

