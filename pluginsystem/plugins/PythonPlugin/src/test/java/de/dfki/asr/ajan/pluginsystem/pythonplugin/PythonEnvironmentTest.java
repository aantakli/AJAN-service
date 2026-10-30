package de.dfki.asr.ajan.pluginsystem.pythonplugin;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Pure-Java tests of environment resolution and the extraction cache (no JEP, no native code). */
public class PythonEnvironmentTest {

  private static final String ROOT = "python-portable/";
  private static final String NIX_SITE = ROOT + "lib/python3.13/site-packages/";
  private static final byte[] AJANLIB = "import rdflib\n".getBytes(StandardCharsets.UTF_8);

  private Path tmp;
  private Path cache;

  @BeforeMethod
  public void setUp() throws IOException {
    tmp = Files.createTempDirectory("pyenv-test");
    cache = tmp.resolve("cache");
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws IOException {
    PythonEnvironment.deleteRecursively(tmp);
  }

  // --- fixtures ---------------------------------------------------------------------------

  /** Entry content; a value starting with "->" is stored as a Unix symlink to the rest. */
  private static byte[] zip(Map<String, String> entries) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(bos)) {
      for (Map.Entry<String, String> e : entries.entrySet()) {
        zos.putNextEntry(new ZipEntry(e.getKey()));
        String v = e.getValue();
        zos.write((v.startsWith("->") ? v.substring(2) : v).getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
      }
    }
    byte[] bytes = bos.toByteArray();
    // java.util.zip cannot write Unix attributes: patch "version made by" (Unix) and the
    // external attributes (st_mode << 16) in the central directory, like `zip -y` does.
    ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i + 46 < bytes.length; i++) {
      if (buf.getInt(i) != 0x02014b50) {
        continue;
      }
      int nameLen = buf.getShort(i + 28) & 0xffff;
      String name = new String(bytes, i + 46, nameLen, StandardCharsets.UTF_8);
      String v = entries.get(name);
      if (v == null) {
        continue;
      }
      int mode = v.startsWith("->") ? 0120777 : (name.endsWith(".so") ? 0100755 : 0100644);
      buf.putShort(i + 4, (short) 0x0314);
      buf.putInt(i + 38, mode << 16);
    }
    return bytes;
  }

  private static Map<String, String> nixEnv() {
    Map<String, String> m = new LinkedHashMap<>();
    m.put(ROOT + "lib/libpython3.13.so.1.0", "ELF-libpython");
    m.put(ROOT + "lib/libpython3.13.so", "->libpython3.13.so.1.0");
    m.put(ROOT + "bin/python3.13", "binary");
    m.put(ROOT + "bin/python3", "->python3.13");
    m.put(NIX_SITE + "jep/libjep.so", "ELF-jep");
    m.put(NIX_SITE + "jep/__init__.py", "");
    return m;
  }

  private static PythonEnvironment.ResourceLoader resources(String zipName, byte[] zip) {
    return name -> {
      if (name.equals(PythonEnvironment.AJAN_LIB_RESOURCE)) {
        return new ByteArrayInputStream(AJANLIB);
      }
      if (zip != null && name.equals(zipName)) {
        return new ByteArrayInputStream(zip);
      }
      return null;
    };
  }

  private PythonEnvironment resolveNix(byte[] zip) throws PythonUnavailableException {
    return PythonEnvironment.resolve(null, null, cache, false, resources("/nix_env.zip", zip));
  }

  private static String hash12(byte[] data) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data))
        .substring(0, 12);
  }

  private static Path externalHome(Path dir) throws IOException {
    Path jep = dir.resolve("lib/python3.13/site-packages/jep");
    Files.createDirectories(jep);
    Files.writeString(jep.resolve("libjep.so"), "ELF");
    return dir;
  }

  // --- tests ------------------------------------------------------------------------------

  @Test
  public void extractsEmbeddedZipAndDescribesLayout() throws Exception {
    byte[] zip = zip(nixEnv());
    PythonEnvironment env = resolveNix(zip);

    Path envDir = cache.resolve(hash12(zip));
    assertTrue(Files.isRegularFile(envDir.resolve(PythonEnvironment.COMPLETE_MARKER)));
    assertEquals(env.home(), envDir.resolve("python-portable"));
    assertEquals(env.sitePackages(), env.home().resolve("lib/python3.13/site-packages"));
    assertEquals(env.jepLibrary(), env.sitePackages().resolve("jep/libjep.so"));
    assertEquals(env.ajanLib(), envDir.resolve("ajan-lib"));
    assertEquals(Files.readAllBytes(env.ajanLib().resolve("AJANlib.py")), AJANLIB);
    assertEquals(
        env.nativePreloads(), List.of(env.home().resolve("lib/libpython3.13.so.1.0")));
  }

  @Test
  public void restoresSymlinkEntries() throws Exception {
    PythonEnvironment env = resolveNix(zip(nixEnv()));

    Path link = env.home().resolve("lib/libpython3.13.so");
    assertEquals(Files.readString(link), "ELF-libpython");
    assertEquals(Files.readString(env.home().resolve("bin/python3")), "binary");
    if (!PythonEnvironment.isWindows()) {
      assertTrue(Files.isSymbolicLink(link), "symlink entry must become a symlink");
      assertEquals(Files.readSymbolicLink(link).toString(), "libpython3.13.so.1.0");
      assertTrue(Files.isExecutable(env.jepLibrary()), "unix mode 0755 restored");
      assertFalse(Files.isSymbolicLink(env.nativePreloads().get(0)));
    }
  }

  @Test
  public void reusesCompleteCacheWithoutReExtracting() throws Exception {
    byte[] zip = zip(nixEnv());
    PythonEnvironment first = resolveNix(zip);
    Path sentinel = first.home().resolve("sentinel");
    Files.writeString(sentinel, "kept");
    Files.delete(first.jepLibrary()); // would be restored by a re-extraction

    PythonUnavailableException e =
        expectThrows(PythonUnavailableException.class, () -> resolveNix(zip));
    assertTrue(e.getMessage().contains("libjep.so"), e.getMessage());
    assertTrue(Files.exists(sentinel), "complete cache must be reused, not re-extracted");
  }

  @Test
  public void incompleteCacheDirIsDeletedAndReExtracted() throws Exception {
    byte[] zip = zip(nixEnv());
    Path envDir = cache.resolve(hash12(zip));
    Files.createDirectories(envDir.resolve("python-portable/lib"));
    Files.writeString(envDir.resolve("leftover"), "partial");

    PythonEnvironment env = resolveNix(zip);

    assertFalse(Files.exists(envDir.resolve("leftover")));
    assertTrue(Files.isRegularFile(envDir.resolve(PythonEnvironment.COMPLETE_MARKER)));
    assertTrue(Files.isRegularFile(env.jepLibrary()));
  }

  @Test
  public void staleSiblingCacheDirsAreRemoved() throws Exception {
    Path stale = cache.resolve("0123456789ab");
    Files.createDirectories(stale.resolve("python-portable"));
    Files.writeString(stale.resolve(PythonEnvironment.COMPLETE_MARKER), "old");
    Path unrelated = cache.resolve("keep-me");
    Files.createDirectories(unrelated);

    byte[] zip = zip(nixEnv());
    resolveNix(zip);

    assertFalse(Files.exists(stale), "stale environment must be deleted");
    assertTrue(Files.isDirectory(unrelated), "only <sha256-12> directories are managed");
    assertTrue(Files.isDirectory(cache.resolve(hash12(zip))));
    assertNotEquals(hash12(zip), "0123456789ab");
  }

  @Test
  public void homePropertyTakesPrecedenceOverEnvVarAndZip() throws Exception {
    Path fromProp = externalHome(tmp.resolve("prop"));
    Path fromEnv = externalHome(tmp.resolve("env"));
    PythonEnvironment.ResourceLoader res = resources("/nix_env.zip", zip(nixEnv()));

    PythonEnvironment env =
        PythonEnvironment.resolve(fromProp.toString(), fromEnv.toString(), cache, false, res);
    assertEquals(env.home(), fromProp.toAbsolutePath().normalize());
    assertTrue(env.source().contains(PythonEnvironment.HOME_PROPERTY), env.source());
    assertEquals(env.ajanLib(), cache.resolve("ajan-lib").toAbsolutePath());
    assertTrue(Files.isRegularFile(env.ajanLib().resolve("AJANlib.py")));

    env = PythonEnvironment.resolve(null, fromEnv.toString(), cache, false, res);
    assertEquals(env.home(), fromEnv.toAbsolutePath().normalize());
    assertTrue(env.source().contains(PythonEnvironment.HOME_ENV), env.source());

    assertFalse(Files.exists(cache.resolve(hash12(zip(nixEnv())))), "zip must not be extracted");
  }

  @Test
  public void externalHomeWithoutJepIsRejectedWithReason() throws Exception {
    Path home = tmp.resolve("nojep/lib/python3.13/site-packages");
    Files.createDirectories(home);
    PythonUnavailableException e =
        expectThrows(
            PythonUnavailableException.class,
            () ->
                PythonEnvironment.resolve(
                    tmp.resolve("nojep").toString(), null, cache, false, resources("x", null)));
    assertTrue(e.getMessage().contains("libjep.so"), e.getMessage());

    e =
        expectThrows(
            PythonUnavailableException.class,
            () ->
                PythonEnvironment.resolve(
                    tmp.resolve("missing").toString(), null, cache, false, resources("x", null)));
    assertTrue(e.getMessage().contains("does not exist"), e.getMessage());
  }

  @Test
  public void missingEverythingNamesWhatIsMissing() {
    PythonUnavailableException e =
        expectThrows(
            PythonUnavailableException.class,
            () -> PythonEnvironment.resolve(null, " ", cache, false, resources("x", null)));
    assertTrue(e.getMessage().contains(PythonEnvironment.HOME_PROPERTY), e.getMessage());
    assertTrue(e.getMessage().contains(PythonEnvironment.HOME_ENV), e.getMessage());
    assertTrue(e.getMessage().contains("nix_env.zip"), e.getMessage());

    e =
        expectThrows(
            PythonUnavailableException.class,
            () -> PythonEnvironment.resolve(null, null, cache, true, resources("x", null)));
    assertTrue(e.getMessage().contains("win_env.zip"), e.getMessage());
  }

  @Test
  public void windowsLayoutUsesJepDllAndPreloadsPythonDlls() throws Exception {
    Map<String, String> m = new LinkedHashMap<>();
    m.put(ROOT + "python313.dll", "dll");
    m.put(ROOT + "python3.dll", "dll");
    m.put(ROOT + "vcruntime140.dll", "dll");
    m.put(ROOT + "Lib/site-packages/jep/jep.dll", "dll");
    byte[] zip = zip(m);

    PythonEnvironment env =
        PythonEnvironment.resolve(null, null, cache, true, resources("/win_env.zip", zip));

    assertEquals(env.sitePackages(), env.home().resolve("Lib/site-packages"));
    assertEquals(env.jepLibrary(), env.sitePackages().resolve("jep/jep.dll"));
    assertEquals(
        env.nativePreloads(),
        List.of(
            env.home().resolve("vcruntime140.dll"),
            env.home().resolve("python313.dll"),
            env.home().resolve("python3.dll")));
  }

  @Test
  public void zipSlipEntriesAreRejected() throws Exception {
    Map<String, String> m = nixEnv();
    m.put("../evil.txt", "x");
    byte[] zip = zip(m);
    PythonUnavailableException e =
        expectThrows(PythonUnavailableException.class, () -> resolveNix(zip));
    assertTrue(e.getMessage().contains("escapes"), e.getMessage());
    assertFalse(Files.exists(tmp.resolve("evil.txt")));
    assertFalse(
        Files.exists(cache.resolve(hash12(zip)).resolve(PythonEnvironment.COMPLETE_MARKER)));
  }

  @Test
  public void symlinksEscapingTheEnvironmentAreRejected() throws Exception {
    Map<String, String> m = nixEnv();
    m.put(ROOT + "lib/escape", "->../../../../outside");
    byte[] zip = zip(m);
    PythonUnavailableException e =
        expectThrows(PythonUnavailableException.class, () -> resolveNix(zip));
    assertTrue(e.getMessage().contains("symlink escapes"), e.getMessage());
  }
}
