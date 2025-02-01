import React, {ReactNode} from 'react';
import {Box, Separator, Flex, Stack, Image, Link} from '@chakra-ui/react';
import ButtonLink from './ButtonLink';
import {generatePath, useLocation} from 'react-router';
import {RoutePath} from '../configuration/consts';
import {srcJoin} from '../utils/application';

interface Props {
  children?: ReactNode;
  active?: string;
}

const Layout: React.FC<Props> = ({children}) => {
  const match = useLocation();

  return (
    <Flex p="10" direction="column" alignItems="stretch" minH="100vh">
      <header>
        <Stack direction="row" gap={4} align="center">
          <Image src={srcJoin(import.meta.env.BASE_URL, 'logo.svg')} width="150px" />
          <ButtonLink to={generatePath(RoutePath.BATCHES)} variant={match.pathname === '/' || match.pathname.startsWith(RoutePath.BATCHES) ? 'solid' : 'plain'}>
            Batches
          </ButtonLink>
          <ButtonLink to={generatePath(RoutePath.SESSIONS)} variant={match.pathname.startsWith(RoutePath.SESSIONS) ? 'solid' : 'plain'}>
            Sessions
          </ButtonLink>
        </Stack>
      </header>
      <Box flex="1" pt="10" pb="10">
        {children}
      </Box>
      <footer>
        <Separator mb="5" />© <Link href="https://exacaster.com">Exacaster</Link>
      </footer>
    </Flex>
  );
};

export default Layout;
