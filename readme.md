# Raycasting Renderer

This project is a simple raycasting-based renderer written in Kotlin. It simulates light rays to render 3D scenes based on voxel data.

![img.png](https://i.imgur.com/yu7x4rU.jpg)

## Features

- **Raycasting**: Simulates light rays to calculate colors and shading.
- **Camera**: Adjustable position, rotation, field of view, and sampling settings.
- **World Rendering**: Supports voxel-based worlds with textures.
- **Multithreading**: Uses coroutines for efficient parallel processing.
- **Texture Management**: Preloads and caches textures for blocks.

## How to Run

1. **Setup**:
   - Unzip textures to /assets (ex. `assets/minecraft/textures/block/`)
   - World files should be in `.schem` format and placed in the `worlds/` directory.

2. **Run the Application**:
    - Run the `main` function in `Main.kt`.

3. **View the Rendered Image**:
    - The rendered image will be displayed in a new window.

## Requirements

- Kotlin 1.8+
- Java 11+

## We are open to PR's!
