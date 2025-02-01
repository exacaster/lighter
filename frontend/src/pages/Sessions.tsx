import {Spinner, Table} from '@chakra-ui/react';
import React from 'react';
import {generatePath} from 'react-router-dom';
import AppActions from '../components/AppActions';
import AppStatus from '../components/AppStatus';
import DateTime from '../components/DateTime';
import Link from '../components/Link';
import PageHeading from '../components/PageHeading';
import Pagination from '../components/Pagination';
import {pageSize, RoutePath} from '../configuration/consts';
import {useQueryString} from '../hooks/common';
import {useSessionDelete, useSessions} from '../hooks/session';

const Sessions: React.FC = () => {
  const from = Number(useQueryString().from) || 0;
  const {data, isLoading} = useSessions(pageSize, from);
  const {mutate: doDelete, isPending: isDeleting} = useSessionDelete();

  if (isLoading || isDeleting) {
    return <Spinner />;
  }

  return (
    <>
      <PageHeading>Sessions</PageHeading>
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
          {data?.applications?.map((session) => (
            <Table.Row key={session.id}>
              <Table.Cell>
                <Link to={generatePath(RoutePath.SESSION, {id: session.id})}>{session.id}</Link>
              </Table.Cell>
              <Table.Cell>{session.submitParams.name}</Table.Cell>
              <Table.Cell>
                <DateTime>{session.createdAt}</DateTime>
              </Table.Cell>
              <Table.Cell>
                <AppStatus status={session.state} />
              </Table.Cell>
              <Table.Cell>
                <AppActions app={session} onDelete={() => doDelete(session.id)} />
              </Table.Cell>
            </Table.Row>
          ))}
        </Table.Body>
      </Table.Root>
      <Pagination path="./" size={pageSize} visibleSize={data?.applications?.length || 0} />
    </>
  );
};

export default Sessions;
