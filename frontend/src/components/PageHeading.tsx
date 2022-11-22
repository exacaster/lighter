import {Heading, HeadingProps} from '@chakra-ui/layout';
import {ReactNode} from 'react';

interface Props {
  children: ReactNode;
  mb?: HeadingProps['mb'];
}

const PageHeading: React.FC<Props> = ({children, mb = '5'}) => {
  return (
    <>
      <Heading size="lg" mb={mb}>
        {children}
      </Heading>
    </>
  );
};

export default PageHeading;
