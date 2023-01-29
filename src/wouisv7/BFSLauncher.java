package wouisv7;//package wouisv6;
//import battlecode.common.Direction;
//import battlecode.common.MapLocation;
//import battlecode.common.RobotController;
//
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.Queue;
//
//
//public class BFSLauncher extends BFS {
//
//    BFSLauncher(RobotController rc) {
//        super(rc);
//    }
//    int[] getBestPath(MapLocation target){
//        MapLocation myLoc = rc.getLocation();
//        Queue<int[]> q = new LinkedList<>();
//        int[] startPath = new int[]{Util.encodeLoc(myLoc)};
//        HashSet<Integer> visited = new HashSet<>();
//        q.add(startPath);
//        while(q.size() > 0){
//            int[] path = q.poll();
//            int node = path[path.length - 1];
//            MapLocation nodeLoc = Util.getLocation(node);
//            if(nodeLoc.equals(target)){
//                return path;
//            }
//
//            for(Direction dir: Robot.directions){
//                MapLocation newLoc = nodeLoc.add(dir);
//                int newLocCode = Util.encodeLoc(newLoc);
//                if(visited.contains(newLocCode)){
//                    continue;
//                }
//
//
//            }
//        }
//    }
//}