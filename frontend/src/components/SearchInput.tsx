import React, {useState} from 'react';
import {Input, InputGroup, IconButton} from '@chakra-ui/react';
import {LuX} from 'react-icons/lu';

const SearchInput: React.FC<{initialValue: string; onSearch: (value: string) => void}> = ({initialValue, onSearch}) => {
  const [value, setValue] = useState(initialValue);

  const clear = () => {
    setValue('');
    onSearch('');
  };

  return (
    <InputGroup
      endElement={
        value && (
          <IconButton size="xs" variant="ghost" aria-label="Clear search" onClick={clear}>
            <LuX />
          </IconButton>
        )
      }
    >
      <Input
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder="Search by id or name... (press Enter)"
        onKeyDown={(e) => e.key === 'Enter' && onSearch(value)}
      />
    </InputGroup>
  );
};

export default SearchInput;
