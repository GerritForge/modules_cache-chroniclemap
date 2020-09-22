Configuration
=============

Global configuration of the cache-chroniclemap libModule is done in the `gerrit.config` file in the
site's etc directory.

Information about gerrit caches mechanism can be found in the relevant
[documentation section](https://charm.cs.illinois.edu/gerrit/Documentation/config-gerrit.html#cache).

Chronicle-map implementation however might require some additional configuration

## Configuration parameters

```cache.<name>.avgKeySize```
:   The average number of bytes to be allocated for the key of this cache.
If key is a boxed primitive type, a value interface or Byteable subclass, i. e.
if key size is known statically, it is automatically accounted by chronicle-map.
In this case, this value will be ignored.

[Official docs](
https://www.javadoc.io/doc/net.openhft/chronicle-map/3.8.0/net/openhft/chronicle/map/ChronicleMapBuilder.html#averageKeySize-double-
)

```cache.<name>.avgValueSize```
:   The average number of bytes to be allocated for a value of this cache.
If key is a boxed primitive type, a value interface or Byteable subclass, i. e.
if key size is known statically, it is automatically accounted by chronicle-map.
In this case, this value will be ignored.

[Official docs](
https://www.javadoc.io/doc/net.openhft/chronicle-map/3.8.0/net/openhft/chronicle/map/ChronicleMapBuilder.html#averageValueSize-double-
)

```cache.<name>.entries```
: The number of entries that this cache is going to hold, _at most_

[Official docs](
https://www.javadoc.io/doc/net.openhft/chronicle-map/3.8.0/net/openhft/chronicle/map/ChronicleMapBuilder.html#entries-long-
)