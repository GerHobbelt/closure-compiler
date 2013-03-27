# Closure Compiler

This is Obvious' fork of the
[Google Closure Compiler](http://code.google.com/p/closure-compiler).

We try to keep it reasonably up to date, but only after testing that it is
compatible with our products. There may occasionally be changes introduced to
work around temporary issues or to try out fixes in preparation of upstream
patches.

The package number reflects the date the last merge was taken from.

## Merging from the main project

The 'pristine' branch contains only changes that we've pulled from the main repo.

The 'master' branch should always contain main repo changes, with our changes layered on top.

To sync changes from the main project, run the following:

```
# Sync changes from code.google.com
git clone git@github.com:Obvious/closure-compiler
cd closure-compiler
git remote add googlecode http://code.google.com/p/closure-compiler
git fetch googlecode
git checkout pristine
git merge googlecode/master
git push origin pristine

# Layer our changes on top
git checkout master
git rebase pristine master
```

## Contributing

This project isn't intended for external contribution, we suggest instead you
[send patches](https://code.google.com/p/closure-compiler/wiki/Contributors)
directly.

## Original README

[Original README](README.txt)