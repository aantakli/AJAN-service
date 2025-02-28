from importlib import metadata

try:
    VERSION = metadata.version(__package__)
except metadata.PackageNotFoundError:
    # Local run without installation
    VERSION = "dev"
