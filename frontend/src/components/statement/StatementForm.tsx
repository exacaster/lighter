import {Button, Card, CardBody, FormControl, FormLabel, HStack, Spacer, Textarea, VStack} from '@chakra-ui/react';
import React from 'react';
import {useSessionStatementSubmit} from '../../hooks/session';
import {Application} from '../../client/types';

interface StatementFormProps {
  session: Application;
}

const StatementForm: React.FC<StatementFormProps> = ({session}) => {
  const {mutateAsync: submit, isPending: isSubmitting} = useSessionStatementSubmit(session.id);
  const handleSubmit = (event: React.FormEvent) => {
    // @ts-expect-error - wrong type definition
    const code = event.target.elements.code.value;
    // @ts-expect-error - wrong type definition
    submit({code}).then(() => (event.target.elements.code.value = ''));
    event.preventDefault();
  };

  if (session.state !== 'idle') {
    return (
      <Card align="center">
        <CardBody>Session cannot accept new statements.</CardBody>
      </Card>
    );
  }

  return (
    <form onSubmit={handleSubmit}>
      <Card>
        <CardBody>
          <VStack align="stretch" spacing={2}>
            <FormControl>
              <FormLabel>New Statement</FormLabel>
              <Textarea name="code" />
            </FormControl>
            <FormControl>
              <HStack>
                <Spacer />
                <Button type="submit" isLoading={isSubmitting}>
                  Submit
                </Button>
              </HStack>
            </FormControl>
          </VStack>
        </CardBody>
      </Card>
    </form>
  );
};

export default StatementForm;
