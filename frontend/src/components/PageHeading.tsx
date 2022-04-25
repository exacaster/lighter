import {Heading} from '@chakra-ui/layout';
import {ReactNode} from 'react';

interface Props {
  children: ReactNode;
}

const PageHeading: React.FC<Props> = ({children}) => {
  return (
    <>
      <Heading size="lg" mb="5">
        {children}
      </Heading>
    </>
  );
};

export default PageHeading;
