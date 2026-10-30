package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Locates the CPython installation (with jep) that the PythonPlugin embeds via JEP.
 *
 * <p>Resolution order: system property {@value #HOME_PROPERTY}, then environment variable
 * {@value #HOME_ENV} (an external CPython with jep installed), then the environment zip embedded in
 * the plugin JAR for the current OS ({@code /nix_env.zip} or {@code /win_env.zip}). The zip is
 * extracted into {@code <cache>/<sha256-12>/}, where {@code <cache>} is {@value
 * #CACHE_DIR_PROPERTY} or a per-user directory ({@code ~/.cache/ajan-python}, Windows {@code
 * %LOCALAPPDATA%\ajan-python}) — never a shared temp dir, because native libraries are loaded
 * from it. On POSIX the cache directories are created 0700 and refused (Python unavailable) when
 * they are symlinks, owned by another user or group/other-writable. Extraction happens in a
 * private {@code .extract-*} temp dir that receives the marker {@value #COMPLETE_MARKER} and is
 * then renamed atomically, so {@code <sha256-12>/} is either absent or complete; an unmarked one
 * (older versions) is redone. Other {@code <sha256-12>} directories are removed without following
 * symlinks. {@code AJANlib.py} (a
 * resource of the plugin JAR) is written to an {@code ajan-lib/} directory that goes on {@code
 * sys.path}.
 */
public final class PythonEnvironment {

  public static final String HOME_PROPERTY = "ajan.python.home";
  public static final String HOME_ENV = "AJAN_PYTHON_HOME";
  public static final String CACHE_DIR_PROPERTY = "ajan.python.cacheDir";
  static final String COMPLETE_MARKER = ".complete";
  static final String AJAN_LIB_DIR = "ajan-lib";
  static final String AJAN_LIB_RESOURCE = "/ajan-lib/AJANlib.py";
  static final String NIX_ZIP = "/nix_env.zip";
  static final String WIN_ZIP = "/win_env.zip";

  private static final Logger LOG = LoggerFactory.getLogger(PythonEnvironment.class);
  private static final Pattern CACHE_ENTRY = Pattern.compile("[0-9a-f]{12}");
  private static final Pattern WIN_PYTHON_DLL = Pattern.compile("python3\\d*\\.dll");
  private static final Pattern NIX_LIBPYTHON = Pattern.compile("libpython3\\.\\d+\\.so\\.1\\.0");
  private static final int S_IFMT = 0170000;
  private static final int S_IFLNK = 0120000;

  /** Opens a classpath resource; returns {@code null} when it does not exist. */
  @FunctionalInterface
  interface ResourceLoader {
    InputStream open(String name) throws IOException;
  }

  private final Path home;
  private final Path sitePackages;
  private final Path jepLibrary;
  private final Path ajanLib;
  private final List<Path> nativePreloads;
  private final String source;

  private PythonEnvironment(
      Path home,
      Path sitePackages,
      Path jepLibrary,
      Path ajanLib,
      List<Path> nativePreloads,
      String source) {
    this.home = home;
    this.sitePackages = sitePackages;
    this.jepLibrary = jepLibrary;
    this.ajanLib = ajanLib;
    this.nativePreloads = List.copyOf(nativePreloads);
    this.source = source;
  }

  /** Python home (PYTHONHOME). */
  public Path home() {
    return home;
  }

  public Path sitePackages() {
    return sitePackages;
  }

  /** Absolute path of the jep JNI library (libjep.so / jep.dll). */
  public Path jepLibrary() {
    return jepLibrary;
  }

  /** Directory containing AJANlib.py; goes on sys.path. */
  public Path ajanLib() {
    return ajanLib;
  }

  /**
   * Native libraries to load before the jep library, in order: libpython (Linux) or the
   * python3XX.dll and its VC runtime (Windows). The JVM loads libraries by absolute path, so their
   * own dependencies are not searched next to them; preloading satisfies jep's dependency on
   * libpython by soname/module name.
   */
  public List<Path> nativePreloads() {
    return nativePreloads;
  }

  /** Human-readable origin (property, env var or embedded zip). */
  public String source() {
    return source;
  }

  @Override
  public String toString() {
    return "PythonEnvironment[home=" + home + ", source=" + source + "]";
  }

  public static PythonEnvironment resolve() throws PythonUnavailableException {
    return resolve(
        System.getProperty(HOME_PROPERTY),
        System.getenv(HOME_ENV),
        defaultCacheBase(),
        isWindows(),
        PythonEnvironment.class::getResourceAsStream);
  }

  /**
   * Per-user cache base (never a shared temp dir: extracted native libraries are loaded into the
   * JVM). {@value #CACHE_DIR_PROPERTY} overrides; otherwise {@code ~/.cache/ajan-python} on
   * POSIX and {@code %LOCALAPPDATA%\ajan-python} (fallback {@code ~\.ajan-python}) on Windows.
   */
  static Path defaultCacheBase() {
    String configured = System.getProperty(CACHE_DIR_PROPERTY);
    if (configured != null && !configured.isBlank()) {
      return Paths.get(configured).toAbsolutePath();
    }
    String userHome = System.getProperty("user.home");
    if (isWindows()) {
      String localAppData = System.getenv("LOCALAPPDATA");
      return (notBlank(localAppData)
              ? Paths.get(localAppData, "ajan-python")
              : Paths.get(userHome, ".ajan-python"))
          .toAbsolutePath();
    }
    return Paths.get(userHome, ".cache", "ajan-python").toAbsolutePath();
  }

  /** A cache directory that is not safe to load native code from. */
  static final class InsecureCacheDirException extends IOException {
    private static final long serialVersionUID = 1L;

    InsecureCacheDirException(String message) {
      super(message);
    }
  }

  private static final Set<PosixFilePermission> OWNER_ONLY =
      EnumSet.of(
          PosixFilePermission.OWNER_READ,
          PosixFilePermission.OWNER_WRITE,
          PosixFilePermission.OWNER_EXECUTE);

  private static boolean posix() {
    return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
  }

  /**
   * Creates {@code dir} (POSIX: mode 0700) if missing, then verifies it: a real directory (not a
   * symlink) and, on POSIX, owned by the current user and not group/other-writable.
   */
  static Path secureDir(Path dir) throws IOException {
    if (!Files.exists(dir, LinkOption.NOFOLLOW_LINKS)) {
      Path parent = dir.toAbsolutePath().getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try {
        if (posix()) {
          Files.createDirectory(dir, PosixFilePermissions.asFileAttribute(OWNER_ONLY));
        } else {
          Files.createDirectory(dir);
        }
      } catch (java.nio.file.FileAlreadyExistsException raced) {
        // created concurrently; verified below
      }
    }
    verifySecureDir(dir);
    return dir;
  }

  static void verifySecureDir(Path dir) throws InsecureCacheDirException, IOException {
    boolean symlink = Files.isSymbolicLink(dir);
    boolean directory = Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS);
    String reason;
    if (symlink || !directory || !posix()) {
      // Windows: per-user profile directory, protected by its ACLs
      reason = insecurityReason(symlink, directory, null, null, null);
    } else {
      reason =
          insecurityReason(
              false,
              true,
              Files.getOwner(dir, LinkOption.NOFOLLOW_LINKS),
              currentUser(dir),
              Files.getPosixFilePermissions(dir, LinkOption.NOFOLLOW_LINKS));
    }
    if (reason != null) {
      throw insecure(dir, reason);
    }
  }

  /**
   * Why a cache directory must not be used, or {@code null} if it is fine. Owner and permission
   * checks are skipped when {@code owner}/{@code perms} are {@code null} (non-POSIX).
   */
  static String insecurityReason(
      boolean symlink,
      boolean directory,
      UserPrincipal owner,
      UserPrincipal me,
      Set<PosixFilePermission> perms) {
    if (symlink) {
      return "is a symbolic link";
    }
    if (!directory) {
      return "is not a directory";
    }
    if (owner != null && me != null && !owner.equals(me)) {
      return "is owned by " + owner.getName() + ", not by " + me.getName();
    }
    if (perms != null
        && (perms.contains(PosixFilePermission.GROUP_WRITE)
            || perms.contains(PosixFilePermission.OTHERS_WRITE))) {
      return "is group/other-writable (" + PosixFilePermissions.toString(perms) + ")";
    }
    return null;
  }

  private static boolean isComplete(Path envDir) {
    return Files.isRegularFile(envDir.resolve(COMPLETE_MARKER), LinkOption.NOFOLLOW_LINKS);
  }

  /**
   * Extracts into a private temp dir, writes the marker, then renames it atomically to {@code
   * envDir}, so {@code envDir} is either absent or complete.
   */
  private static void extractAtomically(Path zip, String zipName, Path cacheBase, Path envDir)
      throws IOException {
    LOG.info("Extracting {} to {}", zipName, envDir);
    Path tmpDir = Files.createTempDirectory(cacheBase, ".extract-"); // 0700 on POSIX
    try {
      extract(zip, tmpDir);
      Files.writeString(tmpDir.resolve(COMPLETE_MARKER), zipName + System.lineSeparator());
      try {
        Files.move(tmpDir, envDir, StandardCopyOption.ATOMIC_MOVE);
      } catch (java.nio.file.FileAlreadyExistsException
          | java.nio.file.DirectoryNotEmptyException e) {
        verifySecureDir(envDir);
        if (!isComplete(envDir)) {
          throw e;
        }
        LOG.info("Python environment {} was extracted concurrently; reusing it", envDir);
      }
    } finally {
      deleteRecursively(tmpDir); // no-op after a successful move
    }
  }

  private static UserPrincipal currentUser(Path dir) throws IOException {
    try {
      return dir.getFileSystem()
          .getUserPrincipalLookupService()
          .lookupPrincipalByName(System.getProperty("user.name"));
    } catch (IOException | UnsupportedOperationException e) {
      // e.g. container uid without passwd entry: a file we create is owned by us
      Path probe = Files.createTempFile(".owner-", ".probe");
      try {
        return Files.getOwner(probe);
      } finally {
        Files.deleteIfExists(probe);
      }
    }
  }

  private static InsecureCacheDirException insecure(Path dir, String why) {
    String msg =
        "refusing to use Python cache directory " + dir + ": it " + why
            + " (native libraries are loaded from it; set " + CACHE_DIR_PROPERTY
            + " to a private directory)";
    LOG.error(msg);
    return new InsecureCacheDirException(msg);
  }

  static boolean isWindows() {
    return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
  }

  static PythonEnvironment resolve(
      String homeProperty,
      String homeEnv,
      Path cacheBase,
      boolean windows,
      ResourceLoader resources)
      throws PythonUnavailableException {
    if (notBlank(homeProperty)) {
      return external(Paths.get(homeProperty), "system property " + HOME_PROPERTY, cacheBase,
          windows, resources);
    }
    if (notBlank(homeEnv)) {
      return external(Paths.get(homeEnv), "environment variable " + HOME_ENV, cacheBase, windows,
          resources);
    }
    String zipName = windows ? WIN_ZIP : NIX_ZIP;
    try {
      InputStream zip = resources.open(zipName);
      if (zip == null) {
        throw new PythonUnavailableException(
            "no Python environment: neither system property " + HOME_PROPERTY
                + " nor environment variable " + HOME_ENV + " is set, and the PythonPlugin JAR"
                + " contains no " + zipName.substring(1) + " (build it into"
                + " pluginsystem/plugins/PythonPlugin/python-env/dist/ before 'mvn install')");
      }
      try (zip) {
        return embedded(zip, zipName.substring(1), cacheBase, windows, resources);
      }
    } catch (IOException e) {
      throw new PythonUnavailableException(
          "failed to prepare embedded Python environment " + zipName.substring(1) + " in "
              + cacheBase + ": " + e,
          e);
    }
  }

  private static boolean notBlank(String s) {
    return s != null && !s.isBlank();
  }

  private static PythonEnvironment external(
      Path home, String origin, Path cacheBase, boolean windows, ResourceLoader resources)
      throws PythonUnavailableException {
    Path absHome = home.toAbsolutePath().normalize();
    if (!Files.isDirectory(absHome)) {
      throw new PythonUnavailableException(
          "Python home from " + origin + " does not exist: " + absHome);
    }
    try {
      secureDir(cacheBase);
      Path ajanLib;
      try (CacheLock lock = new CacheLock(cacheBase)) {
        ajanLib = writeAjanLib(cacheBase.resolve(AJAN_LIB_DIR), resources);
      }
      return describe(absHome, ajanLib, windows, origin + " (" + absHome + ")");
    } catch (IOException e) {
      throw new PythonUnavailableException("failed to prepare " + AJAN_LIB_DIR + " in "
          + cacheBase + ": " + e, e);
    }
  }

  private static PythonEnvironment embedded(
      InputStream zipStream, String zipName, Path cacheBase, boolean windows,
      ResourceLoader resources) throws IOException, PythonUnavailableException {
    secureDir(cacheBase);
    Path tmpZip = Files.createTempFile(cacheBase, ".download-", ".zip");
    try {
      String hash = copyAndHash(zipStream, tmpZip).substring(0, 12);
      Path envDir = cacheBase.resolve(hash);
      Path ajanLib;
      try (CacheLock lock = new CacheLock(cacheBase)) {
        if (Files.exists(envDir, LinkOption.NOFOLLOW_LINKS)) {
          verifySecureDir(envDir); // refuses symlinks and foreign/writable dirs
        }
        if (isComplete(envDir)) {
          LOG.info("Reusing extracted Python environment {}", envDir);
        } else {
          if (Files.exists(envDir, LinkOption.NOFOLLOW_LINKS)) {
            LOG.info("Removing incomplete Python environment {}", envDir);
            deleteRecursively(envDir);
          }
          extractAtomically(tmpZip, zipName, cacheBase, envDir);
        }
        removeStaleSiblings(cacheBase, hash);
        ajanLib = writeAjanLib(envDir.resolve(AJAN_LIB_DIR), resources);
      }
      Path home = findHome(envDir);
      return describe(home, ajanLib, windows, "embedded " + zipName + " (" + envDir + ")");
    } finally {
      Files.deleteIfExists(tmpZip);
    }
  }

  /** Serializes cache access within this JVM (FileChannel locks are per process). */
  private static final java.util.concurrent.locks.ReentrantLock JVM_LOCK =
      new java.util.concurrent.locks.ReentrantLock();

  /** Exclusive lock on the cache directory, across threads and JVMs. */
  private static final class CacheLock implements AutoCloseable {
    private final FileChannel channel;

    private CacheLock(Path cacheBase) throws IOException {
      JVM_LOCK.lock();
      FileChannel ch = null;
      try {
        ch =
            FileChannel.open(
                cacheBase.resolve(".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        ch.lock(); // released when the channel is closed
      } catch (IOException | RuntimeException e) {
        if (ch != null) {
          ch.close();
        }
        JVM_LOCK.unlock();
        throw e;
      }
      this.channel = ch;
    }

    @Override
    public void close() throws IOException {
      try {
        channel.close();
      } finally {
        JVM_LOCK.unlock();
      }
    }
  }

  private static String copyAndHash(InputStream in, Path target) throws IOException {
    MessageDigest sha;
    try {
      sha = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IOException(e);
    }
    try (OutputStream out = new DigestOutputStream(Files.newOutputStream(target), sha)) {
      in.transferTo(out);
    }
    return HexFormat.of().formatHex(sha.digest());
  }

  private static void removeStaleSiblings(Path cacheBase, String current) {
    try (DirectoryStream<Path> entries = Files.newDirectoryStream(cacheBase)) {
      for (Path p : entries) {
        String name = p.getFileName().toString();
        boolean managed = CACHE_ENTRY.matcher(name).matches() || name.startsWith(".extract-");
        if (!name.equals(current) && managed) {
          try {
            deleteRecursively(p);
            LOG.info("Removed stale Python cache entry {}", p);
          } catch (IOException e) {
            // e.g. still in use by another JVM on Windows; retried on the next start
            LOG.warn("Could not remove stale Python environment {}: {}", p, e.toString());
          }
        }
      }
    } catch (IOException e) {
      LOG.warn("Could not list Python cache {}: {}", cacheBase, e.toString());
    }
  }

  static void deleteRecursively(Path root) throws IOException {
    if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
      return;
    }
    if (Files.isSymbolicLink(root)) {
      Files.delete(root); // the link only, never its target
      return;
    }
    List<Path> all; // Files.walk does not follow symlinks
    try (Stream<Path> walk = Files.walk(root)) {
      all = walk.sorted(Comparator.reverseOrder()).toList();
    }
    for (Path p : all) {
      Files.deleteIfExists(p);
    }
  }

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

  /** The zip has one root directory (python-portable/); tolerate a flat layout too. */
  private static Path findHome(Path envDir) throws IOException {
    List<Path> dirs = new ArrayList<>();
    boolean files = false;
    try (DirectoryStream<Path> entries = Files.newDirectoryStream(envDir)) {
      for (Path p : entries) {
        String name = p.getFileName().toString();
        if (name.equals(COMPLETE_MARKER) || name.equals(AJAN_LIB_DIR)) {
          continue;
        }
        if (Files.isDirectory(p)) {
          dirs.add(p);
        } else {
          files = true;
        }
      }
    }
    return dirs.size() == 1 && !files ? dirs.get(0) : envDir;
  }

  private static Path writeAjanLib(Path dir, ResourceLoader resources)
      throws IOException, PythonUnavailableException {
    byte[] content;
    try (InputStream in = resources.open(AJAN_LIB_RESOURCE)) {
      if (in == null) {
        throw new PythonUnavailableException(
            "resource " + AJAN_LIB_RESOURCE + " missing from the PythonPlugin JAR");
      }
      content = in.readAllBytes();
    }
    secureDir(dir);
    Path file = dir.resolve("AJANlib.py");
    if (!Files.isRegularFile(file) || !Arrays.equals(Files.readAllBytes(file), content)) {
      Path tmp = Files.createTempFile(dir, ".AJANlib", ".tmp");
      Files.write(tmp, content);
      Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
    return dir.toAbsolutePath();
  }

  private static PythonEnvironment describe(Path home, Path ajanLib, boolean windows, String source)
      throws PythonUnavailableException, IOException {
    Path site = findSitePackages(home, windows);
    if (site == null) {
      throw new PythonUnavailableException(
          "site-packages not found in Python home " + home + " (expected "
              + (windows ? "Lib/site-packages" : "lib/python3.X/site-packages") + ")");
    }
    String jepName = windows ? "jep.dll" : "libjep.so";
    Path jep = site.resolve("jep").resolve(jepName);
    if (!Files.isRegularFile(jep)) {
      Path mac = site.resolve("jep").resolve("libjep.jnilib");
      if (!windows && Files.isRegularFile(mac)) {
        jep = mac;
      } else {
        throw new PythonUnavailableException(
            "jep native library " + jepName + " not found at " + jep
                + " (is jep installed in this Python?)");
      }
    }
    return new PythonEnvironment(
        home, site, jep.toAbsolutePath(), ajanLib, findPreloads(home, windows), source);
  }

  private static Path findSitePackages(Path home, boolean windows) throws IOException {
    Path win = home.resolve("Lib").resolve("site-packages");
    if (windows && Files.isDirectory(win)) {
      return win;
    }
    Path lib = home.resolve("lib");
    if (Files.isDirectory(lib)) {
      try (Stream<Path> s = Files.list(lib)) {
        List<Path> candidates =
            s.filter(p -> p.getFileName().toString().matches("python3\\.\\d+"))
                .map(p -> p.resolve("site-packages"))
                .filter(Files::isDirectory)
                .sorted()
                .toList();
        if (!candidates.isEmpty()) {
          return candidates.get(candidates.size() - 1);
        }
      }
    }
    return Files.isDirectory(win) ? win : null;
  }

  private static List<Path> findPreloads(Path home, boolean windows) throws IOException {
    List<Path> result = new ArrayList<>();
    if (windows) {
      for (String rt : new String[] {"vcruntime140.dll", "vcruntime140_1.dll"}) {
        Path p = home.resolve(rt);
        if (Files.isRegularFile(p)) {
          result.add(p.toAbsolutePath());
        }
      }
      // python3.dll (stable ABI forwarder) needs python3XX.dll: load the versioned one first.
      result.addAll(
          listMatching(home, WIN_PYTHON_DLL).stream()
              .sorted(Comparator.comparing((Path p) -> p.getFileName().toString().length())
                  .reversed())
              .toList());
    } else {
      result.addAll(listMatching(home.resolve("lib"), NIX_LIBPYTHON));
    }
    return result;
  }

  private static List<Path> listMatching(Path dir, Pattern pattern) throws IOException {
    if (!Files.isDirectory(dir)) {
      return List.of();
    }
    try (Stream<Path> s = Files.list(dir)) {
      return s.filter(p -> pattern.matcher(p.getFileName().toString().toLowerCase(Locale.ROOT))
                  .matches())
          .filter(Files::isRegularFile)
          .map(Path::toAbsolutePath)
          .sorted()
          .toList();
    }
  }
}
