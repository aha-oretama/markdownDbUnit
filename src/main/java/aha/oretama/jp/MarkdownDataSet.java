package aha.oretama.jp;

import java.io.File;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;

/**
 * @author aha-oretama
 */
public class MarkdownDataSet extends CachedDataSet {

    public MarkdownDataSet(File dir) throws DataSetException {
        super(new MarkdownProducer(dir));
    }

}
