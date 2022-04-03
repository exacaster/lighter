import React, {ReactNode} from 'react';
import {Box, Divider, Flex, Stack, Image, Link} from '@chakra-ui/react';
import ButtonLink from './ButtonLink';
import {generatePath, useLocation} from 'react-router';
import {RoutePath} from '../configuration/consts';

type Props = {
  children?: ReactNode;
  active?: string;
};

const Layout: React.FC = ({children}: Props) => {
  const match = useLocation();

  return (
    <Flex p="10" direction="column" alignItems="stretch" minH="100vh">
      <header>
        <Stack direction="row" spacing={4} align="center">
          <Image src={`${process.env.PUBLIC_URL}/images/logo.svg`} width="150px" />
          <ButtonLink to={generatePath(RoutePath.BATCHES)} isActive={match.pathname === '/' || match.pathname.startsWith(RoutePath.BATCHES)}>
            Batches
          </ButtonLink>
          <ButtonLink to={generatePath(RoutePath.SESSIONS)} isActive={match.pathname.startsWith(RoutePath.SESSIONS)}>
            Sessions
          </ButtonLink>
        </Stack>
      </header>
      <Box flex="1" pt="10" pb="10">
        {children}
      </Box>
      <footer>
        <Divider mb="5" />© <Link href="https://exacaster.com">Exacaster</Link>
      </footer>
    </Flex>
  );
};

export default Layout;
