import React from 'react';
import PageHeading from '../components/PageHeading';
import {useBatches} from '../hooks/batch';
import {Table, Thead, Tbody, Tr, Th, Td} from '@chakra-ui/react';
import {generatePath} from 'react-router';
import {useQueryString} from '../hooks/common';
import {pageSize} from '../configuration/consts';
import Pagination from '../components/Pagination';
import Link from '../components/Link';

const Batches: React.FC = () => {
  const from = Number(useQueryString().from) || 0;
  const {data} = useBatches(pageSize, from);

  return (
    <>
      <PageHeading>Batches</PageHeading>
      <Table variant="simple">
        <Thead>
          <Tr>
            <Th>Id</Th>
            <Th>State</Th>
          </Tr>
        </Thead>
        <Tbody>
          {data?.applications?.map((batch) => (
            <Tr key={batch.id}>
              <Td>
                <Link to={generatePath('./batches/:id', {id: batch.id})}>{batch.id}</Link>
              </Td>
              <Td>{batch.state}</Td>
            </Tr>
          ))}
        </Tbody>
      </Table>
      <Pagination path="/" from={from} size={pageSize} visibleSize={data?.applications?.length || 0} />
    </>
  );
};

export default Batches;
