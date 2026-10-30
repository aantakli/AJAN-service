package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

/** Extracts the Python environment zip (a pure function of zip and destination). */
final class ZipExtractor {

  private static final int S_IFMT = 0170000;
  private static final int S_IFLNK = 0120000;

  private ZipExtractor() {}

  /**
   * Extracts with zip-slip protection; restores Unix symlinks (stored with {@code zip -y}) and
   * permissions. Where a symlink cannot be created, its target is copied instead.
   */
  static void extract(Path zipPath, Path dest) throws IOException {
    Path root = dest.toAbsolutePath().normalize();
    boolean posix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    Map<Path, String> links = new LinkedHashMap<>();
    try (ZipFile zip = new ZipFile(zipPath.toFile())) {
      for (FileHeader h : zip.getFileHeaders()) {
        Path target = safeResolve(root, h.getFileName());
        if (h.isDirectory()) {
          Files.createDirectories(target);
          continue;
        }
        Files.createDirectories(target.getParent());
        int mode = unixMode(h);
        if ((mode & S_IFMT) == S_IFLNK) {
          try (InputStream in = zip.getInputStream(h)) {
            links.put(target, new String(in.readAllBytes(), StandardCharsets.UTF_8));
          }
          continue;
        }
        try (InputStream in = zip.getInputStream(h)) {
          Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        if (posix && (mode & 0777) != 0) {
          Files.setPosixFilePermissions(target, permissions(mode));
        }
      }
    }
    createLinks(root, links);
    // The lexical check in createLinks can be bypassed through other links: check real targets.
    Path realRoot = root.toRealPath();
    for (Path link : links.keySet()) {
      if (Files.exists(link) && !link.toRealPath().startsWith(realRoot)) {
        throw new IOException(
            "zip symlink escapes the extraction directory: " + link + " -> " + link.toRealPath());
      }
    }
  }

  private static void createLinks(Path root, Map<Path, String> links) throws IOException {
    List<Map.Entry<Path, String>> pending = new ArrayList<>();
    for (Map.Entry<Path, String> link : links.entrySet()) {
      Path resolved = link.getKey().getParent().resolve(link.getValue()).normalize();
      if (Paths.get(link.getValue()).isAbsolute() || !resolved.startsWith(root)) {
        throw new IOException(
            "zip symlink escapes the extraction directory: " + link.getKey() + " -> "
                + link.getValue());
      }
      try {
        Files.createSymbolicLink(link.getKey(), Paths.get(link.getValue()));
      } catch (IOException | UnsupportedOperationException e) {
        pending.add(link);
      }
    }
    // Fallback (no symlink privilege): copy targets; repeat for chains of links.
    boolean progress = true;
    while (!pending.isEmpty() && progress) {
      progress = false;
      for (var it = pending.iterator(); it.hasNext(); ) {
        Map.Entry<Path, String> link = it.next();
        Path resolved = link.getKey().getParent().resolve(link.getValue()).normalize();
        if (Files.isRegularFile(resolved)) {
          Files.copy(resolved, link.getKey(), StandardCopyOption.REPLACE_EXISTING);
          it.remove();
          progress = true;
        }
      }
    }
    if (!pending.isEmpty()) {
      throw new IOException("cannot materialize zip symlinks: " + pending);
    }
  }

  private static Path safeResolve(Path root, String entryName) throws IOException {
    String name = entryName.replace('\\', '/');
    if (name.startsWith("/") || name.matches("^[A-Za-z]:.*")) {
      throw new IOException("absolute zip entry rejected: " + entryName);
    }
    Path target = root.resolve(name).normalize();
    if (!target.startsWith(root) || target.equals(root)) {
      throw new IOException("zip entry escapes the extraction directory: " + entryName);
    }
    return target;
  }

  private static int unixMode(FileHeader h) {
    byte[] attrs = h.getExternalFileAttributes();
    if (attrs == null || attrs.length < 4 || (h.getVersionMadeBy() >> 8) != 3) {
      return 0;
    }
    return ((attrs[3] & 0xff) << 8) | (attrs[2] & 0xff);
  }

  private static Set<PosixFilePermission> permissions(int mode) {
    Set<PosixFilePermission> perms = EnumSet.noneOf(PosixFilePermission.class);
    PosixFilePermission[] order = {
      PosixFilePermission.OTHERS_EXECUTE, PosixFilePermission.OTHERS_WRITE,
      PosixFilePermission.OTHERS_READ, PosixFilePermission.GROUP_EXECUTE,
      PosixFilePermission.GROUP_WRITE, PosixFilePermission.GROUP_READ,
      PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_WRITE,
      PosixFilePermission.OWNER_READ
    };
    for (int i = 0; i < order.length; i++) {
      if ((mode & (1 << i)) != 0) {
        perms.add(order[i]);
      }
    }
    perms.add(PosixFilePermission.OWNER_READ);
    perms.add(PosixFilePermission.OWNER_WRITE);
    return perms;
  }
}
