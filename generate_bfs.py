import sys
from pathlib import Path

def encode(x, y):
    return (x+7) + 15*(y+7)

# Note: Sage should be (34, 20) but we've reduced for bytecode reasons
# Note: Builder should be (20, 10) but we've reduced for bytecode reasons
RADII = {'Miner': 20, 'Builder': 13, 'Soldier': 20, 'Sage': 20, 'Archon': 34, 'Watchtower': 34, 'Laboratory': 20}
SMALLER_RADII = {'Miner': 10, 'Builder': 5, 'Soldier': 10, 'Sage': 10, 'Archon': 20, 'Watchtower': 20, 'Laboratory': 10}

DIRECTIONS = {
    (1, 0): 'Direction.EAST',
    (-1, 0): 'Direction.WEST',
    (0, 1): 'Direction.NORTH',
    (0, -1): 'Direction.SOUTH',
    (1, 1): 'Direction.NORTHEAST',
    (-1, 1): 'Direction.NORTHWEST',
    (1, -1): 'Direction.SOUTHEAST',
    (-1, -1): 'Direction.SOUTHWEST',
}

def dist(x, y):
    return x*x + y*y

def gen_constants(radius):
    out = f""""""
    for x in range(-7, 8):
        for y in range(-7, 8):
            if dist(x, y) <= radius:
                out += f"""
    static MapLocation l{encode(x,y)}; // location representing relative coordinate ({x}, {y})
    static int d{encode(x,y)}; // shortest distance to location from current location
    static Direction dir{encode(x,y)}; // best direction to take now to optimally get to location
"""
    return out

def sign(x):
    if x > 0:
        return 1
    if x < 0:
        return -1
    return 0

def gen_init(radius):
    out = f"""
        l{encode(0,0)} = rc.getLocation();
        d{encode(0,0)} = 0;
        dir{encode(0,0)} = Direction.CENTER;
"""
    for r2 in range(1, radius+1):
        for x in range(-7, 8):
            for y in range(-7, 8):
                if dist(x, y) == r2:
                    out += f"""
        l{encode(x,y)} = l{encode(x - sign(x), y - sign(y))}.add({DIRECTIONS[(sign(x), sign(y))]}); // ({x}, {y}) from ({x - sign(x)}, {y - sign(y)})
        d{encode(x,y)} = 99999;
        dir{encode(x,y)} = null;
"""
    return out

def gen_bfs(radius):
    visited = set([encode(0,0)])
    out = f"""
"""
    for r2 in range(1, radius+1):
        for x in range(-7, 8):
            for y in range(-7, 8):
                if dist(x, y) == r2:
                    out += f"""
        if (rc.canSenseLocation(l{encode(x,y)}) && rc.senseMapInfo(l{encode(x,y)}).isPassable()) {{ // check ({x}, {y})"""
                    indent = ""
                    if r2 <= 2:
                        out += f"""
            if (!rc.isLocationOccupied(l{encode(x,y)})) {{ """
                        indent = "    "
                    dxdy = [(dx, dy) for dx in range(-1, 2) for dy in range(-1, 2) if (dx, dy) != (0, 0) and dist(x+dx,y+dy) <= radius]
                    dxdy = sorted(dxdy, key=lambda dd: dist(x+dd[0], y+dd[1]))
                    for dx, dy in dxdy:
                        if encode(x+dx, y+dy) in visited:
                            out += f"""
            {indent}if (d{encode(x,y)} > d{encode(x+dx,y+dy)}) {{ // from ({x+dx}, {y+dy})
                {indent}d{encode(x,y)} = d{encode(x+dx,y+dy)};
                {indent}dir{encode(x,y)} = {DIRECTIONS[(-dx, -dy)] if (x+dx,y+dy) == (0, 0) else f'dir{encode(x+dx,y+dy)}'};
            {indent}}}"""
                    out += f"""
            {indent}d{encode(x,y)} += 10;"""
                    if r2 <= 2:
                        out += f"""
            }}"""
                    visited.add(encode(x,y))
                    out += f"""
        }}
"""
    return out

def gen_selection(radius, smaller_radius):
    out = f"""
        int target_dx = target.x - l{encode(0,0)}.x;
        int target_dy = target.y - l{encode(0,0)}.y;
        switch (target_dx) {{"""
    for tdx in range(-7, 8):
        if tdx**2 <= radius:
            out += f"""
                case {tdx}:
                    switch (target_dy) {{"""
            for tdy in range(-7, 8):
                if dist(tdx, tdy) <= radius:
                    out += f"""
                        case {tdy}:
                            return dir{encode(tdx, tdy)}; // destination is at relative location ({tdx}, {tdy})"""
            out += f"""
                    }}
                    break;"""
    out += f"""
        }}
        
        Direction ans = null;
        double bestScore = 0;
        double currDist = Math.sqrt(l{encode(0,0)}.distanceSquaredTo(target));
        """
    for x in range(-7, 8):
        for y in range(-7, 8):
            if smaller_radius < dist(x, y) <= radius: # on the edge of the radius radius
                out += f"""
        double score{encode(x,y)} = (currDist - Math.sqrt(l{encode(x,y)}.distanceSquaredTo(target))) / d{encode(x,y)};
        if (score{encode(x,y)} > bestScore) {{
            bestScore = score{encode(x,y)};
            ans = dir{encode(x,y)};
        }}
"""
    return out

def gen_print(radius):
    out = f"""
        // System.out.println("LOCAL DISTANCES:");"""
    for y in range(7, -8, -1):
        if y**2 <= radius:
            out += f"""
        // System.out.println("""
            for x in range(-7, 8):
                if dist(x, y) <= radius:
                    out += f""""\\t" + d{encode(x,y)} + """
                else:
                    out += f""""\\t" + """
            out = out[:-3] + """);"""
    out += f"""
        // System.out.println("DIRECTIONS:");"""
    for y in range(7, -8, -1):
        if y**2 <= radius:
            out += f"""
        // System.out.println("""
            for x in range(-7, 8):
                if dist(x, y) <= radius:
                    out += f""""\\t" + dir{encode(x,y)} + """
                else:
                    out += f""""\\t" + """
            out = out[:-3] + """);"""
    return out

def gen_full(bot, unit):
    radius = RADII[unit]
    smaller_radius = SMALLER_RADII[unit]
    out_file = Path('./src/') / bot / f'{unit}Pathing.java'
    with open(out_file, 'w') as f:
        f.write(f"""// Inspired by https://github.com/IvanGeffner/battlecode2021/blob/master/thirtyone/BFSMuckraker.java.
package {bot};

import battlecode.common.*;

public class {unit}Pathing implements UnitPathing {{
    
    RobotController rc;
{gen_constants(radius)}

    public {unit}Pathing(RobotController rc) {{
        this.rc = rc;
    }}

    public Direction bestDir(MapLocation target) throws GameActionException {{
{gen_init(radius)}
{gen_bfs(radius)}
{gen_print(radius)}
{gen_selection(radius, smaller_radius)}
        
        return ans;
    }}
}}
""")

if __name__ == '__main__':
    gen_full(sys.argv[1], sys.argv[2])