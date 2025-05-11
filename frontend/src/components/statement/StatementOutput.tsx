import React from 'react';
import {SessionStatement} from '../../client/types';
import {Prism as SyntaxHighlighter} from 'react-syntax-highlighter';

const StatementOutput: React.FC<{output?: SessionStatement['output']}> = ({output}) => {
  if (output?.traceback) {
    return <SyntaxHighlighter>{output.traceback}</SyntaxHighlighter>;
  }

  if (!output?.data) {
    return null;
  }

  const text = String(Object.values(output.data)[0]);
  if (!text) {
    return null;
  }

  return <SyntaxHighlighter>{text}</SyntaxHighlighter>;
};

export default StatementOutput;
