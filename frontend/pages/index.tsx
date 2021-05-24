import {Table, Tbody, Td, Th, Thead, Tr} from '@chakra-ui/react';
import React from 'react';
import Layout from '../components/Layout';
import PageHeading from '../components/PageHeading';

const IndexPage = () => (
  <Layout title="Batches">
    <PageHeading>Batches</PageHeading>
    <Table variant="simple">
      <Thead>
        <Tr>
          <Th>Name</Th>
          <Th>Application ID</Th>
          <Th>Logs</Th>
        </Tr>
      </Thead>
      <Tbody>
        <Tr>
          <Td>Test</Td>
          <Td>todo</Td>
          <Td>todo</Td>
        </Tr>
      </Tbody>
    </Table>
  </Layout>
);

export default IndexPage;
