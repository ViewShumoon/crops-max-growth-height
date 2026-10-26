# Crops Max Growth Height

A server-side Fabric mod for **Minecraft 26.3** that raises the maximum growth height of **bamboo** and **sugar cane**. No commands, no client installation required.

## Features

- Sugar cane columns can grow far beyond the vanilla limit of **3** blocks.
- Bamboo columns can grow far beyond the vanilla limit of **16** blocks.
- Maximum height is fully configurable per crop, default **256**, allowed range **1–512**.
- Growth behaviour otherwise matches vanilla (same random tick guards, age/leaves handling, and "done growing" thresholds).
- Purely server-side: players on vanilla clients are unaffected and don't need to install anything.

## Configuration

On first start the mod generates `config/crops-max-growth-height.toml`:

```toml
# Configuration of crops-max-growth-height
# Allowed range for every value: 1-512

# Maximum total height of one sugar cane column (vanilla is 3)
sugarCaneMaxHeight = 256

# Maximum total height of one bamboo column (vanilla is 16)
bambooMaxHeight = 256
```

Edit the values and restart the server to apply them. Out-of-range values are clamped to `1–512` with a warning in the server log.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) on your server (>= 0.19.5).
2. Drop the mod jar into the `mods` folder.
3. Start the server.

## Building

```bash
./gradlew build
```

The jar is produced in `build/libs`.

## License

[Unlicense](LICENSE.txt)
