import React, {useMemo} from 'react';
import {SessionStatement} from '../../client/types';
import {useSessionStatementCancel} from '../../hooks/session';
import {Box, Card, Flex, IconButton, Spinner, VStack} from '@chakra-ui/react';
import {CodeBlock, a11yLight} from 'react-code-blocks';
import StatementOutput from './StatementOutput';
import {FaCheck, FaStop} from 'react-icons/fa';
import {RiErrorWarningFill} from 'react-icons/ri';

const Statement: React.FC<{sessionId: string; statement: SessionStatement}> = ({sessionId, statement}) => {
  const {mutate: cancel, isPending: isCanceling} = useSessionStatementCancel(sessionId, statement.id);

  const statusIcon = useMemo(() => {
    switch (statement.state) {
      case 'available':
        return <FaCheck color="green.500" />;
      case 'canceled':
        return <FaStop />;
      case 'error':
        return <RiErrorWarningFill color="red.500" />;
      case 'waiting':
        return <Spinner />;
    }
  }, [statement.state]);

  return (
    <Card.Root>
      <Card.Body>
        <VStack align="stretch" gap={1}>
          <Flex gap={2}>
            <Box flex={1}>
              <CodeBlock theme={a11yLight} language="python" text={statement.code} />
              <StatementOutput output={statement.output} />
            </Box>
            <Box>
              <VStack>
                {statusIcon}
                {statement.state !== 'canceled' ? (
                  <IconButton onClick={() => cancel()} loading={isCanceling} aria-label="Cancel">
                    <FaStop />
                  </IconButton>
                ) : null}
              </VStack>
            </Box>
          </Flex>
        </VStack>
      </Card.Body>
    </Card.Root>
  );
};

export default Statement;
