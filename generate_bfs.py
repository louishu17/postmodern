# Written by Tim Kaler (tfk) --- this is some version of my pathing codegen for battlecode 2022. Not sure if its the final version or not. 

VISION_RADIUS_SQ = 20




pre_valid_offsets = []

for dx in range(-10,10):
  for dy in range(-10,10):
    if dx**2 + dy**2 <= VISION_RADIUS_SQ:
      pre_valid_offsets.append((dx**2+dy**2,dx,dy))

pre_valid_offsets = (sorted(pre_valid_offsets))[::-1]


valid_offsets = []
for offset in pre_valid_offsets:
  valid_offsets.append((offset[1],offset[2]))


offset_ring_index = dict()
for offset in valid_offsets:
  offset_ring_index[offset] = 100

offset_ring_index[(0,0)] = 0

all_offset_set = False
while not all_offset_set:
  all_offset_set = True
  for offset in valid_offsets:
    for dx in range (-1, 2):
      for dy in range (-1, 2):
        if (dx==dy and dy == 0) \
	    or (offset[0]+dx, offset[1]+dy) not in offset_ring_index:
          continue
        if offset_ring_index[offset] > offset_ring_index[(offset[0]+dx, offset[1] + dy)] + 1:
          offset_ring_index[offset] = offset_ring_index[(offset[0]+dx, offset[1]+dy)] + 1
          all_offset_set = False

def get_adjacent_offsets_towards_origin(offset):
  neighbor_list = []
  for dx in range (-1,2):
    for dy in range (-1,2):
      if (dx==dy and dy == 0) \
          or (offset[0]+dx, offset[1]+dy) not in offset_ring_index:
        continue
      if offset_ring_index[offset] < offset_ring_index[(offset[0]+dx, offset[1]+dy)]:
        neighbor_list.append((offset[0]+dx, offset[1]+dy))
  return neighbor_list



def get_location_var_name(offset):
  dx = offset[0]
  dy = offset[1]
  if dx < 0:
    dx = "m"+str(abs(dx))
  else:
    dx = str(dx)
  if dy < 0:
    dy = "m"+str(abs(dy))
  else:
    dy = str(dy)
  return "location_" + dx + "_" + dy


def get_distance_var_name(offset):
  dx = offset[0]
  dy = offset[1]
  if dx < 0:
    dx = "m"+str(abs(dx))
  else:
    dx = str(dx)
  if dy < 0:
    dy = "m"+str(abs(dy))
  else:
    dy = str(dy)
  return "distance_" + dx + "_" + dy

def get_rubble_var_name(offset):
  dx = offset[0]
  dy = offset[1]
  if dx < 0:
    dx = "m"+str(abs(dx))
  else:
    dx = str(dx)
  if dy < 0:
    dy = "m"+str(abs(dy))
  else:
    dy = str(dy)
  return "rubble_" + dx + "_" + dy


def declare_rubble_vars(valid_offsets):
  statement_list = []
  for offset in valid_offsets:
    varname = get_rubble_var_name(offset)
    statement = "public static double " + varname + ";"
    statement_list.append(statement)
    #print statement
  #print len(statement_list)
  return "\n".join(statement_list)

def declare_location_vars(valid_offsets):
  statement_list = []
  for offset in valid_offsets:
    varname = get_location_var_name(offset)
    statement = "public static MapLocation " + varname + ";"
    statement_list.append(statement)
    #print statement
  #print len(statement_list)
  return "\n".join(statement_list)

def declare_distance_vars(valid_offsets):
  statement_list = [] 
  for offset in valid_offsets:
    varname = get_distance_var_name(offset)
    statement = "public static double " + varname + ";"
    statement_list.append(statement)
  return "\n".join(statement_list)


def init_rubble_vars(valid_offsets):
  statement_list = []
  statement_template = """
    @{varname_location} = rc.getLocation().translate(@{offset_0}, @{offset_1});
    if (rc.onTheMap(@{varname_location})) {
      @{varname} = 1.0 + rc.senseRubble(@{varname_location})/10.0;
      @{varname_distance} = 10*Math.sqrt(@{varname_location}.distanceSquaredTo(target));
    } else {
      @{varname} = 100000000.0;
      @{varname_distance} = 100000000.0;
    }
    """
  for offset in valid_offsets:
    varname = get_rubble_var_name(offset)
    statement = statement_template.replace("@{offset_0}", str(offset[0])).replace("@{offset_1}", str(offset[1])).replace("@{varname}", varname).replace("@{varname_distance}", get_distance_var_name(offset)).replace("@{varname_location}", get_location_var_name(offset))
    #statement = varname + " = rc.senseRubble(rc.getLocation().translate("+str(offset[0])+"," + str(offset[1]) +"))/10.0;"
    statement_list.append(statement)
    #print statement
  #print len(statement_list)
  return "\n".join(statement_list)

def init_rubble_vars_specialized(valid_offsets, min_dx, max_dx, min_dy, max_dy):
  statement_list = []

  statement_template_onmap = """
    @{varname_location} = rc.getLocation().translate(@{offset_0}, @{offset_1});
    @{varname} = 1.0;
    @{varname_distance} = 10*Math.sqrt(@{varname_location}.distanceSquaredTo(target));
    """

  statement_template_offmap = """
    @{varname_location} = rc.getLocation().translate(@{offset_0}, @{offset_1});
    @{varname} = 100000000.0;
    @{varname_distance} = 100000000.0;
    """

  for offset in valid_offsets:
    statement_template = None
    if offset[0] >= min_dx and offset[0] <= max_dx and offset[1] >= min_dy and offset[1] <= max_dy:
        # on the map
        statement_template = statement_template_onmap
    else:
        # off the map
        statement_template = statement_template_offmap
    varname = get_rubble_var_name(offset)
    statement = statement_template.replace("@{offset_0}", str(offset[0])).replace("@{offset_1}", str(offset[1])).replace("@{varname}", varname).replace("@{varname_distance}", get_distance_var_name(offset)).replace("@{varname_location}", get_location_var_name(offset))
    #statement = varname + " = rc.senseRubble(rc.getLocation().translate("+str(offset[0])+"," + str(offset[1]) +"))/10.0;"
    statement_list.append(statement)
    #print statement
  #print len(statement_list)
  return "\n".join(statement_list)
   


 
def relaxation(valid_offsets):
  statement_list = []

  template = """
  if (@{point_dist} > @{neighbor_dist} + @{neighbor_cost}) {
    @{point_dist} = @{neighbor_dist} + @{neighbor_cost};
  }
  """
  for offset in valid_offsets:
    if offset == (0,0):
      continue
    neighbors = get_adjacent_offsets_towards_origin(offset)
    for n in neighbors:
      statement = template.replace("@{neighbor_dist}", get_distance_var_name(n)).replace("@{neighbor_cost}", get_rubble_var_name(n)).replace("@{point_dist}", get_distance_var_name(offset))
      statement_list.append(statement)
  return "\n".join(statement_list)
 
def choose_best_location(valid_offsets):
  statement_list = []

  statement_list.append("MapLocation best_location = null;")
  statement_list.append("//distance_0_0 = 100000000;")

  template = """
  if (@{point_dist} > @{neighbor_dist} + @{neighbor_cost} && !rc.isLocationOccupied(@{neighbor_loc})) {
    @{point_dist} = @{neighbor_dist} + @{neighbor_cost};
    best_location = @{neighbor_loc};
  }
  """
  for offset in valid_offsets:
    if offset != (0,0):
      continue
    neighbors = get_adjacent_offsets_towards_origin(offset)
    for n in neighbors:
      statement = template.replace("@{neighbor_dist}", get_distance_var_name(n)).replace("@{neighbor_cost}", get_rubble_var_name(n)).replace("@{point_dist}", get_distance_var_name(offset)).replace("@{neighbor_loc}", get_location_var_name(n))
      statement_list.append(statement)


  end_of_statement = """ if (best_location == null && ten_pointo != 10.0) { 
      ten_pointo_value = 10.0;
      MapLocation res = do_BF(target); 
      ten_pointo_value = ten_pointo_value_default;
      return res; 
  } 
  if (best_location != null) {
      rc.setIndicatorDot(best_location, 0, 0, 255); 
  } else { 
      rc.setIndicatorDot(rc.getLocation(), 100,100,100); 
  }
  return best_location; 
  """

  #statement_list.append("return best_location;")
  statement_list.append(end_of_statement)
  return "\n".join(statement_list)


extra_methods = ""

def expand_base(name):
    global valid_offsets
    global extra_methods

    #print name
    arglist = name.replace("test_function_", "").split("_")[1:]
    #print arglist

    min_dx = None
    max_dx = None
    if arglist[0] == "default":
        max_dx = 100
    else:
        max_dx = int(arglist[0])

    if arglist[1] == "default":
        min_dx = -100
    else:
        min_dx = -1 * int(arglist[1])

    if arglist[2] == "default":
        max_dy = 100
    else:
        max_dy = int(arglist[2])

    if arglist[3] == "default":
        min_dy = -100
    else:
        min_dy = -1*int(arglist[3])
    extra_methods += "static public void generated_" + name + "(MapLocation target) throws GameActionException {\n" +init_rubble_vars_specialized(valid_offsets, min_dx, max_dx, min_dy, max_dy) + "\n}"
    return "generated_" + name + "(target);"

    #print str((max_dx, min_dx, max_dy, min_dy))
    #return "".join(arglist)



def switch_statement(switch_dict, case_chain):

    line_list = []
    line_list.append("switch(" + switch_dict["switch_key"] + ") {") 


    copied_dict = dict()
    default_case = None
    switch_key = None

    for x in switch_dict.keys():
        if x == "default":
            if type(switch_dict[x]) is dict:
                default_case = switch_statement(switch_dict[x], case_chain + "_default")
            else:
                default_case = switch_dict[x].replace("@{CASECHAIN}", expand_base(case_chain + "_default"))
            continue
        if x == "switch_key":
            switch_key = switch_dict[x]
            continue
        if type(switch_dict[x]) is dict:
            copied_dict[x] = switch_statement(switch_dict[x], case_chain + "_" + str(x))
        else:
            copied_dict[x] = switch_dict[x].replace("@{CASECHAIN}", expand_base(case_chain + "_" + str(x)))

    for x in copied_dict.keys():
        line_list.append("case " + str(x) + ":")
        line_list.append(copied_dict[x])
        line_list.append("break;")
    line_list.append("default:")
    line_list.append(default_case)
    line_list.append("break;")
    line_list.append("}")
    data = "\n".join(line_list)
    return data






def gen_switches():
    yswitch_inner = dict()
    yswitch_inner["switch_key"] = "rc.getLocation().y"
    yswitch_inner["default"] = "@{CASECHAIN}"
    yswitch_inner[0] = "@{CASECHAIN}"
    yswitch_inner[1] = "@{CASECHAIN}"
    yswitch_inner[2] = "@{CASECHAIN}"
    yswitch_inner[3] = "@{CASECHAIN}"

    yswitch_inner_sparse = dict()
    yswitch_inner_sparse["switch_key"] = "rc.getLocation().y"
    yswitch_inner_sparse["default"] = "@{CASECHAIN}"

    yswitch_outer = dict()
    yswitch_outer["switch_key"] = "rc.getMapHeight() - 1 - rc.getLocation().y"

    yswitch_outer["default"] = yswitch_inner
    yswitch_outer[0] = yswitch_inner_sparse
    yswitch_outer[1] = yswitch_inner_sparse
    yswitch_outer[2] = yswitch_inner_sparse
    yswitch_outer[3] = yswitch_inner_sparse


    xswitch_inner = dict()
    xswitch_inner["switch_key"] = "rc.getLocation().x"
    xswitch_inner["default"] = yswitch_outer
    xswitch_inner[0] = yswitch_outer
    xswitch_inner[1] = yswitch_outer
    xswitch_inner[2] = yswitch_outer
    xswitch_inner[3] = yswitch_outer
    xswitch_inner[4] = yswitch_outer

    xswitch_inner_sparse = dict()
    xswitch_inner_sparse["switch_key"] = "rc.getLocation().x"
    xswitch_inner_sparse["default"] = yswitch_outer


    xswitch_outer = dict()
    xswitch_outer["switch_key"] = "rc.getMapWidth() - 1 - rc.getLocation().x"
    xswitch_outer["default"] = xswitch_inner
    xswitch_outer[0] = xswitch_inner_sparse
    xswitch_outer[1] = xswitch_inner_sparse
    xswitch_outer[2] = xswitch_inner_sparse
    xswitch_outer[3] = xswitch_inner_sparse
    xswitch_outer[4] = xswitch_inner_sparse

    #print switch_statement(xswitch_outer, "")
    return switch_statement(xswitch_outer, "")










print ("""import battlecode.common.*;

public class NavigationBF {
    // Static variable declarations
        public static RobotController rc;
        public static int NUM_ITERS = 1;
        public static double ten_pointo_value_default = 2.0;
        public static double ten_pointo_value = ten_pointo_value_default;
""")

print (declare_rubble_vars(valid_offsets))
print (declare_distance_vars(valid_offsets))
print (declare_location_vars(valid_offsets))
print ("static public MapLocation do_BF(MapLocation target) throws GameActionException {")
print ("int ten = 10;")
print ("double ten_pointo = ten_pointo_value;")
#print init_rubble_vars(valid_offsets)
print (gen_switches())
print ("for (int iter  = 0; iter < NUM_ITERS; iter++) {")
print (relaxation(valid_offsets))
print ("}")
print (choose_best_location(valid_offsets))
print ("}")
print (extra_methods)
print ("}")
 

    

