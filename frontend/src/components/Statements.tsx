import {Application} from '../client/types';
import {useStatements} from '../hooks/session';
import {Card, Spinner, VStack} from '@chakra-ui/react';
import Statement from './statement/Statement';
import React from 'react';
import StatementForm from './statement/StatementForm';

interface StatementsProps {
  session: Application;
}

const Statements: React.FC<StatementsProps> = ({session}) => {
  const {data: page, isLoading} = useStatements(session.id, 5, 0);

  if (isLoading) {
    return <Spinner />;
  }

  return (
    <VStack align="stretch" gap={2}>
      {!page?.statements.length && (
        <Card.Root>
          <Card.Body>Session has no statements</Card.Body>
        </Card.Root>
      )}
      {page?.statements.reverse().map((statement) => <Statement key={statement.id} sessionId={session.id} statement={statement} />)}
      <StatementForm session={session} />
    </VStack>
  );
};

export default Statements;
