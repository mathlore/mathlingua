import React from 'react';
import { TopLevelNodeType } from '../../types';
import { GroupView } from './GroupView';
import { TextBlockView } from './TextBlockView';

export interface TopLevelNodeTypeViewProps {
  node: TopLevelNodeType;
}

export const TopLevelNodeTypeView = (props: TopLevelNodeTypeViewProps) => {
  const isGroup = (props.node as any).Sections;
  if (isGroup) {
    return (
      <div style={styles.mathlinguaTopLevelEntity}>
        <GroupView node={props.node as any} indent={0} />
      </div>
    );
  } else {
    return (
      <div style={styles.mathlinguaTopLevelTextBlock}>
        <TextBlockView node={props.node as any} />
      </div>
    );
  }
};

const styles = {
  mathlinguaTopLevelEntity: {
    fontFamily: 'monospace',
    boxShadow: '0 1px 5px rgba(0,0,0,.2)',
    padding: '2ex',
    margin: '1ex',
    width: 'max-content',
    height: 'max-content',
    maxWidth: '50%',
    overflow: 'auto',
    marginLeft: 'auto',
    marginRight: 'auto',
  },
  mathlinguaTopLevelTextBlock: {
    color: '#555555',
    maxWidth: '100%',
  },
};
