import React, {useMemo} from 'react';
import {SessionStatement} from '../../client/types';
import {useSessionStatementCancel} from '../../hooks/session';
import {CheckIcon, CloseIcon, WarningTwoIcon} from '@chakra-ui/icons';
import {Box, Card, CardBody, Flex, IconButton, Spinner, VStack} from '@chakra-ui/react';
import {CodeBlock} from 'react-code-blocks';
import StatementOutput from './StatementOutput';

// eslint-disable-next-line
const Statement: React.FC<{sessionId: string; statement: SessionStatement; theme: any}> = ({sessionId, statement, theme}) => {
  const {mutate: cancel, isPending: isCanceling} = useSessionStatementCancel(sessionId, statement.id);

  const statusIcon = useMemo(() => {
    switch (statement.state) {
      case 'available':
        return <CheckIcon color="green.500" />;
      case 'to_cancel':
        return <CloseIcon />;
      case 'canceled':
        return <CloseIcon />;
      case 'error':
        return <WarningTwoIcon color="red.500" />;
      case 'waiting':
        return <Spinner />;
    }
  }, [statement.state]);

  return (
    <Card>
      <CardBody>
        <VStack align="stretch" spacing={1}>
          <Flex gap={2}>
            <Box flex={1}>
              <CodeBlock language="python" text={statement.code} theme={theme} />
              <StatementOutput theme={theme} output={statement.output} />
            </Box>
            <Box>
              <VStack>
                {statusIcon}
                {statement.state !== 'canceled' ? (
                  <IconButton onClick={() => cancel()} isLoading={isCanceling} aria-label="Cancel" icon={<CloseIcon />} />
                ) : null}
              </VStack>
            </Box>
          </Flex>
        </VStack>
      </CardBody>
    </Card>
  );
};

export default Statement;
