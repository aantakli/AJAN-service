import io
import sys
import threading
import traceback
from contextlib import contextmanager
from enum import Enum

import rdflib

class Status(Enum):
   FRESH = "FRESH"
   RUNNING = "RUNNING"
   SUCCEEDED = "SUCCEEDED"
   FAILED = "FAILED"

class NodeResult:
   status = Status.FRESH
   label = "My NodeStatus is fresh"
   rdf_output = rdflib.Graph()

   def __init__(self, status: Status, label: str, rdf_output: rdflib.Graph):
      self.status = status
      self.label = label
      self.rdf_output = rdf_output


class LeafNode:
   def executeLeafNode(self, input: rdflib.Graph) -> NodeResult:
      """This Method is executed by AJAN itself to get python based NodeResult"""
      pass

   def getClassName(self) -> str:
      """return name of own class"""
      pass


# --- stdout capture -----------------------------------------------------------------------------
# sys.stdout is process-global, but leaf nodes run concurrently on several Java threads. The proxy
# routes writes of a thread that is capturing into that thread's buffer; all other writes go to the
# real stdout, which is never replaced again (so an exception cannot lose it).

class _ThreadLocalStdout(io.TextIOBase):
   def __init__(self, real):
      self._real = real
      self._buffers = {}  # thread ident -> StringIO
      self._lock = threading.Lock()

   def _target(self):
      return self._buffers.get(threading.get_ident(), self._real)

   def begin(self):
      with self._lock:
         self._buffers[threading.get_ident()] = io.StringIO()

   def end(self) -> str:
      with self._lock:
         buf = self._buffers.pop(threading.get_ident(), None)
      return buf.getvalue() if buf is not None else ""

   def capturing(self) -> bool:
      return threading.get_ident() in self._buffers

   def writable(self):
      return True

   def write(self, s):
      target = self._target()
      if target is None:  # no real stdout (embedded without console)
         return len(s)
      return target.write(s)

   def flush(self):
      target = self._target()
      if target is not None:
         target.flush()

   @property
   def encoding(self):
      return getattr(self._real, "encoding", "utf-8")

   def isatty(self):
      target = self._target()
      return bool(target is not None and target.isatty())

   def fileno(self):
      if self._real is None:
         raise io.UnsupportedOperation("fileno")
      return self._real.fileno()


_install_lock = threading.Lock()


def install_stdout_proxy() -> _ThreadLocalStdout:
   """Installs the thread-local stdout proxy once (idempotent) and returns it."""
   with _install_lock:
      if not isinstance(sys.stdout, _ThreadLocalStdout):
         sys.stdout = _ThreadLocalStdout(sys.stdout)
      return sys.stdout


install_stdout_proxy()


@contextmanager
def capture_stdout():
   """Captures this thread's prints; yields a list that holds the text after the block."""
   proxy = install_stdout_proxy()
   nested = proxy.capturing()
   out = []
   if not nested:
      proxy.begin()
   try:
      yield out
   finally:
      out.append("" if nested else proxy.end())


# --- LeafNode contract --------------------------------------------------------------------------

def _status_name(status) -> str:
   if isinstance(status, Status):
      return status.value
   if status is None:
      return Status.FAILED.value
   return str(status).strip().upper()


def _to_turtle(output) -> str:
   if output is None:
      return ""
   if isinstance(output, rdflib.Graph):
      return output.serialize(format="turtle") if len(output) > 0 else ""
   return str(output)


def _leaf_classes(namespace: dict) -> list:
   found = []
   for value in namespace.values():
      if (isinstance(value, type) and issubclass(value, LeafNode) and value is not LeafNode
            and value not in found):
         found.append(value)
   return found


def run_leaf(namespace: dict, input_turtle: str) -> tuple:
   """Runs the single LeafNode subclass defined in namespace on the input.

   Returns (status, label, output_turtle). Without a subclass the legacy globals
   status (default "SUCCEEDED") and rdf_output apply. More than one subclass: ValueError.
   """
   classes = _leaf_classes(namespace)
   if len(classes) > 1:
      raise ValueError("script defines more than one AJANlib.LeafNode subclass: "
                       + ", ".join(c.__name__ for c in classes))
   if not classes:
      status = namespace.get("status", Status.SUCCEEDED.value)
      return _status_name(status), "", _to_turtle(namespace.get("rdf_output", ""))
   graph = rdflib.Graph()
   if input_turtle and input_turtle.strip():
      graph.parse(data=input_turtle, format="turtle")
   result = classes[0]().executeLeafNode(graph)
   if result is None:
      return Status.FAILED.value, classes[0].__name__ + ".executeLeafNode returned None", ""
   label = getattr(result, "label", "")
   return (_status_name(getattr(result, "status", None)), "" if label is None else str(label),
           _to_turtle(getattr(result, "rdf_output", None)))


def execute_leaf_script(script: str, input_turtle: str) -> tuple:
   """Executes a PythonLeafNode script; never raises.

   Returns (status, label, output_turtle, stdout); a Python exception yields
   ("FAILED", traceback, "", stdout).
   """
   input_turtle = input_turtle or ""
   namespace = {
      "__name__": "__main__",
      "__builtins__": __builtins__,
      "AJANlib": sys.modules[__name__],
      "sys": sys,
      "StringIO": io.StringIO,
      "input_rdf": input_turtle,
      "status": Status.SUCCEEDED.value,
      "rdf_output": "",
   }
   with capture_stdout() as out:
      try:
         exec(compile(script, "<PythonLeafNode>", "exec"), namespace)
         status, label, output = run_leaf(namespace, input_turtle)
      except BaseException:  # SystemExit too: a script must not end the embedded interpreter
         status, label, output = Status.FAILED.value, traceback.format_exc(), ""
   return status, label, output, out[0]


# --- script files -------------------------------------------------------------------------------
# sys.path is process-global and shared by all concurrent scripts. A script's directory is put in
# front of sys.path while at least one script from that directory runs (reference counted), so
# concurrent scripts never remove each other's entries. sys.argv is process-global too: the Java
# side serializes the calls that pass argv (see PythonExecutionServiceImpl.executeScriptFile).

_path_lock = threading.Lock()
_path_refs = {}  # directory -> number of running scripts from it
_NO_ARGV = object()  # run_script_file did not touch sys.argv


def _acquire_script_dir(directory: str):
   with _path_lock:
      count = _path_refs.get(directory, 0)
      if count == 0:
         sys.path.insert(0, directory)
      _path_refs[directory] = count + 1


def _release_script_dir(directory: str):
   with _path_lock:
      count = _path_refs.get(directory, 0) - 1
      if count > 0:
         _path_refs[directory] = count
         return
      _path_refs.pop(directory, None)
      try:
         sys.path.remove(directory)
      except ValueError:
         pass  # the script removed it itself


def _exit_failure(code):
   """None for a successful SystemExit code (None/0), otherwise the failure message."""
   if code is None or code == 0:
      return None
   if isinstance(code, int):
      return "script exited with code %d" % code
   return "script exited: %s" % (code,)


def run_script_file(namespace: dict, path: str, argv=None) -> tuple:
   """Runs the script file at path as __main__ in namespace; never raises.

   The script directory is in front of sys.path while it runs (sibling imports). With argv
   (a sequence, possibly a Java list), sys.argv is [path] + argv during the run and restored
   afterwards. Returns (ok, message): SystemExit with code None/0 is ok, any other code or
   exception is not (message: exit code or traceback).
   """
   import os
   path = os.path.abspath(str(path))
   directory = os.path.dirname(path)
   namespace["__name__"] = "__main__"
   namespace["__file__"] = path
   namespace.setdefault("__builtins__", __builtins__)
   saved_argv = _NO_ARGV
   if argv is not None:
      saved_argv = getattr(sys, "argv", None)
      sys.argv = [path] + [str(a) for a in argv]
   _acquire_script_dir(directory)
   try:
      with open(path, "rb") as f:
         source = f.read()
      exec(compile(source, path, "exec"), namespace)
      return True, ""
   except SystemExit as e:
      failure = _exit_failure(e.code)
      return failure is None, failure or ""
   except BaseException:  # a script must not end the embedded interpreter
      return False, traceback.format_exc()
   finally:
      _release_script_dir(directory)
      if saved_argv is not _NO_ARGV:
         if saved_argv is None:
            del sys.argv
         else:
            sys.argv = saved_argv
