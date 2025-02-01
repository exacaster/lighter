import {Button, Card, Center, Field, HStack, Spacer, Textarea, VStack} from '@chakra-ui/react';
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
      <Card.Root>
        <Card.Body>
          <Center>Session cannot accept new statements.</Center>
        </Card.Body>
      </Card.Root>
    );
  }

  return (
    <form onSubmit={handleSubmit}>
      <Card.Root>
        <Card.Body>
          <VStack align="stretch" gap={2}>
            <Field.Root>
              <Field.Label>New Statement</Field.Label>
              <Textarea name="code" />
            </Field.Root>
            <Field.Root>
              <HStack>
                <Spacer />
                <Button type="submit" loading={isSubmitting}>
                  Submit
                </Button>
              </HStack>
            </Field.Root>
          </VStack>
        </Card.Body>
      </Card.Root>
    </form>
  );
};

export default StatementForm;
