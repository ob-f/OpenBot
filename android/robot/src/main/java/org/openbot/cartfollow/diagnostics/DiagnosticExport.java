package org.openbot.cartfollow.diagnostics;

import java.io.*;
import java.util.Arrays;
import java.util.zip.*;

/** Local-only ZIP export of a closed or interrupted diagnostic session. */
public final class DiagnosticExport {
  private DiagnosticExport() {}

  public static void writeZip(File session, OutputStream output) throws IOException {
    CartFollowDiagnosticSession active = CartFollowDiagnosticSession.active(session);
    if (active != null && !active.io.isClosed()) throw new IOException("请先停止测试，等待日志收尾");
    if (!session.isDirectory()) throw new IOException("测试记录不存在");
    try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(output))) {
      add(session.getCanonicalFile(), session.getCanonicalFile(), zip);
    }
  }

  private static void add(File root, File dir, ZipOutputStream zip) throws IOException {
    File[] files = dir.listFiles();
    if (files == null) throw new IOException("无法读取记录目录");
    Arrays.sort(files, (a, b) -> a.getName().compareTo(b.getName()));
    for (File file : files) {
      File canonical = file.getCanonicalFile();
      if (!canonical.getPath().startsWith(root.getPath() + File.separator))
        throw new IOException("记录路径越界");
      if (file.isDirectory()) add(root, canonical, zip);
      else if (!file.getName().endsWith(".tmp")) {
        zip.putNextEntry(
            new ZipEntry(
                canonical.getPath().substring(root.getPath().length() + 1).replace('\\', '/')));
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
          byte[] buffer = new byte[32768];
          int count;
          while ((count = in.read(buffer)) >= 0) zip.write(buffer, 0, count);
        }
        zip.closeEntry();
      }
    }
  }
}
