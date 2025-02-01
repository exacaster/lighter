import {Stack, Text} from '@chakra-ui/react';
import {generatePath, Link} from 'react-router-dom';
import AppStatus, {statusMap} from './AppStatus';
import {FaCheck} from 'react-icons/fa';

interface Props {
  status?: string;
  path: string;
}
const StatusFilter: React.FC<Props> = ({status, path}) => {
  return (
    <Stack borderWidth="1px" borderRadius="lg" padding="4" mt="5" mb="5" direction="row" gap={4}>
      <Text>Filter by status:</Text>
      <Link to={generatePath(path)}>
        <AppStatus prefix={!status ? <FaCheck /> : null} status="ALL" />
      </Link>
      {Object.keys(statusMap).map((key) => (
        <Link to={generatePath(path + `?status=${key}`)} key={key}>
          <AppStatus prefix={status === key ? <FaCheck size={10} /> : null} status={key} />
        </Link>
      ))}
    </Stack>
  );
};

export default StatusFilter;
