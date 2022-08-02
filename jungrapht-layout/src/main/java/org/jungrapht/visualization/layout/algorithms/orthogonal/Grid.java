package org.jungrapht.visualization.layout.algorithms.orthogonal;

class Grid<V> {
  //
  //    int width;
  //    int height;
  //    int gridWidth;
  //    int gridHeight;
  //    List<List<Cell<V>>> cells;
  //    public Grid(int size) {
  //        this.gridWidth = this.gridHeight = size;
  //        this.cells = new ArrayList<>();
  //        for (int i=0; i<size; i++) {
  //            this.cells.add(new ArrayList<>());
  //        }
  //        for (int i=0; i<gridHeight; i++) {
  //            for (int j=0; j<gridWidth; j++) {
  //                this.cells.get(i).add(Cell.of(i, j, 1, 1));
  //            }
  //        }
  //    }
  //
  //    Cell<V> getRectangleContaining(double x, double y) {
  //        return this.cells.get((int)x).get((int)y);
  //    }
  //
  //    Cell<V> getRectangleContaining(V v) {
  //        return cells.stream().flatMap(Collection::stream)
  //                .filter(cell -> cell.getOccupant() == v)
  //                .findFirst().orElse(null);
  //    }
  //
  //    List<Cell<V>> neighborsOf(Cell<V> r, int dist) {
  //        int x = (int)r.x;
  //        int y = (int)r.y;
  //        List<Cell<V>> list = new ArrayList<>();
  //        // start above, CW around all neighbors
  //        if (y > 0) {
  //            list.add(cells.get(x).get(y - dist));
  //        }
  //
  //        if (x < gridWidth-dist) {
  //            if (y > 0) {
  //                list.add(cells.get(x + dist).get(y - dist));
  //            }
  //            list.add(cells.get(x + dist).get(y));
  //            if (y < gridHeight-dist) {
  //                list.add(cells.get(x + dist).get(y + dist));
  //            }
  //        }
  //
  //        if (y < gridHeight-dist) {
  //            list.add(cells.get(x).get(y + dist));
  //        }
  //
  //        if (x > 0) {
  //            if (y < gridHeight-dist) {
  //                list.add(cells.get(x - dist).get(y + dist));
  //            }
  //            list.add(cells.get(x - dist).get(y));
  //            if (y > 0) {
  //                list.add(cells.get(x - dist).get(y - dist));
  //            }
  //        }
  //        return list;
  //    }
  //
  //    Collection<Cell<V>> findNearbyEmptyCells(Cell cell, int dist) {
  //        List<Cell<V>> neighbors = neighborsOf(cell, dist);
  //        Collection<Cell<V>> cells = neighbors.stream().filter(c -> !c.isOccupied()).collect(Collectors.toList());
  //        if (!cells.isEmpty()) {
  //            return cells;
  //        } else {
  //            return findNearbyEmptyCells(cell, dist++);
  //        }
  //    }
  //
  //    List<Cell<V>> neighborsOf(Cell<V> r) {
  //        return neighborsOf(r, 1);
  //    }
  //
  //    static class Cell<V> {
  //
  //        double x;
  //        double y;
  //        double width;
  //        double height;
  //        V occupant;
  //
  //        /**
  //         * @param x      left most x location
  //         * @param y      top most y location
  //         * @param width  horizontal size of rectangle when aligned
  //         * @param height vertical size of rectangle when aligned
  //         */
  //        public Cell(double x, double y, double width, double height) {
  //            this(x, y, width, height, null);
  //        }
  //
  //        /**
  //         * @param x      left most x location
  //         * @param y      top most y location
  //         * @param width  horizontal size of rectangle when aligned
  //         * @param height vertical size of rectangle when aligned
  //         * @param occupant a vertex contained in this Cell
  //         */
  //        public Cell(double x, double y, double width, double height, V occupant) {
  //            this.x = x;
  //            this.y = y;
  //            this.width = width;
  //            this.height = height;
  //            this.occupant = occupant;
  //        }
  //
  //
  //        /**
  //         * @param x location of upper left corner
  //         * @param y location of upper left corner
  //         * @param width size in x dimension
  //         * @param height size in y dimension
  //         * @return a new Rectangle with the passed properties
  //         */
  //        public static Cell of(double x, double y, double width, double height) {
  //            return new Cell(x, y, width, height);
  //        }
  //
  //        public static Cell of(double width, double height) {
  //            return new Cell(0, 0, width, height);
  //        }
  //
  ////        /**
  ////         * @param x location of upper left corner
  ////         * @param y location of upper left corner
  ////         * @param width size in x dimension
  ////         * @param height size in y dimension
  ////         * @return a new Rectangle with the passed properties
  ////         */
  ////        public static Cell of(double x, double y, double width, double height) {
  ////            return new Cell(x, y, width, height);
  ////        }
  //
  //        public static Cell from(Point min, Point max) {
  //
  //            return new Cell(min.x, min.y, max.x - min.x, max.y - min.y);
  //        }
  //
  //        public boolean isOccupied() {
  //            return occupant != null;
  //        }
  //
  //        public void setOccupant(V v) {
  //            this.occupant = v;
  //        }
  //
  //        public V getOccupant() {
  //            return this.occupant;
  //        }
  //
  //        @Override
  //        public String toString() {
  //            return "Cell{" +
  //                    "occupant=" + occupant +
  //                    ", x=" + x +
  //                    ", y=" + y +
  //                    '}';
  //        }
  //
  //        @Override
  //        public boolean equals(Object o) {
  //            if (this == o) return true;
  //            if (o == null || getClass() != o.getClass()) return false;
  //            Cell<?> cell = (Cell<?>) o;
  //            return Double.compare(cell.x, x) == 0 && Double.compare(cell.y, y) == 0 && Double.compare(cell.width, width) == 0 && Double.compare(cell.height, height) == 0 && Objects.equals(occupant, cell.occupant);
  //        }
  //
  //        @Override
  //        public int hashCode() {
  //            return Objects.hash(x, y, width, height, occupant);
  //        }
  //    }
  //
}
