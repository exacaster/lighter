import React from 'react';
import {useParams} from 'react-router';
import PageHeading from '../components/PageHeading';
import {useApplicationLog} from '../hooks/batch';

const Batch: React.FC = () => {
  const {id} = useParams<{id: string}>();
  const {data: logs} = useApplicationLog(id);

  return <>
    <PageHeading>Batch {id}</PageHeading>
    {logs?.log}
  </>;
};

export default Batch;