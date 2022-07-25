import React from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import Sessions from './pages/Sessions';
import Batches from './pages/Batches';
import {ChakraProvider} from '@chakra-ui/react';
import Layout from './components/Layout';
import {QueryCache, QueryClient, QueryClientProvider} from '@tanstack/react-query';
import Batch from './pages/Batch';

import {extendTheme} from '@chakra-ui/react';
import '@fontsource/open-sans/700.css';
import '@fontsource/open-sans/600.css';
import '@fontsource/open-sans';
import Session from './pages/Session';
import {RoutePath} from './configuration/consts';

const theme = extendTheme({
  fonts: {
    heading: 'Open Sans',
    body: 'Open Sans',
  },
});

const queryCache = new QueryCache();
const queryClient = new QueryClient({
  queryCache,
  defaultOptions: {
    queries: {
      staleTime: 120000,
    },
  },
});

function App() {
  return (
    <ChakraProvider theme={theme}>
      <QueryClientProvider client={queryClient}>
        <Router basename={process.env.PUBLIC_URL}>
          <Layout>
            <Routes>
              <Route path={RoutePath.SESSION} element={<Session />} />
              <Route path={RoutePath.SESSIONS} element={<Sessions />} />
              <Route path={RoutePath.BATCH} element={<Batch />} />
              <Route path={RoutePath.BATCHES} element={<Batches />} />
              <Route path="/" element={<Batches />} />
            </Routes>
          </Layout>
        </Router>
      </QueryClientProvider>
    </ChakraProvider>
  );
}

export default App;
