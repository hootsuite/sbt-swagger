# Notable changes for each version increment

## Development

- Nothing

## 0.2.0

- Uses SBT's `classDirectory` and `target` keys to get the directory containing compiled
  code and the "target" directory to output, rather than hardcoding relative paths (which
  may not be correct in a multi-module or cross-compiled build)
    - This means that the swagger docs will now be output to the `target` directory of the project that it is run on,
      _not_ the root project `target` dir in a multi-module project.

## Pre 0.2.0

- `¯\_(ツ)_/¯`
