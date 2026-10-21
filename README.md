# Skyline

The core implementation files and dataset are located in the `src/main/java/com/github/davidmoten/rtree/` directory.

---

### 1. Skyline (`Skyline.java`)
- **Location**: `src/main/java/com/github/davidmoten/rtree/Skyline.java`
- **Key Functions**:
  - Implements the Branch-and-Bound Skyline (BBS) algorithm on top of the R-tree.
  - Traverses R-tree nodes and data points top-down using a priority queue ordered by minimum distance ($x + y$).
  - Dynamically prunes dominated MBR nodes and candidate points against the evolving skyline set.
  - Provides the `recover(...)` method for incremental recovery: upon deleting a skyline point, it restricts search to the upper-right region strictly dominated by that point to efficiently discover newly exposed skyline members.

---

### 2. SkylineMain (`SkylineMain.java`)
- **Location**: `src/main/java/com/github/davidmoten/rtree/SkylineMain.java`
- **Key Functions**:
  - Main application entry point (`main` method).
  - Reads and parses 2D coordinate points from `Dataset_for_PA1` and builds the R-tree spatial index.
  - Calls `Skyline.compute(tree)` to compute the skyline points.
  - Prints the total point count, skyline size, and individual skyline point IDs and coordinates to the console.

---

### 3. DynamicSkyline (`DynamicSkyline.java`)
- **Location**: `src/main/java/com/github/davidmoten/rtree/DynamicSkyline.java`
- **Key Functions**:
  - A wrapper class that dynamically maintains the skyline under insertions and deletions.
  - **Dynamic Insertion (`add`)**: Inserts a new point into the R-tree; checks whether it is dominated by current skyline members, and if not, adds it to the skyline and removes points dominated by this new point.
  - **Dynamic Deletion (`delete`)**: Removes a point from the R-tree; if the removed point belonged to the skyline, calls `Skyline.recover(...)` to recompute and restore skyline members in the affected region.
  - Exposes getter methods for the current skyline set (`skyline()`) and the underlying R-tree (`tree()`).

---

### 4. Dataset (`Dataset_for_PA1`)
- **Location**: `src/main/java/com/github/davidmoten/rtree/Dataset_for_PA1`
- **Key Functions**:
  - Test and benchmark dataset file.
  - Contains 2D spatial point coordinates (space-separated $x$ and $y$ per line) used for populating the R-tree and verifying algorithm correctness.

---

### 5. DynamicSkylineDemo.java

- **Location**: `src/main/java/com/github/davidmoten/rtree/DynamicSkylineDemo.java`
- Run its `main` method from the project root, just like `SkylineMain`.
- Loads the dataset and prints its original 8 Skyline points.
- Inserts one point, ID `-1` at `(32.82, 17.52)`, which dominates all original
  Skyline points. The Skyline then contains only this new point.
- Deletes that same point and prints the restored 8 original Skyline points.
- Prints `Original skyline restored: true` after checking the result.
- The dataset file is not modified. The demonstration point is chosen automatically
  from the dataset's bounds.


## How to Run `SkylineMain`

> **Note**: Always run from the project root directory (`skyLine/`) so that the program can locate `src/main/java/com/github/davidmoten/rtree/Dataset_for_PA1`.

### Method 1: In VS Code / IDE (Recommended)
1. Open [`src/main/java/com/github/davidmoten/rtree/SkylineMain.java`](src/main/java/com/github/davidmoten/rtree/SkylineMain.java).
2. Click the **Run** button right above `public static void main(String[] args)`.

### Method 2: From Terminal via `java`
Run the following command directly from the project root:
```bash
java -cp "target/classes:$HOME/.m2/repository/io/reactivex/rxjava/1.3.8/rxjava-1.3.8.jar:$HOME/.m2/repository/com/github/davidmoten/guava-mini/0.1.7/guava-mini-0.1.7.jar" com.github.davidmoten.rtree.SkylineMain
```

### Method 3: Using Maven
If Maven is installed (`brew install maven`):
```bash
mvn compile exec:java -Dexec.mainClass="com.github.davidmoten.rtree.SkylineMain"
```

### Expected Output
```text
points=38377
skyline=8
36516 33.92 20.08
36285 34.78 19.24
37191 34.36 19.77
20958 35.32 19.2
33873 36.13 19.05
474 36.6 19.0
37347 33.82 25.82
37296 41.47 18.52
```

AI was used to help implement skyline and assist with code review, incremental skyline maintenance, the dynamic demonstration, automated tests, and documentation.
