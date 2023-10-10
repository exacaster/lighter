import {Application} from '../client/types';
import {useStatements} from '../hooks/session';
import {Spinner, useColorMode, VStack} from '@chakra-ui/react';
import {a11yLight, a11yDark} from 'react-code-blocks';
import Statement from './statement/Statement';
import React from 'react';
import StatementForm from './statement/StatementForm';

interface StatementsProps {
  session: Application;
}

const Statements: React.FC<StatementsProps> = ({session}) => {
  const {data: page, isLoading} = useStatements(session.id, 5, 0);
  const {colorMode} = useColorMode();
  const theme = colorMode === 'light' ? a11yLight : a11yDark;

  if (isLoading) {
    return <Spinner />;
  }
  if (!page?.statements.length) {
    return <div>Session has no statements</div>;
  }

  return (
    <VStack align="stretch" spacing={2}>
      {page.statements.toReversed().map((statement) => (
        <Statement key={statement.id} sessionId={session.id} statement={statement} theme={theme} />
      ))}
      <StatementForm session={session} />
    </VStack>
  );
};

export default Statements;
