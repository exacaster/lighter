import React from 'react';
import PageHeading from '../components/PageHeading';
import {useBatchDelete, useBatches} from '../hooks/batch';
import {Table, Thead, Tbody, Tr, Th, Td, Spinner} from '@chakra-ui/react';
import {generatePath} from 'react-router';
import {useQueryString} from '../hooks/common';
import {pageSize, RoutePath} from '../configuration/consts';
import Pagination from '../components/Pagination';
import Link from '../components/Link';
import AppStatus from '../components/AppStatus';
import DateTime from '../components/DateTime';
import AppActions from '../components/AppActions';

const Batches: React.FC = () => {
  const from = Number(useQueryString().from) || 0;
  const {data, isLoading} = useBatches(pageSize, from);
  const {mutate: doDelete, isLoading: isDeleting} = useBatchDelete();

  if (isLoading || isDeleting) {
    return <Spinner />;
  }

  return (
    <>
      <PageHeading>Batches</PageHeading>
      <Table variant="simple" size="sm">
        <Thead>
          <Tr>
            <Th>Id</Th>
            <Th>Name</Th>
            <Th>Created</Th>
            <Th>State</Th>
            <Th>Actions</Th>
          </Tr>
        </Thead>
        <Tbody>
          {data?.applications?.map((batch) => (
            <Tr key={batch.id}>
              <Td>
                <Link to={generatePath(RoutePath.BATCH, {id: batch.id})}>{batch.id}</Link>
              </Td>
              <Td>{batch.submitParams.name}</Td>
              <Td>
                <DateTime>{batch.createdAt}</DateTime>
              </Td>
              <Td>
                <AppStatus status={batch.state} />
              </Td>
              <Td>
                <AppActions app={batch} onDelete={() => doDelete(batch.id)} />
              </Td>
            </Tr>
          ))}
        </Tbody>
      </Table>
      <Pagination path="./" from={from} size={pageSize} visibleSize={data?.applications?.length || 0} />
    </>
  );
};

export default Batches;
