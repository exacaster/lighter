import React from 'react';
import {BrowserRouter as Router, Switch, Route} from 'react-router-dom';
import Sessions from './pages/Sessions';
import Batches from './pages/Batches';
import {ChakraProvider} from '@chakra-ui/react';
import Layout from './components/Layout';
import {QueryCache, QueryClient, QueryClientProvider} from 'react-query';
import Batch from './pages/Batch';

import {extendTheme} from '@chakra-ui/react';
import '@fontsource/open-sans/700.css';
import '@fontsource/open-sans/600.css';
import '@fontsource/open-sans';
import Session from './pages/Session';

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
            <Switch>
              <Route path="/sessions/:id">
                <Session />
              </Route>
              <Route path="/sessions">
                <Sessions />
              </Route>
              <Route path="/batches/:id">
                <Batch />
              </Route>
              <Route path="/">
                <Batches />
              </Route>
            </Switch>
          </Layout>
        </Router>
      </QueryClientProvider>
    </ChakraProvider>
  );
}

export default App;
