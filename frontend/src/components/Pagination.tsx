import React from 'react';
import {generatePath} from 'react-router';
import {toQueryString} from '../hooks/common';
import ButtonLink from './ButtonLink';
import {Spacer, Stack} from '@chakra-ui/react';
import {useSearchParams} from 'react-router-dom';

interface PaginationProps {
  path: string;
  size: number;
  visibleSize: number;
}

const Pagination: React.FC<PaginationProps> = ({path, size, visibleSize}) => {
  const [queryParams] = useSearchParams();
  const from = Number(queryParams.get('from')) || 0;
  const queryString = (from: number) => {
    return toQueryString({...queryParams, from});
  };

  if (from === 0 && visibleSize < size) {
    return null;
  }

  return (
    <Stack borderWidth="1px" borderRadius="lg" padding="4" mt="5" direction="row" gap={4}>
      <Spacer />
      {from > 0 && (
        <ButtonLink size="sm" to={generatePath(path + queryString(from - size))}>
          Previous
        </ButtonLink>
      )}
      {visibleSize === size && (
        <ButtonLink size="sm" to={generatePath(path + queryString(from + size))}>
          Next
        </ButtonLink>
      )}
    </Stack>
  );
};

export default Pagination;
