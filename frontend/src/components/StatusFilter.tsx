import {Stack, Text} from '@chakra-ui/react';
import {generatePath, Link, useSearchParams} from 'react-router-dom';
import AppStatus, {statusMap} from './AppStatus';
import {FaCheck} from 'react-icons/fa';

interface Props {
  status?: string | null;
  path: string;
}
const StatusFilter: React.FC<Props> = ({status, path}) => {
  const [queryParams] = useSearchParams();
  const statusPath = (newStatus?: string) => {
    const params = new URLSearchParams(queryParams);
    params.delete('from');
    if (newStatus) {
      params.set('status', newStatus);
    } else {
      params.delete('status');
    }
    return generatePath(path) + '?' + params.toString();
  };

  return (
    <Stack borderWidth="1px" borderRadius="lg" padding="4" mt="5" mb="5" direction="row" gap={4}>
      <Text>Filter by status:</Text>
      <Link to={statusPath()}>
        <AppStatus prefix={!status ? <FaCheck /> : null} status="ALL" />
      </Link>
      {Object.keys(statusMap).map((key) => (
        <Link to={statusPath(key)} key={key}>
          <AppStatus prefix={status === key ? <FaCheck size={10} /> : null} status={key} />
        </Link>
      ))}
    </Stack>
  );
};

export default StatusFilter;
