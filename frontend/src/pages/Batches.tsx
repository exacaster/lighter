import React, {useCallback} from 'react';
import PageHeading from '../components/PageHeading';
import {useBatchDelete, useBatches} from '../hooks/batch';
import {Table, Spinner, Flex, Box} from '@chakra-ui/react';
import {generatePath} from 'react-router';
import {pageSize, RoutePath} from '../configuration/consts';
import Pagination from '../components/Pagination';
import Link from '../components/Link';
import AppStatus from '../components/AppStatus';
import DateTime from '../components/DateTime';
import AppActions from '../components/AppActions';
import StatusFilter from '../components/StatusFilter';
import SearchInput from '../components/SearchInput';
import {useSearchParams} from 'react-router-dom';

const Batches: React.FC = () => {
  const [params, setParams] = useSearchParams();
  const fromInt = Number(params.get('from')) || 0;
  const status = params.get('status');
  const search = params.get('search') ?? undefined;
  const {data, isLoading} = useBatches(pageSize, fromInt, params.get('status'), search);
  const {mutate: doDelete, isPending: isDeleting} = useBatchDelete();

  const doSearch = useCallback(
    (value: string) => {
      const newParams = new URLSearchParams(params);
      newParams.set('search', value);
      setParams(newParams);
    },
    [params, setParams],
  );

  if (isLoading || isDeleting) {
    return <Spinner />;
  }

  return (
    <>
      <Flex align="center" mb="5" gap="4">
        <Box flex="3">
          <PageHeading mb="0">Batches</PageHeading>
        </Box>
        <Box flex="1">
          <SearchInput key={search} initialValue={search ?? ''} onSearch={doSearch} />
        </Box>
      </Flex>
      <StatusFilter path="./" status={status} />
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
