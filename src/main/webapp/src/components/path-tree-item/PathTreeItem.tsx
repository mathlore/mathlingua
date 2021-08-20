import { useState } from 'react';
import { Link } from 'react-router-dom';
import styles from './PathTreeItem.module.css';

import { sidePanelVisibilityChanged } from '../../store/sidePanelVisibleSlice';
import { useAppDispatch, useAppSelector } from '../../support/hooks';
import { isOnMobile } from '../../support/util';
import {
  selectViewedPath,
  viewedPathUpdated,
} from '../../store/viewedPathSlice';
import { selectIsEditMode } from '../../store/isEditModeSlice';
import { selectErrorResults } from '../../store/errorResultsSlice';

export interface PathTreeNode {
  name: string;
  isDir: boolean;
  path?: string;
  children: PathTreeNode[];
}

export interface PathTreeItemProps {
  node: PathTreeNode;
}

export const PathTreeItem = (props: PathTreeItemProps) => {
  const dispatch = useAppDispatch();
  const [isExpanded, setIsExpanded] = useState(false);
  const viewedPath = useAppSelector(selectViewedPath) || '';
  const isEditMode = useAppSelector(selectIsEditMode);
  const allErrorResults = useAppSelector(selectErrorResults);
  const thisErrorResults = props.node.isDir
    ? []
    : allErrorResults.filter((err) => err.relativePath === props.node.path);

  const getErrorStats = () => {
    if (!isEditMode || thisErrorResults.length === 0) {
      return null;
    }
    const title = thisErrorResults.length === 1 ? ' error' : ' errors';
    return (
      <span className={styles.errorStats}>
        {' '}
        ({thisErrorResults.length} {title})
      </span>
    );
  };

  if (props.node.isDir) {
    return (
      <span>
        <li
          className={styles.mathlinguaListDirItem + ' ' + styles.sidePanelItem}
          onClick={() => setIsExpanded(!isExpanded)}
        >
          {isExpanded ? (
            <button className={styles.triangle}>&#9662;</button>
          ) : (
            <button className={styles.triangle}>&#9656;</button>
          )}
          {props.node.name.replace('_', ' ')}
          {getErrorStats()}
        </li>
        {isExpanded ? (
          <ul>
            {props.node.children.map((child) => (
              <PathTreeItem key={child.name} node={child} />
            ))}
          </ul>
        ) : null}
      </span>
    );
  }

  return (
    <li className={styles.mathlinguaListFileItem + ' ' + styles.sidePanelItem}>
      <Link
        to={`/${props.node.path}`}
        key={props.node.name}
        className={
          viewedPath === props.node.path
            ? `${styles.link} ${styles.selected}`
            : styles.link
        }
        onClick={() => {
          if (isEditMode || isOnMobile()) {
            dispatch(sidePanelVisibilityChanged(false));
          }
          dispatch(viewedPathUpdated(props.node.path));
        }}
      >
        {props.node.name.replace('.math', '').replace('_', ' ')}
        {getErrorStats()}
      </Link>
    </li>
  );
};
