import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route,
} from "react-router-dom";
import Sessions from './pages/Sessions';
import Batches from './pages/Batches';
import {ChakraProvider} from '@chakra-ui/react';
import Layout from './components/Layout';

function App() {
  return (
    <ChakraProvider>
    <Router>
    <Layout>

      <Switch>
      <Route path="/sessions">
        <Sessions />
      </Route>
      <Route path="/">
        <Batches />
      </Route>
      </Switch>
      </Layout>

    </Router>
    </ChakraProvider>
  );
}

export default App;
