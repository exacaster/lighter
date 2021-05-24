import React, {ReactNode} from 'react';
import Link from 'next/link';
import Head from 'next/head';
import {Box, Divider, Flex, Stack, Image} from '@chakra-ui/react';
import ButtonLink from './ButtonLink';

type Props = {
  children?: ReactNode;
  title?: string;
  active?: string;
};

const Layout = ({children, active = '/', title = 'Lighter'}: Props) => (
  <>
    <Head>
      <title>{title}</title>
      <meta charSet="utf-8" />
      <meta name="viewport" content="initial-scale=1.0, width=device-width" />
    </Head>
    <Flex p="10" direction="column" alignItems="stretch" minH="100%">
    <header>
      <Stack direction="row" spacing={4} align="center">
        <Image src="https://upload.wikimedia.org/wikipedia/commons/f/f3/Apache_Spark_logo.svg" width="100px"/>
        <ButtonLink href="/" isActive={active === '/'}>
          Batches
        </ButtonLink>
        <ButtonLink href="/sessions" isActive={active === '/sessions'}>
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
);

export default Layout;
