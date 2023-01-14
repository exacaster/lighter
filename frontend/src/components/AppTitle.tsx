import {Grid, GridItem, Text} from '@chakra-ui/react';
import React from 'react';
import {Application} from '../client/types';
import AppActions from './AppActions';
import AppStatus from './AppStatus';
import DateTime from './DateTime';
import PageHeading from './PageHeading';

interface Props {
  app: Application;
  onDelete?: () => void;
}

const AppTitle: React.FC<Props> = ({app, onDelete}) => {
  return (
    <>
      <PageHeading mb="3">
        <Grid templateColumns="repeat(5, 1fr)">
          <GridItem colSpan={4}>
            {app.submitParams.name} <AppStatus status={app.state} />
          </GridItem>
          <GridItem colSpan={1} justifySelf="end">
            <AppActions app={app} onDelete={onDelete} />
          </GridItem>
        </Grid>
      </PageHeading>
      <Text fontSize="sm" color="gray">
        <Text as="b">Application Id: </Text> {app.id}
        {' | '}
        <Text as="b">Created: </Text>
        <DateTime>{app.createdAt}</DateTime>
        {' | '}
        <Text as="b">Last checked: </Text>
        <DateTime>{app.contactedAt}</DateTime>
      </Text>
    </>
  );
};

export default AppTitle;
