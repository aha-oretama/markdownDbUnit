package aha.oretama.jp;

import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author aha-oretama
 */
public class MarkdownProducer implements IDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(MarkdownProducer.class);
    private static final IDataSetConsumer EMPTY_CONSUMER = new DefaultConsumer();
    private IDataSetConsumer _consumer;
    private File file;

    private static final List<String> EXTENSIONS = Arrays.asList("md", "markdown");
    private static final MutableDataHolder OPTIONS = new MutableDataSet()
        .set(Parser.EXTENSIONS, Collections.singletonList(TablesExtension.create()));

    public MarkdownProducer(File file) {
        this._consumer = EMPTY_CONSUMER;
        this.file = file;
    }

    public void setConsumer(IDataSetConsumer consumer) throws DataSetException {
        logger.debug("setConsumer(consumer) - start");
        this._consumer = consumer;
    }

    public void produce() throws DataSetException {
        logger.debug("produce() - start");
        String path = file.getAbsolutePath();
        if (!file.isFile() || !EXTENSIONS.contains(FilenameUtils.getExtension(path))) {
            throw new DataSetException("'" + path + "' should be a file");
        }

        this._consumer.startDataSet();

        try {
            produceFromMarkdown();
        } catch (IOException ioEx) {
            throw new DataSetException("error reading file '" + path + "'", ioEx);
        }

        this._consumer.endDataSet();
    }

    private void produceFromMarkdown() throws IOException, DataSetException {
        produceFromMarkdown(FileUtils.readFileToString(file, Charset.defaultCharset()));
    }

    private void produceFromMarkdown(String markdown) throws DataSetException {
        Parser parser = Parser.builder(OPTIONS).build();
        Document document = parser.parse(markdown);

        Node node = document.getFirstChild();

        while (node != null) {
            String tableName = null;
            if (node instanceof Heading) {
                Text text = (Text) node.getFirstChild();
                tableName = text.getChars().trim().unescape();
            }

            if (node instanceof TableBlock) {
                Node tableNode = node.getFirstChild();
                while (tableNode != null) {

                    // make columns from TableHead.
                    if (tableNode instanceof TableHead) {
                        // TableHead -> TableRow -> TableCell
                        Node tableCell = tableNode.getFirstChild().getFirstChild();
                        List<Column> columns = new ArrayList<Column>();
                        while (tableCell != null && tableCell instanceof TableCell) {
                            String columnName = ((TableCell) tableCell).getText().trim().unescape();
                            columns.add(new Column(columnName, DataType.UNKNOWN));

                            tableCell = tableCell.getNext();
                        }

                        ITableMetaData metaData =
                            new DefaultTableMetaData(tableName, columns.toArray(new Column[0]));
                        this._consumer.startTable(metaData);

                    // make rows from TableBody.
                    } else if (tableNode instanceof TableBody) {

                        // TableBody -> TableRow -> TableCell
                        Node tableCell = tableNode.getFirstChild().getFirstChild();
                        List<Object> row = new ArrayList<Object>();
                        while (tableCell != null && tableCell instanceof TableCell) {
                            String cell = ((TableCell) tableCell).getText().trim().unescape();
                            row.add(cell);

                            tableCell = tableCell.getNext();
                        }
                        this._consumer.row(row.toArray());
                    }

                    tableNode = tableNode.getNext();
                }
            }

            node = node.getNext();
        }
    }
}
