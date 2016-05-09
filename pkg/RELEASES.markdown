Release checklist
=================

Pre-Requisites
--------------

- Write the change-log
- lein clean
- lein test

Releasing
---------

- Bump version (`x.y.z-SNAPSHOT` to `x.y.z`)
- `git commit`
- `git tag x.y.z`
- `lein deploy clojars` (requires access to the `riemann` group)
- `lein pkg` to build DEB and RPM packages.
- Bump to next version `x.y.z+1-SNAPSHOT` 
- `git push` and `git push --tags`
- Generate docs with `lein codox`
- Bump version in `index.html` and `quickstart.html`
- Seal-off version in `thanks.html`
