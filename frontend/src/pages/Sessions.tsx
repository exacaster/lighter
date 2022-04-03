import {Spinner, Table, Tbody, Td, Th, Thead, Tr} from '@chakra-ui/react';
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
  const {mutate: doDelete, isLoading: isDeleting} = useSessionDelete();

  if (isLoading || isDeleting) {
    return <Spinner />;
  }

  return (
    <>
      <PageHeading>Sessions</PageHeading>
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
          {data?.applications?.map((session) => (
            <Tr key={session.id}>
              <Td>
                <Link to={generatePath(RoutePath.SESSION, {id: session.id})}>{session.id}</Link>
              </Td>
              <Td>{session.submitParams.name}</Td>
              <Td>
                <DateTime>{session.createdAt}</DateTime>
              </Td>
              <Td>
                <AppStatus status={session.state} />
              </Td>
              <Td>
                <AppActions app={session} onDelete={() => doDelete(session.id)} />
              </Td>
            </Tr>
          ))}
        </Tbody>
      </Table>
      <Pagination path="./" from={from} size={pageSize} visibleSize={data?.applications?.length || 0} />
    </>
  );
};

export default Sessions;
