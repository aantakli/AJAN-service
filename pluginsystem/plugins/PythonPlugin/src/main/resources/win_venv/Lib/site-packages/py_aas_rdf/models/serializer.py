#  MIT License
#
#  Copyright (c) 2023. Mohammad Hossein Rimaz
#
#  Permission is hereby granted, free of charge, to any person obtaining a copy of
#  this software and associated documentation files (the “Software”), to deal in
#  the Software without restriction, including without limitation the rights to use,
#  copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
#  Software, and to permit persons to whom the Software is furnished to do so, subject
#   to the following conditions:
#
#  The above copyright notice and this permission notice shall be included in all
#  copies or substantial portions of the Software.
#
#  THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
#  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
#  PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
#  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
#  CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
#  OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
from rdflib import URIRef
from rdflib.plugins.serializers.turtle import TurtleSerializer


class TurtleSerializerCustom(TurtleSerializer):
    def getQName(self, uri, gen_prefix=True):
        if not isinstance(uri, URIRef):
            return None

        parts = None

        try:
            parts = self.store.compute_qname(uri, generate=gen_prefix)
        except Exception:
            # is the uri a namespace in itself?
            pfx = self.store.store.prefix(uri)
            if pfx is not None:
                parts = (pfx, uri, "")
            else:
                # nothing worked
                return None

        prefix, namespace, local = parts

        local = local.replace(r"(", r"\(").replace(r")", r"\)")

        # QName cannot end with .
        if local.endswith("."):
            return None
        if prefix.startswith("ns"):
            return None
        prefix = self.addNamespace(prefix, namespace)

        return "%s:%s" % (prefix, local)
