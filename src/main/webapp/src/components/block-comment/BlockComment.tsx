import RenderedComponent from '../rendered-component/RenderedComponent';
import styles from './BlockComment.module.css';

export interface BlockCommentProps {
  rawHtml: string;
  renderedHtml: string;
}

export const BlockComment = (props: BlockCommentProps) => {
  return (
    <div
      className={styles.mathlinguaBlockCommentTopLevel}
      dangerouslySetInnerHTML={{
        __html: props.renderedHtml,
      }}
    ></div>
  );
};
