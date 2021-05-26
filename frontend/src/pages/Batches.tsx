import React from 'react';
import PageHeading from '../components/PageHeading';
import {useBatches} from '../hooks/batch';
import {
  Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
} from "@chakra-ui/react";
import {generatePath} from 'react-router';
import {Link} from 'react-router-dom';

const Batches: React.FC = () => {
  const {data} = useBatches();

  return <>
    <PageHeading>Batches</PageHeading>
    <Table variant="simple">
      <Thead>
        <Tr>
          <Th>Id</Th>
          <Th>State</Th>
        </Tr>
      </Thead>
      <Tbody>
        {data?.applications?.map((batch) => <Tr key={batch.id}>
          <Td><Link to={generatePath('/batches/:id', {id: batch.id})}>{batch.id}</Link></Td>
          <Td>{batch.state}</Td>
        </Tr>)}
      </Tbody>
    </Table>
  </>;
};

export default Batches;
