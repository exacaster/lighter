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
        {data?.batches?.map((batch) => <Tr key={batch.id}>
          <Td>{batch.id}</Td>
          <Td>{batch.state}</Td>
        </Tr>)}
      </Tbody>
    </Table>
  </>;
};

export default Batches;
