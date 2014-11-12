import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import static org.fest.assertions.Assertions.assertThat;

public class SampleTest {

    public static final String testTableName = "test_table";
    public static final byte[] cf1 = Bytes.toBytes("cf1");
    public static final byte[] column = Bytes.toBytes("column");
    private HBaseTestingUtility hBaseTestingUtility;

    @Before
    public void setUp() throws Exception {
        startClusters();
        createTestTable();
    }

    @After
    public void tearDown() throws Exception {
        hBaseTestingUtility.shutdownMiniMapReduceCluster();
        hBaseTestingUtility.shutdownMiniCluster();
    }

    @Test
    public void shouldCountRowsInHBase() throws Exception {

        // Given
        Configuration configuration = hBaseTestingUtility.getConfiguration();

        // When
        populateTestTable();

        // Then
        int numberOfRowsInHBase = countRows(new HTable(configuration, testTableName));
        assertThat(numberOfRowsInHBase).isEqualTo(2);
    }

    private int countRows(HTable testTable) throws IOException {
        int rows = 0;
        ResultScanner scanner = testTable.getScanner(new Scan());
        while (scanner.next() != null) {
            rows++;
        }
        return rows;
    }

    private void startClusters() throws Exception {
        hBaseTestingUtility = new HBaseTestingUtility();
        hBaseTestingUtility.startMiniMapReduceCluster();
        hBaseTestingUtility.startMiniCluster();
    }

    private void createTestTable() throws IOException {
        HTableDescriptor hTableDescriptor = new HTableDescriptor(testTableName);
        hTableDescriptor.addFamily(new HColumnDescriptor(cf1));
        hBaseTestingUtility.getHBaseAdmin().createTable(hTableDescriptor);
    }

    private byte[] key(String keyText) {
        return toBytes(keyText);
    }

    private void populateTestTable() throws IOException {
        HTable table = new HTable(hBaseTestingUtility.getConfiguration(), testTableName);

        Put put;

        put = new Put(key("some key1"));
        put.add(cf1, column, Bytes.toBytes("some-value-1"));
        table.put(put);

        put = new Put(key("some key2"));
        put.add(cf1, column, Bytes.toBytes("some-value-2"));
        table.put(put);

        table.flushCommits();
        table.close();
    }

}

