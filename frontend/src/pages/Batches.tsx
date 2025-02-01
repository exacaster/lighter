import React from 'react';
import PageHeading from '../components/PageHeading';
import {useBatchDelete, useBatches} from '../hooks/batch';
import {Table, Spinner} from '@chakra-ui/react';
import {generatePath} from 'react-router';
import {useQueryString} from '../hooks/common';
import {pageSize, RoutePath} from '../configuration/consts';
import Pagination from '../components/Pagination';
import Link from '../components/Link';
import AppStatus from '../components/AppStatus';
import DateTime from '../components/DateTime';
import AppActions from '../components/AppActions';
import StatusFilter from '../components/StatusFilter';

const Batches: React.FC = () => {
  const {from, status} = useQueryString();
  const fromInt = Number(from) || 0;
  const {data, isLoading} = useBatches(pageSize, fromInt, status as string);
  const {mutate: doDelete, isPending: isDeleting} = useBatchDelete();

  if (isLoading || isDeleting) {
    return <Spinner />;
  }

  return (
    <>
      <PageHeading>Batches</PageHeading>
      <StatusFilter path="./" status={status as string} />
      <Table.Root size="sm">
        <Table.Header>
          <Table.Row>
            <Table.ColumnHeader>Id</Table.ColumnHeader>
            <Table.ColumnHeader>Name</Table.ColumnHeader>
            <Table.ColumnHeader>Created</Table.ColumnHeader>
            <Table.ColumnHeader>State</Table.ColumnHeader>
            <Table.ColumnHeader>Actions</Table.ColumnHeader>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {data?.applications?.map((batch) => (
            <Table.Row key={batch.id}>
              <Table.Cell>
                <Link to={generatePath(RoutePath.BATCH, {id: batch.id})}>{batch.id}</Link>
              </Table.Cell>
              <Table.Cell>{batch.submitParams.name}</Table.Cell>
              <Table.Cell>
                <DateTime>{batch.createdAt}</DateTime>
              </Table.Cell>
              <Table.Cell>
                <AppStatus status={batch.state} />
              </Table.Cell>
              <Table.Cell>
                <AppActions app={batch} onDelete={() => doDelete(batch.id)} />
              </Table.Cell>
            </Table.Row>
          ))}
        </Table.Body>
      </Table.Root>
      <Pagination path="./" size={pageSize} visibleSize={data?.applications?.length || 0} />
    </>
  );
};

export default Batches;
