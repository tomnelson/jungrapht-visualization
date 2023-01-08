package org.jungrapht.visualization.layout.algorithms.orthogonal;

import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Grid<V> {

//      int width;
//      int height;
//      int gridWidth;
//      int gridHeight;
//      List<List<Rectangle>> cells;
//      public Grid(int width, int height) {
//          this.gridWidth = width;
//          this.gridHeight = height;
////          this.cells = new Cell[width][height];
////          for (int i=0; i<gridHeight; i++) {
////              for (int j=0; j<gridWidth; j++) {
////                  this.cells[i][j] = Cell.of(i, j, 1, 1);
////              }
////          }
//      }
//
//      Cell<V> getCellContaining(double x, double y) {
//          return this.cells[(int)x][(int)y];
//      }
//
//      Cell<V> getRectangleContaining(V v) {
//          return Arrays.stream(cells).flatMap(c -> Arrays.stream(c))
//                  .filter(cell -> cell.getOccupant() == v)
//                  .findFirst().orElse(null);
//      }
//
//      List<Cell<V>> neighborsOf(Cell<V> r, int dist) {
//          int x = (int)r.x;
//          int y = (int)r.y;
//          List<Cell<V>> list = new ArrayList<>();
//          // start above, CW around all neighbors
//          if (y > 0) {
//              list.add(cells[x][y - dist]);
//          }
//
//          if (x < gridWidth-dist) {
//              if (y > 0) {
//                  list.add(cells[x][y - dist]);
//              }
//              list.add(cells[x + dist][y]);
//              if (y < gridHeight-dist) {
//                  list.add(cells[x + dist][y + dist]);
//              }
//          }
//
//          if (y < gridHeight-dist) {
//              list.add(cells[x][y + dist]);
//          }
//
//          if (x > 0) {
//              if (y < gridHeight-dist) {
//                  list.add(cells[x - dist][y + dist]);
//              }
//              list.add(cells[x - dist][y]);
//              if (y > 0) {
//                  list.add(cells[x - dist][y - dist]);
//              }
//          }
//          return list;
//      }
//
//      Collection<Cell<V>> findNearbyEmptyCells(Cell cell, int dist) {
//          List<Cell<V>> neighbors = neighborsOf(cell, dist);
//          Collection<Cell<V>> cells = neighbors.stream().filter(c -> !c.isOccupied()).collect(Collectors.toList());
//          if (!cells.isEmpty()) {
//              return cells;
//          } else {
//              return findNearbyEmptyCells(cell, dist++);
//          }
//      }
//
//      List<Cell<V>> neighborsOf(Cell<V> r) {
//          return neighborsOf(r, 1);
//      }
//
////      static class Cell<V> {
////
////          double x;
////          double y;
////          double width;
////          double height;
////          V occupant;
////
////          /**
////           * @param x      left most x location
////           * @param y      top most y location
////           * @param width  horizontal size of rectangle when aligned
////           * @param height vertical size of rectangle when aligned
////           */
////          public Cell(double x, double y, double width, double height) {
////              this(x, y, width, height, null);
////          }
////
////          /**
////           * @param x      left most x location
////           * @param y      top most y location
////           * @param width  horizontal size of rectangle when aligned
////           * @param height vertical size of rectangle when aligned
////           * @param occupant a vertex contained in this Cell
////           */
////          public Cell(double x, double y, double width, double height, V occupant) {
////              this.x = x;
////              this.y = y;
////              this.width = width;
////              this.height = height;
////              this.occupant = occupant;
////          }
////
////
////          /**
////           * @param x location of upper left corner
////           * @param y location of upper left corner
////           * @param width size in x dimension
////           * @param height size in y dimension
////           * @return a new Rectangle with the passed properties
////           */
////          public static Cell of(double x, double y, double width, double height) {
////              return new Cell(x, y, width, height);
////          }
////
////          public static Cell of(double width, double height) {
////              return new Cell(0, 0, width, height);
////          }
////
////  //        /**
////  //         * @param x location of upper left corner
////  //         * @param y location of upper left corner
////  //         * @param width size in x dimension
////  //         * @param height size in y dimension
////  //         * @return a new Rectangle with the passed properties
////  //         */
////  //        public static Cell of(double x, double y, double width, double height) {
////  //            return new Cell(x, y, width, height);
////  //        }
////
////          public static Cell from(Point min, Point max) {
////
////              return new Cell(min.x, min.y, max.x - min.x, max.y - min.y);
////          }
////
////          public boolean isOccupied() {
////              return occupant != null;
////          }
////
////          public void setOccupant(V v) {
////              this.occupant = v;
////          }
////
////          public V getOccupant() {
////              return this.occupant;
////          }
////
////          @Override
////          public String toString() {
////              return "Cell{" +
////                      "occupant=" + occupant +
////                      ", x=" + x +
////                      ", y=" + y +
////                      '}';
////          }
////
////          @Override
////          public boolean equals(Object o) {
////              if (this == o) return true;
////              if (o == null || getClass() != o.getClass()) return false;
////              Cell<?> cell = (Cell<?>) o;
////              return Double.compare(cell.x, x) == 0 && Double.compare(cell.y, y) == 0 && Double.compare(cell.width, width) == 0 && Double.compare(cell.height, height) == 0 && Objects.equals(occupant, cell.occupant);
////          }
////
////          @Override
////          public int hashCode() {
////              return Objects.hash(x, y, width, height, occupant);
////          }
////      }
//
//
//    public static <V> Set<Cell<V>> getUnoccupiedCellsAtDistFromCell(Cell[][] cellGrid, Cell<V> cell, int dist) {
//        Set<Cell<V>> set = new HashSet<>();
//        int x = (int)cell.getX();
//        int y = (int)cell.getY();
//        for (int i=-dist; i<=dist; i++) {
//            int nx = x+i;
//            if (nx < 0 || nx >= cellGrid[0].length) { // width of array
//                continue;
//            }
//            int ny = y+i+dist;
//            if (ny >= 0 || ny >= cellGrid.length) { // height of array
//                Cell c = cellGrid[nx][ny];
//                if (c.occupant == null) {
//                    set.add(c);
//                }
//            }
//            ny = y-i-dist;
//            if (ny >= 0 || ny >= cellGrid.length) {
//                Cell c = cellGrid[nx][ny];
//                if (c.occupant == null) {
//                    set.add(c);
//                }
//            }
//        }
//        return set;
//    }
//
}
