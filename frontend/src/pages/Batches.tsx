import React from 'react';
import PageHeading from '../components/PageHeading';
import {useBatchDelete, useBatches} from '../hooks/batch';
import {Table, Thead, Tbody, Tr, Th, Td, IconButton, Spinner, Link as ExLink, ButtonGroup, HStack} from '@chakra-ui/react';
import {generatePath} from 'react-router';
import {useQueryString} from '../hooks/common';
import {pageSize} from '../configuration/consts';
import Pagination from '../components/Pagination';
import Link from '../components/Link';
import {CloseIcon, ExternalLinkIcon} from '@chakra-ui/icons';
import {useConfiguration} from '../hooks/configuration';

const Batches: React.FC = () => {
  const from = Number(useQueryString().from) || 0;
  const {data, isLoading} = useBatches(pageSize, from);
  const {mutate: doDelete, isLoading: isDeleting} = useBatchDelete();
  const {data: conf} = useConfiguration();

  if (isLoading || isDeleting) {
    return <Spinner />;
  }

  return (
    <>
      <PageHeading>Batches</PageHeading>
      <Table variant="simple">
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
                <Link to={generatePath('./batches/:id', {id: batch.id})}>{batch.id}</Link>
              </Td>
              <Td>{batch.submitParams.name}</Td>
              <Td>{batch.createdAt}</Td>
              <Td>{batch.state}</Td>
              <Td>
                <HStack>
                  {!!conf?.sparkHistoryServerUrl && !!batch.appId && (
                    <IconButton
                      icon={<ExternalLinkIcon />}
                      title="History"
                      aria-label="History"
                      as={ExLink}
                      target="_blank"
                      href={`${conf?.sparkHistoryServerUrl}/history/${batch.appId}/jobs`}
                    />
                  )}
                  <IconButton title="Delete" aria-label="Delete" onClick={() => doDelete(batch.id)} icon={<CloseIcon />} />
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

export default Batches;
