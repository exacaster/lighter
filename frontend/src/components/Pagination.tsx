import {Spacer, Stack} from '@chakra-ui/layout';
import React from 'react';
import {generatePath} from 'react-router';
import ButtonLink from './ButtonLink';

interface PaginationProps {
  path: string;
  size: number;
  from: number;
  visibleSize: number;
}

const Pagination: React.FC<PaginationProps> = ({path, size, from, visibleSize}) => {
  if (from === 0 && visibleSize < size) {
    return null;
  }

  return <Stack borderWidth="1px" borderRadius="lg" padding="4" mt="5" direction="row" spacing={4}>
    <Spacer />
    {from > 0 && <ButtonLink size="sm" to={generatePath(path + `?from=${from - size}`)}>Previous</ButtonLink>}
    {visibleSize === size && <ButtonLink size="sm" to={generatePath(path + `?from=${from + size}`)}>Next</ButtonLink>}
  </Stack>;
};

export default Pagination;
