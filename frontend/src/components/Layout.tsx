import React, {ReactNode} from 'react';
import {Box, Divider, Flex, Stack, Image, Link} from '@chakra-ui/react';
import ButtonLink from './ButtonLink';
import {generatePath, useLocation} from 'react-router';

type Props = {
  children?: ReactNode;
  active?: string;
};

const Layout: React.FC = ({children}: Props) => {
  const match = useLocation();

  return (
    <>
      <Flex p="10" direction="column" alignItems="stretch" minH="100vh">
        <header>
          <Stack direction="row" spacing={4} align="center">
            <Image src={`${process.env.PUBLIC_URL}/images/logo.svg`} width="150px" />
            <ButtonLink to={generatePath('/batches')} isActive={match.pathname === '/' || match.pathname === '/batches'}>
              Batches
            </ButtonLink>
            <ButtonLink to={generatePath('/sessions')} isActive={match.pathname === '/sessions'}>
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
    </>
  );
};

export default Layout;
