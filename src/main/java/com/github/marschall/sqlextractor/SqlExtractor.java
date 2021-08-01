package com.github.marschall.sqlextractor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;

public final class SqlExtractor {

  private static final byte LATIN1 = 0;
  private static final byte UTF16  = 1;

  void parse(File heapDumpFile) throws IOException {
    Heap heap = HeapFactory.createHeap(heapDumpFile);
    JavaClass oracleSqlClass = heap.getJavaClassByName("oracle.jdbc.driver.OracleSql");
    Iterator<?> instancesIterator = oracleSqlClass.getInstancesIterator();
    while (instancesIterator.hasNext()) {
      Instance oracleSql = (Instance) instancesIterator.next();
      Instance originalSql = (Instance) oracleSql.getValueOfField("originalSql");
      System.out.println(toString(originalSql));
    }
  }

  private static String toString(Instance string) {
    PrimitiveArrayInstance value = (PrimitiveArrayInstance) string.getValueOfField("value");
    String valueClassName = value.getJavaClass().getName();
    if (valueClassName.equals("char[]")) {
      char[] c = new char[value.getLength()];
      int i = 0;
      for (Object each : value.getValues()) {
        c[i++] = each.toString().charAt(0);
      }
      return new String(c);
    } else if (valueClassName.equals("byte[]")) {
      byte[] b = new byte[value.getLength()];
      int i = 0;
      for (Object each : value.getValues()) {
        b[i++] = Byte.parseByte(each.toString());
      }
      Byte coder = (Byte) string.getValueOfField("coder");
      return new String(b, getCharset(coder));
    } else {
      throw new IllegalStateException("unknown field type: " + valueClassName);
    }
  }

  private static Charset getCharset(byte coder) {
    if (coder == LATIN1) {
      return StandardCharsets.ISO_8859_1;
    } else if (coder == UTF16) {
      return StandardCharsets.UTF_16;
    } else {
      throw new IllegalStateException("unknown coder: " + coder);
    }
  }

  public static void main(String[] args) throws IOException {
//    if (args.length == 0) {
//      System.err.println("usage: heap dump file");
//      System.exit(1);
//    }
//    File heapDumpFile = new File(args[0]);
    File heapDumpFile = new File("/home/marschall/git/sqlid/sqlid/src/test/resources/lobprefetching11.hprof");
    SqlExtractor extractor = new SqlExtractor();
    extractor.parse(heapDumpFile);
  }

}
