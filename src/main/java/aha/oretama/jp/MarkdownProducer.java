package aha.oretama.jp;

import com.vladsch.flexmark.ast.NodeVisitor;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableCaption;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.ext.tables.TableSeparator;
import com.vladsch.flexmark.ext.tables.TableVisitorExt;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sekineyasufumi on 2017/08/22.
 */
public class MarkdownProducer implements IDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(MarkdownProducer.class);
    private static final IDataSetConsumer EMPTY_CONSUMER = new DefaultConsumer();
    private IDataSetConsumer _consumer;
    private String _theFile;

    private static final List<String> EXTENSIONS = Arrays.asList(".md", ".markdown");

    public MarkdownProducer(File theFile) {
        this._consumer = EMPTY_CONSUMER;
        this._theFile = theFile.getAbsolutePath();
    }

    public void setConsumer(IDataSetConsumer consumer) throws DataSetException {
        logger.debug("setConsumer(consumer) - start");
        this._consumer = consumer;
    }

    public void produce() throws DataSetException {
        logger.debug("produce() - start");
        File markdown = new File(this._theFile);
        if (!markdown.isFile() || !EXTENSIONS.contains(FilenameUtils.getExtension(markdown.getAbsolutePath()))) {
            throw new DataSetException("'" + this._theFile + "' should be a file");
        }

        this._consumer.startDataSet();

        produceFromMarkdown(markdown);

        this._consumer.endDataSet();
    }

    private void produceFromMarkdown(File file) {
        NodeVisitor visitor = new NodeVisitor(TableVisitorExt.VISIT_HANDLERS(new TableVisitor()));
        Parser parser = Parser.builder().build();
        try {
            visitor.visit(parser.parse(FileUtils.readFileToString(file, Charset.defaultCharset())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static class TableVisitor implements com.vladsch.flexmark.ext.tables.TableVisitor {

        public void visit(TableBlock tableBlock) {
            System.out.println(tableBlock.toAstString(false));
        }

        public void visit(TableHead tableHead) {
            System.out.println(tableHead.toAstString(false));

            BasedSequence[] basedSequences =tableHead.getSegments();

            Column[] columns = new Column[basedSequences.length];
            for(int i = 0; i < basedSequences.length; ++i) {
                String columnName = basedSequences[i].unescape();
                columnName = columnName.trim();
                columns[i] = new Column(columnName, DataType.UNKNOWN);
            }
        }

        public void visit(TableSeparator tableSeparator) {
            System.out.println(tableSeparator.toAstString(false));
        }

        public void visit(TableBody tableBody) {
            System.out.println(tableBody.toAstString(false));
        }

        public void visit(TableRow tableRow) {
            System.out.println(tableRow.toAstString(false));
        }

        public void visit(TableCell tableCell) {
            System.out.println(tableCell.toAstString(false));
        }

        public void visit(TableCaption tableCaption) {
            System.out.println(tableCaption.toAstString(false));
        }

    }
}
