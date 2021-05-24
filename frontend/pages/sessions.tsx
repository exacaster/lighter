import {Alert} from '@chakra-ui/react';
import React from 'react';
import Layout from '../components/Layout'
import PageHeading from '../components/PageHeading';

const SessionsPage = () => (
  <Layout title="Sessions" active="/sessions">
    <PageHeading>Sessions</PageHeading>
    <Alert status="info">
      comming soon!
    </Alert>
  </Layout>
);

export default SessionsPage;
