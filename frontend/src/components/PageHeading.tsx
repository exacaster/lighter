import {Heading} from '@chakra-ui/layout';

const PageHeading: React.FC = ({children}) => {
  return (
    <>
      <Heading size="lg" mb="5">
        {children}
      </Heading>
    </>
  );
};

export default PageHeading;
