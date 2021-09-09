import {CloseIcon, ExternalLinkIcon} from '@chakra-ui/icons';
import {HStack, IconButton, Spinner, Link as ExLink, Table, Tbody, Td, Th, Thead, Tr} from '@chakra-ui/react';
import React from 'react';
import {generatePath} from 'react-router-dom';
import AppStatus from '../components/AppStatus';
import DateTime from '../components/DateTime';
import Link from '../components/Link';
import PageHeading from '../components/PageHeading';
import Pagination from '../components/Pagination';
import {pageSize} from '../configuration/consts';
import {useQueryString} from '../hooks/common';
import {useConfiguration} from '../hooks/configuration';
import {useSessionDelete, useSessions} from '../hooks/session';

const Sessions: React.FC = () => {
  const from = Number(useQueryString().from) || 0;
  const {data, isLoading} = useSessions(pageSize, from);
  const {mutate: doDelete, isLoading: isDeleting} = useSessionDelete();
  const {data: conf} = useConfiguration();

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
                <Link to={generatePath('./sessions/:id', {id: session.id})}>{session.id}</Link>
              </Td>
              <Td>{session.submitParams.name}</Td>
              <Td>
                <DateTime>{session.createdAt}</DateTime>
              </Td>
              <Td>
                <AppStatus status={session.state} />
              </Td>
              <Td>
                <HStack>
                  {!!conf?.sparkHistoryServerUrl && !!session.appId && (
                    <IconButton
                      size="sm"
                      icon={<ExternalLinkIcon />}
                      title="History"
                      aria-label="History"
                      as={ExLink}
                      target="_blank"
                      href={`${conf?.sparkHistoryServerUrl}/history/${session.appId}/jobs`}
                    />
                  )}
                  <IconButton size="sm" title="Delete" aria-label="Delete" onClick={() => doDelete(session.id)} icon={<CloseIcon />} />
                </HStack>
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
