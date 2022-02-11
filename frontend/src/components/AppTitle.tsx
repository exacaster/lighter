import {Grid, GridItem} from '@chakra-ui/react';
import React from 'react';
import {Application} from '../client/types';
import AppActions from './AppActions';
import AppStatus from './AppStatus';
import PageHeading from './PageHeading';

interface Props {
  app: Application;
}

const AppTitle: React.FC<Props> = ({app, children}) => {
  return (
    <PageHeading>
      <Grid templateColumns="repeat(5, 1fr)">
        <GridItem colSpan={4}>
          {children} <AppStatus status={app.state} />
        </GridItem>
        <GridItem colSpan={1} justifySelf="end">
          <AppActions app={app} />
        </GridItem>
      </Grid>
    </PageHeading>
  );
};

export default AppTitle;
