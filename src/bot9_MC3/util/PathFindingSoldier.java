package bot9_MC3.util;

import battlecode.common.*;

import java.util.HashSet;

public class PathFindingSoldier {
    private final RobotController rc;
    private static int width,height;
    private MapLocation exploreTarget;
    public PathFindingSoldier(RobotController rc){
        this.rc = rc;
        width = rc.getMapWidth();
        height = rc.getMapHeight();
        newExploreLocation();
    }

    static MapLocation l30;  static Direction d30;  static int v30;  static int p30;  static double dist30; static int dSQ30;
    static MapLocation l31;  static Direction d31;  static int v31;  static int p31;  static double dist31; static int dSQ31;
    static MapLocation l32;  static Direction d32;  static int v32;  static int p32;  static double dist32; static int dSQ32;
    static MapLocation l33;  static Direction d33;  static int v33;  static int p33;  static double dist33; static int dSQ33;
    static MapLocation l34;  static Direction d34;  static int v34;  static int p34;  static double dist34; static int dSQ34;
    static MapLocation l42;  static Direction d42;  static int v42;  static int p42;  static double dist42; static int dSQ42;
    static MapLocation l43;  static Direction d43;  static int v43;  static int p43;
    static MapLocation l44;  static Direction d44;  static int v44;  static int p44;
    static MapLocation l45;  static Direction d45;  static int v45;  static int p45;
    static MapLocation l46;  static Direction d46;  static int v46;  static int p46;
    static MapLocation l47;  static Direction d47;  static int v47;  static int p47;
    static MapLocation l48;  static Direction d48;  static int v48;  static int p48;  static double dist48; static int dSQ48;
    static MapLocation l54;  static Direction d54;  static int v54;  static int p54;  static double dist54; static int dSQ54;
    static MapLocation l55;  static Direction d55;  static int v55;  static int p55;
    static MapLocation l56;  static Direction d56;  static int v56;  static int p56;
    static MapLocation l57;  static Direction d57;  static int v57;  static int p57;
    static MapLocation l58;  static Direction d58;  static int v58;  static int p58;
    static MapLocation l59;  static Direction d59;  static int v59;  static int p59;
    static MapLocation l60;  static Direction d60;  static int v60;  static int p60;
    static MapLocation l61;  static Direction d61;  static int v61;  static int p61;
    static MapLocation l62;  static Direction d62;  static int v62;  static int p62;  static double dist62; static int dSQ62;
    static MapLocation l67;  static Direction d67;  static int v67;  static int p67;  static double dist67; static int dSQ67;
    static MapLocation l68;  static Direction d68;  static int v68;  static int p68;
    static MapLocation l69;  static Direction d69;  static int v69;  static int p69;
    static MapLocation l70;  static Direction d70;  static int v70;  static int p70;
    static MapLocation l71;  static Direction d71;  static int v71;  static int p71;
    static MapLocation l72;  static Direction d72;  static int v72;  static int p72;
    static MapLocation l73;  static Direction d73;  static int v73;  static int p73;
    static MapLocation l74;  static Direction d74;  static int v74;  static int p74;
    static MapLocation l75;  static Direction d75;  static int v75;  static int p75;  static double dist75; static int dSQ75;
    static MapLocation l80;  static Direction d80;  static int v80;  static int p80;  static double dist80; static int dSQ80;
    static MapLocation l81;  static Direction d81;  static int v81;  static int p81;
    static MapLocation l82;  static Direction d82;  static int v82;  static int p82;
    static MapLocation l83;  static Direction d83;  static int v83;  static int p83;
    static MapLocation l84;  static Direction d84;  static int v84;                                         static int dSQ84;
    static MapLocation l85;  static Direction d85;  static int v85;  static int p85;
    static MapLocation l86;  static Direction d86;  static int v86;  static int p86;
    static MapLocation l87;  static Direction d87;  static int v87;  static int p87;
    static MapLocation l88;  static Direction d88;  static int v88;  static int p88;  static double dist88; static int dSQ88;
    static MapLocation l93;  static Direction d93;  static int v93;  static int p93;  static double dist93; static int dSQ93;
    static MapLocation l94;  static Direction d94;  static int v94;  static int p94;
    static MapLocation l95;  static Direction d95;  static int v95;  static int p95;
    static MapLocation l96;  static Direction d96;  static int v96;  static int p96;
    static MapLocation l97;  static Direction d97;  static int v97;  static int p97;
    static MapLocation l98;  static Direction d98;  static int v98;  static int p98;
    static MapLocation l99;  static Direction d99;  static int v99;  static int p99;
    static MapLocation l100; static Direction d100; static int v100; static int p100;
    static MapLocation l101; static Direction d101; static int v101; static int p101; static double dist101; static int dSQ101;
    static MapLocation l106; static Direction d106; static int v106; static int p106; static double dist106; static int dSQ106;
    static MapLocation l107; static Direction d107; static int v107; static int p107;
    static MapLocation l108; static Direction d108; static int v108; static int p108;
    static MapLocation l109; static Direction d109; static int v109; static int p109;
    static MapLocation l110; static Direction d110; static int v110; static int p110;
    static MapLocation l111; static Direction d111; static int v111; static int p111;
    static MapLocation l112; static Direction d112; static int v112; static int p112;
    static MapLocation l113; static Direction d113; static int v113; static int p113;
    static MapLocation l114; static Direction d114; static int v114; static int p114; static double dist114; static int dSQ114;
    static MapLocation l120; static Direction d120; static int v120; static int p120; static double dist120; static int dSQ120;
    static MapLocation l121; static Direction d121; static int v121; static int p121;
    static MapLocation l122; static Direction d122; static int v122; static int p122;
    static MapLocation l123; static Direction d123; static int v123; static int p123;
    static MapLocation l124; static Direction d124; static int v124; static int p124;
    static MapLocation l125; static Direction d125; static int v125; static int p125;
    static MapLocation l126; static Direction d126; static int v126; static int p126; static double dist126; static int dSQ126;
    static MapLocation l134; static Direction d134; static int v134; static int p134; static double dist134; static int dSQ134;
    static MapLocation l135; static Direction d135; static int v135; static int p135; static double dist135; static int dSQ135;
    static MapLocation l136; static Direction d136; static int v136; static int p136; static double dist136; static int dSQ136;
    static MapLocation l137; static Direction d137; static int v137; static int p137; static double dist137; static int dSQ137;
    static MapLocation l138; static Direction d138; static int v138; static int p138; static double dist138; static int dSQ138;

    public Direction getBestDir(MapLocation target){
        l84  = rc.getLocation();                 v84 = 0;
        l85  = l84.add(Direction.NORTH);         v85 = 1000000;  d85 = null;
        l72  = l85.add(Direction.WEST);          v72 = 1000000;  d72 = null;
        l71  = l72.add(Direction.SOUTH);         v71 = 1000000;  d71 = null;
        l70  = l71.add(Direction.SOUTH);         v70 = 1000000;  d70 = null;
        l83  = l70.add(Direction.EAST);          v83 = 1000000;  d83 = null;
        l96  = l83.add(Direction.EAST);          v96 = 1000000;  d96 = null;
        l97  = l96.add(Direction.NORTH);         v97 = 1000000;  d97 = null;
        l98  = l97.add(Direction.NORTH);         v98 = 1000000;  d98 = null;
        l99  = l98.add(Direction.NORTH);         v99 = 1000000;  d99 = null;
        l86  = l99.add(Direction.WEST);          v86 = 1000000;  d86 = null;
        l73  = l86.add(Direction.WEST);          v73 = 1000000;  d73 = null;
        l60  = l73.add(Direction.WEST);          v60 = 1000000;  d60 = null;
        l59  = l60.add(Direction.SOUTH);         v59 = 1000000;  d59 = null;
        l58  = l59.add(Direction.SOUTH);         v58 = 1000000;  d58 = null;
        l57  = l58.add(Direction.SOUTH);         v57 = 1000000;  d57 = null;
        l56  = l57.add(Direction.SOUTH);         v56 = 1000000;  d56 = null;
        l69  = l56.add(Direction.EAST);          v69 = 1000000;  d69 = null;
        l82  = l69.add(Direction.EAST);          v82 = 1000000;  d82 = null;
        l95  = l82.add(Direction.EAST);          v95 = 1000000;  d95 = null;
        l108 = l95.add(Direction.EAST);         v108 = 1000000; d108 = null;
        l109 = l108.add(Direction.NORTH);       v109 = 1000000; d109 = null;
        l110 = l109.add(Direction.NORTH);       v110 = 1000000; d110 = null;
        l111 = l110.add(Direction.NORTH);       v111 = 1000000; d111 = null;
        l112 = l111.add(Direction.NORTH);       v112 = 1000000; d112 = null;
        l100 = l112.add(Direction.NORTHWEST);   v100 = 1000000; d100 = null;
        l87  = l100.add(Direction.WEST);         v87 = 1000000;  d87 = null;
        l74  = l87.add(Direction.WEST);          v74 = 1000000;  d74 = null;
        l61  = l74.add(Direction.WEST);          v61 = 1000000;  d61 = null;
        l47  = l61.add(Direction.SOUTHWEST);     v47 = 1000000;  d47 = null;
        l46  = l47.add(Direction.SOUTH);         v46 = 1000000;  d46 = null;
        l45  = l46.add(Direction.SOUTH);         v45 = 1000000;  d45 = null;
        l44  = l45.add(Direction.SOUTH);         v44 = 1000000;  d44 = null;
        l43  = l44.add(Direction.SOUTH);         v43 = 1000000;  d43 = null;
        l55  = l43.add(Direction.SOUTHEAST);     v55 = 1000000;  d55 = null;
        l68  = l55.add(Direction.EAST);          v68 = 1000000;  d68 = null;
        l81  = l68.add(Direction.EAST);          v81 = 1000000;  d81 = null;
        l94  = l81.add(Direction.EAST);          v94 = 1000000;  d94 = null;
        l107 = l94.add(Direction.EAST);         v107 = 1000000; d107 = null;
        l121 = l107.add(Direction.NORTHEAST);   v121 = 1000000; d121 = null;
        l122 = l121.add(Direction.NORTH);       v122 = 1000000; d122 = null;
        l123 = l122.add(Direction.NORTH);       v123 = 1000000; d123 = null;
        l124 = l123.add(Direction.NORTH);       v124 = 1000000; d124 = null;
        l125 = l124.add(Direction.NORTH);       v125 = 1000000; d125 = null;
        l113 = l125.add(Direction.NORTHWEST);   v113 = 1000000; d113 = null;
        l101 = l113.add(Direction.NORTHWEST);   v101 = 1000000; d101 = null;
        l88  = l101.add(Direction.WEST);         v88 = 1000000;  d88 = null;
        l75  = l88.add(Direction.WEST);          v75 = 1000000;  d75 = null;
        l62  = l75.add(Direction.WEST);          v62 = 1000000;  d62 = null;
        l48  = l62.add(Direction.SOUTHWEST);     v48 = 1000000;  d48 = null;
        l34  = l48.add(Direction.SOUTHWEST);     v34 = 1000000;  d34 = null;
        l33  = l34.add(Direction.SOUTH);         v33 = 1000000;  d33 = null;
        l32  = l33.add(Direction.SOUTH);         v32 = 1000000;  d32 = null;
        l31  = l32.add(Direction.SOUTH);         v31 = 1000000;  d31 = null;
        l30  = l31.add(Direction.SOUTH);         v30 = 1000000;  d30 = null;
        l42  = l30.add(Direction.SOUTHEAST);     v42 = 1000000;  d42 = null;
        l54  = l42.add(Direction.SOUTHEAST);     v54 = 1000000;  d54 = null;
        l67  = l54.add(Direction.EAST);          v67 = 1000000;  d67 = null;
        l80  = l67.add(Direction.EAST);          v80 = 1000000;  d80 = null;
        l93  = l80.add(Direction.EAST);          v93 = 1000000;  d93 = null;
        l106 = l93.add(Direction.EAST);         v106 = 1000000; d106 = null;
        l120 = l106.add(Direction.NORTHEAST);   v120 = 1000000; d120 = null;
        l134 = l120.add(Direction.NORTHEAST);   v134 = 1000000; d134 = null;
        l135 = l134.add(Direction.NORTH);       v135 = 1000000; d135 = null;
        l136 = l135.add(Direction.NORTH);       v136 = 1000000; d136 = null;
        l137 = l136.add(Direction.NORTH);       v137 = 1000000; d137 = null;
        l138 = l137.add(Direction.NORTH);       v138 = 1000000; d138 = null;
        l126 = l138.add(Direction.NORTHWEST);   v126 = 1000000; d126 = null;
        l114 = l126.add(Direction.NORTHWEST);   v114 = 1000000; d114 = null;

        //Maybe Remove the try/catch statement
        try{
            if(rc.onTheMap(l71)){
                if(!rc.isLocationOccupied(l71)){
                    p71 = turnsToMove(l71);
                    if(v71 > v84 + p71){
                        v71 = v84 + p71;
                        d71 = Direction.WEST;
                    }
                }
            }
            if (rc.onTheMap(l83)) {
                if (!rc.isLocationOccupied(l83)) {
                    p83 = turnsToMove(l83);
                    if (v83 > v84 + p83) {
                        v83 = v84 + p83;
                        d83 = Direction.SOUTH;
                    }
                    if (v83 > v71 + p83) {
                        v83 = v71 + p83;
                        d83 = d71;
                    }
                }
            }
            if (rc.onTheMap(l85)) {
                if (!rc.isLocationOccupied(l85)) {
                    p85 = turnsToMove(l85);
                    if (v85 > v84 + p85) {
                        v85 = v84 + p85;
                        d85 = Direction.NORTH;
                    }
                    if (v85 > v71 + p85) {
                        v85 = v71 + p85;
                        d85 = d71;
                    }
                }
            }
            if (rc.onTheMap(l97)) {
                if (!rc.isLocationOccupied(l97)) {
                    p97 = turnsToMove(l97);
                    if (v97 > v84 + p97) {
                        v97 = v84 + p97;
                        d97 = Direction.EAST;
                    }
                    if (v97 > v85 + p97) {
                        v97 = v85 + p97;
                        d97 = d85;
                    }
                    if (v97 > v83 + p97) {
                        v97 = v83 + p97;
                        d97 = d83;
                    }
                }
            }
            if (rc.onTheMap(l70)) {
                if (!rc.isLocationOccupied(l70)) {
                    p70 = turnsToMove(l70);
                    if (v70 > v84 + p70) {
                        v70 = v84 + p70;
                        d70 = Direction.SOUTHWEST;
                    }
                    if (v70 > v71 + p70) {
                        v70 = v71 + p70;
                        d70 = d71;
                    }
                    if (v70 > v83 + p70) {
                        v70 = v83 + p70;
                        d70 = d83;
                    }
                }
            }
            if (rc.onTheMap(l72)) {
                if (!rc.isLocationOccupied(l72)) {
                    p72 = turnsToMove(l72);
                    if (v72 > v84 + p72) {
                        v72 = v84 + p72;
                        d72 = Direction.NORTHWEST;
                    }
                    if (v72 > v71 + p72) {
                        v72 = v71 + p72;
                        d72 = d71;
                    }
                    if (v72 > v85 + p72) {
                        v72 = v85 + p72;
                        d72 = d85;
                    }
                }
            }
            if (rc.onTheMap(l96)) {
                if (!rc.isLocationOccupied(l96)) {
                    p96 = turnsToMove(l96);
                    if (v96 > v84 + p96) {
                        v96 = v84 + p96;
                        d96 = Direction.SOUTHEAST;
                    }
                    if (v96 > v97 + p96) {
                        v96 = v97 + p96;
                        d96 = d97;
                    }
                    if (v96 > v83 + p96) {
                        v96 = v83 + p96;
                        d96 = d83;
                    }
                }
            }
            if (rc.onTheMap(l98)) {
                if (!rc.isLocationOccupied(l98)) {
                    p98 = turnsToMove(l98);
                    if (v98 > v84 + p98) {
                        v98 = v84 + p98;
                        d98 = Direction.NORTHEAST;
                    }
                    if (v98 > v85 + p98) {
                        v98 = v85 + p98;
                        d98 = d85;
                    }
                    if (v98 > v97 + p98) {
                        v98 = v97 + p98;
                        d98 = d97;
                    }
                }
            }
            if (rc.onTheMap(l58)) {
                p58 = turnsToMove(l58);
                if (v58 > v71 + p58) {
                    v58 = v71 + p58;
                    d58 = d71;
                }
                if (v58 > v70 + p58) {
                    v58 = v70 + p58;
                    d58 = d70;
                }
                if (v58 > v72 + p58) {
                    v58 = v72 + p58;
                    d58 = d72;
                }
            }
            if (rc.onTheMap(l82)) {
                p82 = turnsToMove(l82);
                if (v82 > v83 + p82) {
                    v82 = v83 + p82;
                    d82 = d83;
                }
                if (v82 > v70 + p82) {
                    v82 = v70 + p82;
                    d82 = d70;
                }
                if (v82 > v96 + p82) {
                    v82 = v96 + p82;
                    d82 = d96;
                }
            }
            if (rc.onTheMap(l86)) {
                p86 = turnsToMove(l86);
                if (v86 > v85 + p86) {
                    v86 = v85 + p86;
                    d86 = d85;
                }
                if (v86 > v72 + p86) {
                    v86 = v72 + p86;
                    d86 = d72;
                }
                if (v86 > v98 + p86) {
                    v86 = v98 + p86;
                    d86 = d98;
                }
            }
            if (rc.onTheMap(l110)) {
                p110 = turnsToMove(l110);
                if (v110 > v97 + p110) {
                    v110 = v97 + p110;
                    d110 = d97;
                }
                if (v110 > v98 + p110) {
                    v110 = v98 + p110;
                    d110 = d98;
                }
                if (v110 > v96 + p110) {
                    v110 = v96 + p110;
                    d110 = d96;
                }
            }
            if (rc.onTheMap(l57)) {
                p57 = turnsToMove(l57);
                if (v57 > v71 + p57) {
                    v57 = v71 + p57;
                    d57 = d71;
                }
                if (v57 > v70 + p57) {
                    v57 = v70 + p57;
                    d57 = d70;
                }
                if (v57 > v58 + p57) {
                    v57 = v58 + p57;
                    d57 = d58;
                }
            }
            if (rc.onTheMap(l59)) {
                p59 = turnsToMove(l59);
                if (v59 > v71 + p59) {
                    v59 = v71 + p59;
                    d59 = d71;
                }
                if (v59 > v72 + p59) {
                    v59 = v72 + p59;
                    d59 = d72;
                }
                if (v59 > v58 + p59) {
                    v59 = v58 + p59;
                    d59 = d58;
                }
            }
            if (rc.onTheMap(l69)) {
                p69 = turnsToMove(l69);
                if (v69 > v83 + p69) {
                    v69 = v83 + p69;
                    d69 = d83;
                }
                if (v69 > v70 + p69) {
                    v69 = v70 + p69;
                    d69 = d70;
                }
                if (v69 > v82 + p69) {
                    v69 = v82 + p69;
                    d69 = d82;
                }
                if (v69 > v57 + p69) {
                    v69 = v57 + p69;
                    d69 = d57;
                }
            }
            if (rc.onTheMap(l73)) {
                p73 = turnsToMove(l73);
                if (v73 > v85 + p73) {
                    v73 = v85 + p73;
                    d73 = d85;
                }
                if (v73 > v72 + p73) {
                    v73 = v72 + p73;
                    d73 = d72;
                }
                if (v73 > v86 + p73) {
                    v73 = v86 + p73;
                    d73 = d86;
                }
                if (v73 > v59 + p73) {
                    v73 = v59 + p73;
                    d73 = d59;
                }
            }
            if (rc.onTheMap(l95)) {
                p95 = turnsToMove(l95);
                if (v95 > v83 + p95) {
                    v95 = v83 + p95;
                    d95 = d83;
                }
                if (v95 > v96 + p95) {
                    v95 = v96 + p95;
                    d95 = d96;
                }
                if (v95 > v82 + p95) {
                    v95 = v82 + p95;
                    d95 = d82;
                }
            }
            if (rc.onTheMap(l99)) {
                p99 = turnsToMove(l99);
                if (v99 > v85 + p99) {
                    v99 = v85 + p99;
                    d99 = d85;
                }
                if (v99 > v98 + p99) {
                    v99 = v98 + p99;
                    d99 = d98;
                }
                if (v99 > v86 + p99) {
                    v99 = v86 + p99;
                    d99 = d86;
                }
            }
            if (rc.onTheMap(l109)) {
                p109 = turnsToMove(l109);
                if (v109 > v97 + p109) {
                    v109 = v97 + p109;
                    d109 = d97;
                }
                if (v109 > v96 + p109) {
                    v109 = v96 + p109;
                    d109 = d96;
                }
                if (v109 > v110 + p109) {
                    v109 = v110 + p109;
                    d109 = d110;
                }
                if (v109 > v95 + p109) {
                    v109 = v95 + p109;
                    d109 = d95;
                }
            }
            if (rc.onTheMap(l111)) {
                p111 = turnsToMove(l111);
                if (v111 > v97 + p111) {
                    v111 = v97 + p111;
                    d111 = d97;
                }
                if (v111 > v98 + p111) {
                    v111 = v98 + p111;
                    d111 = d98;
                }
                if (v111 > v110 + p111) {
                    v111 = v110 + p111;
                    d111 = d110;
                }
                if (v111 > v99 + p111) {
                    v111 = v99 + p111;
                    d111 = d99;
                }
            }
            if (rc.onTheMap(l56)) {
                p56 = turnsToMove(l56);
                if (v56 > v70 + p56) {
                    v56 = v70 + p56;
                    d56 = d70;
                }
                if (v56 > v57 + p56) {
                    v56 = v57 + p56;
                    d56 = d57;
                }
                if (v56 > v69 + p56) {
                    v56 = v69 + p56;
                    d56 = d69;
                }
            }
            if (rc.onTheMap(l60)) {
                p60 = turnsToMove(l60);
                if (v60 > v72 + p60) {
                    v60 = v72 + p60;
                    d60 = d72;
                }
                if (v60 > v59 + p60) {
                    v60 = v59 + p60;
                    d60 = d59;
                }
                if (v60 > v73 + p60) {
                    v60 = v73 + p60;
                    d60 = d73;
                }
            }
            if (rc.onTheMap(l108)) {
                p108 = turnsToMove(l108);
                if (v108 > v96 + p108) {
                    v108 = v96 + p108;
                    d108 = d96;
                }
                if (v108 > v109 + p108) {
                    v108 = v109 + p108;
                    d108 = d109;
                }
                if (v108 > v95 + p108) {
                    v108 = v95 + p108;
                    d108 = d95;
                }
            }
            if (rc.onTheMap(l112)) {
                p112 = turnsToMove(l112);
                if (v112 > v98 + p112) {
                    v112 = v98 + p112;
                    d112 = d98;
                }
                if (v112 > v99 + p112) {
                    v112 = v99 + p112;
                    d112 = d99;
                }
                if (v112 > v111 + p112) {
                    v112 = v111 + p112;
                    d112 = d111;
                }
            }
            if (rc.onTheMap(l45)) {
                p45 = turnsToMove(l45);
                if (v45 > v58 + p45) {
                    v45 = v58 + p45;
                    d45 = d58;
                }
                if (v45 > v57 + p45) {
                    v45 = v57 + p45;
                    d45 = d57;
                }
                if (v45 > v59 + p45) {
                    v45 = v59 + p45;
                    d45 = d59;
                }
            }
            if (rc.onTheMap(l81)) {
                p81 = turnsToMove(l81);
                if (v81 > v82 + p81) {
                    v81 = v82 + p81;
                    d81 = d82;
                }
                if (v81 > v69 + p81) {
                    v81 = v69 + p81;
                    d81 = d69;
                }
                if (v81 > v95 + p81) {
                    v81 = v95 + p81;
                    d81 = d95;
                }
            }
            if (rc.onTheMap(l87)) {
                p87 = turnsToMove(l87);
                if (v87 > v86 + p87) {
                    v87 = v86 + p87;
                    d87 = d86;
                }
                if (v87 > v73 + p87) {
                    v87 = v73 + p87;
                    d87 = d73;
                }
                if (v87 > v99 + p87) {
                    v87 = v99 + p87;
                    d87 = d99;
                }
            }
            if (rc.onTheMap(l123)) {
                p123 = turnsToMove(l123);
                if (v123 > v110 + p123) {
                    v123 = v110 + p123;
                    d123 = d110;
                }
                if (v123 > v111 + p123) {
                    v123 = v111 + p123;
                    d123 = d111;
                }
                if (v123 > v109 + p123) {
                    v123 = v109 + p123;
                    d123 = d109;
                }
            }
            if (rc.onTheMap(l44)) {
                p44 = turnsToMove(l44);
                if (v44 > v58 + p44) {
                    v44 = v58 + p44;
                    d44 = d58;
                }
                if (v44 > v57 + p44) {
                    v44 = v57 + p44;
                    d44 = d57;
                }
                if (v44 > v56 + p44) {
                    v44 = v56 + p44;
                    d44 = d56;
                }
                if (v44 > v45 + p44) {
                    v44 = v45 + p44;
                    d44 = d45;
                }
            }
            if (rc.onTheMap(l46)) {
                p46 = turnsToMove(l46);
                if (v46 > v58 + p46) {
                    v46 = v58 + p46;
                    d46 = d58;
                }
                if (v46 > v59 + p46) {
                    v46 = v59 + p46;
                    d46 = d59;
                }
                if (v46 > v60 + p46) {
                    v46 = v60 + p46;
                    d46 = d60;
                }
                if (v46 > v45 + p46) {
                    v46 = v45 + p46;
                    d46 = d45;
                }
            }
            if (rc.onTheMap(l68)) {
                p68 = turnsToMove(l68);
                if (v68 > v82 + p68) {
                    v68 = v82 + p68;
                    d68 = d82;
                }
                if (v68 > v69 + p68) {
                    v68 = v69 + p68;
                    d68 = d69;
                }
                if (v68 > v56 + p68) {
                    v68 = v56 + p68;
                    d68 = d56;
                }
                if (v68 > v81 + p68) {
                    v68 = v81 + p68;
                    d68 = d81;
                }
            }
            if (rc.onTheMap(l74)) {
                p74 = turnsToMove(l74);
                if (v74 > v86 + p74) {
                    v74 = v86 + p74;
                    d74 = d86;
                }
                if (v74 > v73 + p74) {
                    v74 = v73 + p74;
                    d74 = d73;
                }
                if (v74 > v60 + p74) {
                    v74 = v60 + p74;
                    d74 = d60;
                }
                if (v74 > v87 + p74) {
                    v74 = v87 + p74;
                    d74 = d87;
                }
            }
            if (rc.onTheMap(l94)) {
                p94 = turnsToMove(l94);
                if (v94 > v82 + p94) {
                    v94 = v82 + p94;
                    d94 = d82;
                }
                if (v94 > v95 + p94) {
                    v94 = v95 + p94;
                    d94 = d95;
                }
                if (v94 > v108 + p94) {
                    v94 = v108 + p94;
                    d94 = d108;
                }
                if (v94 > v81 + p94) {
                    v94 = v81 + p94;
                    d94 = d81;
                }
            }
            if (rc.onTheMap(l100)) {
                p100 = turnsToMove(l100);
                if (v100 > v86 + p100) {
                    v100 = v86 + p100;
                    d100 = d86;
                }
                if (v100 > v99 + p100) {
                    v100 = v99 + p100;
                    d100 = d99;
                }
                if (v100 > v112 + p100) {
                    v100 = v112 + p100;
                    d100 = d112;
                }
                if (v100 > v87 + p100) {
                    v100 = v87 + p100;
                    d100 = d87;
                }
            }
            if (rc.onTheMap(l122)) {
                p122 = turnsToMove(l122);
                if (v122 > v110 + p122) {
                    v122 = v110 + p122;
                    d122 = d110;
                }
                if (v122 > v109 + p122) {
                    v122 = v109 + p122;
                    d122 = d109;
                }
                if (v122 > v108 + p122) {
                    v122 = v108 + p122;
                    d122 = d108;
                }
                if (v122 > v123 + p122) {
                    v122 = v123 + p122;
                    d122 = d123;
                }
            }
            if (rc.onTheMap(l124)) {
                p124 = turnsToMove(l124);
                if (v124 > v110 + p124) {
                    v124 = v110 + p124;
                    d124 = d110;
                }
                if (v124 > v111 + p124) {
                    v124 = v111 + p124;
                    d124 = d111;
                }
                if (v124 > v112 + p124) {
                    v124 = v112 + p124;
                    d124 = d112;
                }
                if (v124 > v123 + p124) {
                    v124 = v123 + p124;
                    d124 = d123;
                }
            }
            if (rc.onTheMap(l43)) {
                p43 = turnsToMove(l43);
                if (v43 > v57 + p43) {
                    v43 = v57 + p43;
                    d43 = d57;
                }
                if (v43 > v56 + p43) {
                    v43 = v56 + p43;
                    d43 = d56;
                }
                if (v43 > v44 + p43) {
                    v43 = v44 + p43;
                    d43 = d44;
                }
            }
            if (rc.onTheMap(l47)) {
                p47 = turnsToMove(l47);
                if (v47 > v59 + p47) {
                    v47 = v59 + p47;
                    d47 = d59;
                }
                if (v47 > v60 + p47) {
                    v47 = v60 + p47;
                    d47 = d60;
                }
                if (v47 > v46 + p47) {
                    v47 = v46 + p47;
                    d47 = d46;
                }
            }
            if (rc.onTheMap(l55)) {
                p55 = turnsToMove(l55);
                if (v55 > v69 + p55) {
                    v55 = v69 + p55;
                    d55 = d69;
                }
                if (v55 > v56 + p55) {
                    v55 = v56 + p55;
                    d55 = d56;
                }
                if (v55 > v68 + p55) {
                    v55 = v68 + p55;
                    d55 = d68;
                }
                if (v55 > v43 + p55) {
                    v55 = v43 + p55;
                    d55 = d43;
                }
            }
            if (rc.onTheMap(l61)) {
                p61 = turnsToMove(l61);
                if (v61 > v73 + p61) {
                    v61 = v73 + p61;
                    d61 = d73;
                }
                if (v61 > v60 + p61) {
                    v61 = v60 + p61;
                    d61 = d60;
                }
                if (v61 > v74 + p61) {
                    v61 = v74 + p61;
                    d61 = d74;
                }
                if (v61 > v47 + p61) {
                    v61 = v47 + p61;
                    d61 = d47;
                }
            }
            if (rc.onTheMap(l107)) {
                p107 = turnsToMove(l107);
                if (v107 > v95 + p107) {
                    v107 = v95 + p107;
                    d107 = d95;
                }
                if (v107 > v108 + p107) {
                    v107 = v108 + p107;
                    d107 = d108;
                }
                if (v107 > v94 + p107) {
                    v107 = v94 + p107;
                    d107 = d94;
                }
            }
            if (rc.onTheMap(l113)) {
                p113 = turnsToMove(l113);
                if (v113 > v99 + p113) {
                    v113 = v99 + p113;
                    d113 = d99;
                }
                if (v113 > v112 + p113) {
                    v113 = v112 + p113;
                    d113 = d112;
                }
                if (v113 > v100 + p113) {
                    v113 = v100 + p113;
                    d113 = d100;
                }
            }
            if (rc.onTheMap(l121)) {
                p121 = turnsToMove(l121);
                if (v121 > v109 + p121) {
                    v121 = v109 + p121;
                    d121 = d109;
                }
                if (v121 > v108 + p121) {
                    v121 = v108 + p121;
                    d121 = d108;
                }
                if (v121 > v122 + p121) {
                    v121 = v122 + p121;
                    d121 = d122;
                }
                if (v121 > v107 + p121) {
                    v121 = v107 + p121;
                    d121 = d107;
                }
            }
            if (rc.onTheMap(l125)) {
                p125 = turnsToMove(l125);
                if (v125 > v111 + p125) {
                    v125 = v111 + p125;
                    d125 = d111;
                }
                if (v125 > v112 + p125) {
                    v125 = v112 + p125;
                    d125 = d112;
                }
                if (v125 > v124 + p125) {
                    v125 = v124 + p125;
                    d125 = d124;
                }
                if (v125 > v113 + p125) {
                    v125 = v113 + p125;
                    d125 = d113;
                }
            }
            if (rc.onTheMap(l32)) {
                p32 = turnsToMove(l32);
                if (v32 > v45 + p32) {
                    v32 = v45 + p32;
                    d32 = d45;
                }
                if (v32 > v44 + p32) {
                    v32 = v44 + p32;
                    d32 = d44;
                }
                if (v32 > v46 + p32) {
                    v32 = v46 + p32;
                    d32 = d46;
                }
            }
            if (rc.onTheMap(l80)) {
                p80 = turnsToMove(l80);
                if (v80 > v81 + p80) {
                    v80 = v81 + p80;
                    d80 = d81;
                }
                if (v80 > v68 + p80) {
                    v80 = v68 + p80;
                    d80 = d68;
                }
                if (v80 > v94 + p80) {
                    v80 = v94 + p80;
                    d80 = d94;
                }
            }
            if (rc.onTheMap(l88)) {
                p88 = turnsToMove(l88);
                if (v88 > v87 + p88) {
                    v88 = v87 + p88;
                    d88 = d87;
                }
                if (v88 > v74 + p88) {
                    v88 = v74 + p88;
                    d88 = d74;
                }
                if (v88 > v100 + p88) {
                    v88 = v100 + p88;
                    d88 = d100;
                }
            }
            if (rc.onTheMap(l136)) {
                p136 = turnsToMove(l136);
                if (v136 > v123 + p136) {
                    v136 = v123 + p136;
                    d136 = d123;
                }
                if (v136 > v124 + p136) {
                    v136 = v124 + p136;
                    d136 = d124;
                }
                if (v136 > v122 + p136) {
                    v136 = v122 + p136;
                    d136 = d122;
                }
            }
            if (rc.onTheMap(l31)) {
                p31 = turnsToMove(l31);
                if (v31 > v45 + p31) {
                    v31 = v45 + p31;
                    d31 = d45;
                }
                if (v31 > v44 + p31) {
                    v31 = v44 + p31;
                    d31 = d44;
                }
                if (v31 > v43 + p31) {
                    v31 = v43 + p31;
                    d31 = d43;
                }
                if (v31 > v32 + p31) {
                    v31 = v32 + p31;
                    d31 = d32;
                }
            }
            if (rc.onTheMap(l33)) {
                p33 = turnsToMove(l33);
                if (v33 > v45 + p33) {
                    v33 = v45 + p33;
                    d33 = d45;
                }
                if (v33 > v46 + p33) {
                    v33 = v46 + p33;
                    d33 = d46;
                }
                if (v33 > v47 + p33) {
                    v33 = v47 + p33;
                    d33 = d47;
                }
                if (v33 > v32 + p33) {
                    v33 = v32 + p33;
                    d33 = d32;
                }
            }
            if (rc.onTheMap(l67)) {
                p67 = turnsToMove(l67);
                if (v67 > v81 + p67) {
                    v67 = v81 + p67;
                    d67 = d81;
                }
                if (v67 > v68 + p67) {
                    v67 = v68 + p67;
                    d67 = d68;
                }
                if (v67 > v55 + p67) {
                    v67 = v55 + p67;
                    d67 = d55;
                }
                if (v67 > v80 + p67) {
                    v67 = v80 + p67;
                    d67 = d80;
                }
            }
            if (rc.onTheMap(l75)) {
                p75 = turnsToMove(l75);
                if (v75 > v87 + p75) {
                    v75 = v87 + p75;
                    d75 = d87;
                }
                if (v75 > v74 + p75) {
                    v75 = v74 + p75;
                    d75 = d74;
                }
                if (v75 > v61 + p75) {
                    v75 = v61 + p75;
                    d75 = d61;
                }
                if (v75 > v88 + p75) {
                    v75 = v88 + p75;
                    d75 = d88;
                }
            }
            if (rc.onTheMap(l93)) {
                p93 = turnsToMove(l93);
                if (v93 > v81 + p93) {
                    v93 = v81 + p93;
                    d93 = d81;
                }
                if (v93 > v94 + p93) {
                    v93 = v94 + p93;
                    d93 = d94;
                }
                if (v93 > v107 + p93) {
                    v93 = v107 + p93;
                    d93 = d107;
                }
                if (v93 > v80 + p93) {
                    v93 = v80 + p93;
                    d93 = d80;
                }
            }
            if (rc.onTheMap(l101)) {
                p101 = turnsToMove(l101);
                if (v101 > v87 + p101) {
                    v101 = v87 + p101;
                    d101 = d87;
                }
                if (v101 > v100 + p101) {
                    v101 = v100 + p101;
                    d101 = d100;
                }
                if (v101 > v113 + p101) {
                    v101 = v113 + p101;
                    d101 = d113;
                }
                if (v101 > v88 + p101) {
                    v101 = v88 + p101;
                    d101 = d88;
                }
            }
            if (rc.onTheMap(l135)) {
                p135 = turnsToMove(l135);
                if (v135 > v123 + p135) {
                    v135 = v123 + p135;
                    d135 = d123;
                }
                if (v135 > v122 + p135) {
                    v135 = v122 + p135;
                    d135 = d122;
                }
                if (v135 > v121 + p135) {
                    v135 = v121 + p135;
                    d135 = d121;
                }
                if (v135 > v136 + p135) {
                    v135 = v136 + p135;
                    d135 = d136;
                }
            }
            if (rc.onTheMap(l137)) {
                p137 = turnsToMove(l137);
                if (v137 > v123 + p137) {
                    v137 = v123 + p137;
                    d137 = d123;
                }
                if (v137 > v124 + p137) {
                    v137 = v124 + p137;
                    d137 = d124;
                }
                if (v137 > v125 + p137) {
                    v137 = v125 + p137;
                    d137 = d125;
                }
                if (v137 > v136 + p137) {
                    v137 = v136 + p137;
                    d137 = d136;
                }
            }
            if (rc.onTheMap(l42)) {
                p42 = turnsToMove(l42);
                if (v42 > v56 + p42) {
                    v42 = v56 + p42;
                    d42 = d56;
                }
                if (v42 > v43 + p42) {
                    v42 = v43 + p42;
                    d42 = d43;
                }
                if (v42 > v55 + p42) {
                    v42 = v55 + p42;
                    d42 = d55;
                }
            }
            if (rc.onTheMap(l48)) {
                p48 = turnsToMove(l48);
                if (v48 > v60 + p48) {
                    v48 = v60 + p48;
                    d48 = d60;
                }
                if (v48 > v47 + p48) {
                    v48 = v47 + p48;
                    d48 = d47;
                }
                if (v48 > v61 + p48) {
                    v48 = v61 + p48;
                    d48 = d61;
                }
            }
            if (rc.onTheMap(l120)) {
                p120 = turnsToMove(l120);
                if (v120 > v108 + p120) {
                    v120 = v108 + p120;
                    d120 = d108;
                }
                if (v120 > v121 + p120) {
                    v120 = v121 + p120;
                    d120 = d121;
                }
                if (v120 > v107 + p120) {
                    v120 = v107 + p120;
                    d120 = d107;
                }
            }
            if (rc.onTheMap(l126)) {
                p126 = turnsToMove(l126);
                if (v126 > v112 + p126) {
                    v126 = v112 + p126;
                    d126 = d112;
                }
                if (v126 > v113 + p126) {
                    v126 = v113 + p126;
                    d126 = d113;
                }
                if (v126 > v125 + p126) {
                    v126 = v125 + p126;
                    d126 = d125;
                }
            }
            if (rc.onTheMap(l30)) {
                p30 = turnsToMove(l30);
                if (v30 > v44 + p30) {
                    v30 = v44 + p30;
                    d30 = d44;
                }
                if (v30 > v43 + p30) {
                    v30 = v43 + p30;
                    d30 = d43;
                }
                if (v30 > v31 + p30) {
                    v30 = v31 + p30;
                    d30 = d31;
                }
                if (v30 > v42 + p30) {
                    v30 = v42 + p30;
                    d30 = d42;
                }
            }
            if (rc.onTheMap(l34)) {
                p34 = turnsToMove(l34);
                if (v34 > v46 + p34) {
                    v34 = v46 + p34;
                    d34 = d46;
                }
                if (v34 > v47 + p34) {
                    v34 = v47 + p34;
                    d34 = d47;
                }
                if (v34 > v33 + p34) {
                    v34 = v33 + p34;
                    d34 = d33;
                }
                if (v34 > v48 + p34) {
                    v34 = v48 + p34;
                    d34 = d48;
                }
            }
            if (rc.onTheMap(l54)) {
                p54 = turnsToMove(l54);
                if (v54 > v68 + p54) {
                    v54 = v68 + p54;
                    d54 = d68;
                }
                if (v54 > v55 + p54) {
                    v54 = v55 + p54;
                    d54 = d55;
                }
                if (v54 > v67 + p54) {
                    v54 = v67 + p54;
                    d54 = d67;
                }
                if (v54 > v42 + p54) {
                    v54 = v42 + p54;
                    d54 = d42;
                }
            }
            if (rc.onTheMap(l62)) {
                p62 = turnsToMove(l62);
                if (v62 > v74 + p62) {
                    v62 = v74 + p62;
                    d62 = d74;
                }
                if (v62 > v61 + p62) {
                    v62 = v61 + p62;
                    d62 = d61;
                }
                if (v62 > v75 + p62) {
                    v62 = v75 + p62;
                    d62 = d75;
                }
                if (v62 > v48 + p62) {
                    v62 = v48 + p62;
                    d62 = d48;
                }
            }
            if (rc.onTheMap(l106)) {
                p106 = turnsToMove(l106);
                if (v106 > v94 + p106) {
                    v106 = v94 + p106;
                    d106 = d94;
                }
                if (v106 > v107 + p106) {
                    v106 = v107 + p106;
                    d106 = d107;
                }
                if (v106 > v93 + p106) {
                    v106 = v93 + p106;
                    d106 = d93;
                }
                if (v106 > v120 + p106) {
                    v106 = v120 + p106;
                    d106 = d120;
                }
            }
            if (rc.onTheMap(l114)) {
                p114 = turnsToMove(l114);
                if (v114 > v100 + p114) {
                    v114 = v100 + p114;
                    d114 = d100;
                }
                if (v114 > v113 + p114) {
                    v114 = v113 + p114;
                    d114 = d113;
                }
                if (v114 > v101 + p114) {
                    v114 = v101 + p114;
                    d114 = d101;
                }
                if (v114 > v126 + p114) {
                    v114 = v126 + p114;
                    d114 = d126;
                }
            }
            if (rc.onTheMap(l134)) {
                p134 = turnsToMove(l134);
                if (v134 > v122 + p134) {
                    v134 = v122 + p134;
                    d134 = d122;
                }
                if (v134 > v121 + p134) {
                    v134 = v121 + p134;
                    d134 = d121;
                }
                if (v134 > v135 + p134) {
                    v134 = v135 + p134;
                    d134 = d135;
                }
                if (v134 > v120 + p134) {
                    v134 = v120 + p134;
                    d134 = d120;
                }
            }
            if (rc.onTheMap(l138)) {
                p138 = turnsToMove(l138);
                if (v138 > v124 + p138) {
                    v138 = v124 + p138;
                    d138 = d124;
                }
                if (v138 > v125 + p138) {
                    v138 = v125 + p138;
                    d138 = d125;
                }
                if (v138 > v137 + p138) {
                    v138 = v137 + p138;
                    d138 = d137;
                }
                if (v138 > v126 + p138) {
                    v138 = v126 + p138;
                    d138 = d126;
                }
            }
            int dx = target.x - l84.x;
            int dy = target.y - l84.y;
            switch(dx){
                case -4:
                    switch(dy){
                        case -2: return d30;
                        case -1: return d31;
                        case  0: return d32;
                        case  1: return d33;
                        case  2: return d34;
                    }break;
                case -3:
                    switch(dy){
                        case -3: return d42;
                        case -2: return d43;
                        case -1: return d44;
                        case  0: return d45;
                        case  1: return d46;
                        case  2: return d47;
                        case  3: return d48;
                    }break;
                case -2:
                    switch(dy){
                        case -4: return d54;
                        case -3: return d55;
                        case -2: return d56;
                        case -1: return d57;
                        case  0: return d58;
                        case  1: return d59;
                        case  2: return d60;
                        case  3: return d61;
                        case  4: return d62;
                    }break;
                case -1:
                    switch(dy){
                        case -4: return d67;
                        case -3: return d68;
                        case -2: return d69;
                        case -1: return d70;
                        case  0: return d71;
                        case  1: return d72;
                        case  2: return d73;
                        case  3: return d74;
                        case  4: return d75;
                    }break;
                case 0:
                    switch(dy){
                        case -4: return d80;
                        case -3: return d81;
                        case -2: return d82;
                        case -1: return d83;
                        case  0: return d84;
                        case  1: return d85;
                        case  2: return d86;
                        case  3: return d87;
                        case  4: return d88;
                    }break;
                case 1:
                    switch(dy){
                        case -4: return d93;
                        case -3: return d94;
                        case -2: return d95;
                        case -1: return d96;
                        case  0: return d97;
                        case  1: return d98;
                        case  2: return d99;
                        case  3: return d100;
                        case  4: return d101;
                    }break;
                case 2:
                    switch(dy){
                        case -4: return d106;
                        case -3: return d107;
                        case -2: return d108;
                        case -1: return d109;
                        case  0: return d110;
                        case  1: return d111;
                        case  2: return d112;
                        case  3: return d113;
                        case  4: return d114;
                    }break;
                case 3:
                    switch(dy){
                        case -3: return d120;
                        case -2: return d121;
                        case -1: return d122;
                        case  0: return d123;
                        case  1: return d124;
                        case  2: return d125;
                        case  3: return d126;
                    }break;
                case 4:
                    switch(dy){
                        case -2: return d134;
                        case -1: return d135;
                        case  0: return d136;
                        case  1: return d137;
                        case  2: return d138;
                    }break;
            }
            Direction ans = null;
            //If bytecode is an issue, change ints to doubles
            double bestEstimation = 100000;
            dSQ84 = l84.distanceSquaredTo(target);

            dSQ30 = l30.distanceSquaredTo(target);
            dist30 = Math.sqrt(dSQ30) + v30;
            if(bestEstimation > dist30 && dSQ84 > dSQ30){
                bestEstimation = dist30;
                ans = d30;
            }
            dSQ31 = l31.distanceSquaredTo(target);
            dist31 = Math.sqrt(dSQ31) + v31;
            if(bestEstimation > dist31 && dSQ84 > dSQ31){
                bestEstimation = dist31;
                ans = d31;
            }
            dSQ32 = l32.distanceSquaredTo(target);
            dist32 = Math.sqrt(dSQ32) + v32;
            if(bestEstimation > dist32 && dSQ84 > dSQ32){
                bestEstimation = dist32;
                ans = d32;
            }
            dSQ33 = l33.distanceSquaredTo(target);
            dist33 = Math.sqrt(dSQ33) + v33;
            if(bestEstimation > dist33 && dSQ84 > dSQ33){
                bestEstimation = dist33;
                ans = d33;
            }
            dSQ34 = l34.distanceSquaredTo(target);
            dist34 = Math.sqrt(dSQ34) + v34;
            if(bestEstimation > dist34 && dSQ84 > dSQ34){
                bestEstimation = dist34;
                ans = d34;
            }
            dSQ42 = l42.distanceSquaredTo(target);
            dist42 = Math.sqrt(dSQ42) + v42;
            if(bestEstimation > dist42 && dSQ84 > dSQ42){
                bestEstimation = dist42;
                ans = d42;
            }
            dSQ48 = l48.distanceSquaredTo(target);
            dist48 = Math.sqrt(dSQ48) + v48;
            if(bestEstimation > dist48 && dSQ84 > dSQ48){
                bestEstimation = dist48;
                ans = d48;
            }
            dSQ54 = l54.distanceSquaredTo(target);
            dist54 = Math.sqrt(dSQ54) + v54;
            if(bestEstimation > dist54 && dSQ84 > dSQ54){
                bestEstimation = dist54;
                ans = d54;
            }
            dSQ62 = l62.distanceSquaredTo(target);
            dist62 = Math.sqrt(dSQ62) + v62;
            if(bestEstimation > dist62 && dSQ84 > dSQ62){
                bestEstimation = dist62;
                ans = d62;
            }
            dSQ67 = l67.distanceSquaredTo(target);
            dist67 = Math.sqrt(dSQ67) + v67;
            if(bestEstimation > dist67 && dSQ84 > dSQ67){
                bestEstimation = dist67;
                ans = d67;
            }
            dSQ75 = l75.distanceSquaredTo(target);
            dist75 = Math.sqrt(dSQ75) + v75;
            if(bestEstimation > dist75 && dSQ84 > dSQ75){
                bestEstimation = dist75;
                ans = d75;
            }
            dSQ80 = l80.distanceSquaredTo(target);
            dist80 = Math.sqrt(dSQ80) + v80;
            if(bestEstimation > dist80 && dSQ84 > dSQ80){
                bestEstimation = dist80;
                ans = d80;
            }
            dSQ88 = l88.distanceSquaredTo(target);
            dist88 = Math.sqrt(dSQ88) + v88;
            if(bestEstimation > dist88 && dSQ84 > dSQ88){
                bestEstimation = dist88;
                ans = d88;
            }
            dSQ93 = l93.distanceSquaredTo(target);
            dist93 = Math.sqrt(dSQ93) + v93;
            if(bestEstimation > dist93 && dSQ84 > dSQ93){
                bestEstimation = dist93;
                ans = d93;
            }
            dSQ101 = l101.distanceSquaredTo(target);
            dist101 = Math.sqrt(dSQ101) + v101;
            if(bestEstimation > dist101 && dSQ84 > dSQ101){
                bestEstimation = dist101;
                ans = d101;
            }
            dSQ106 = l106.distanceSquaredTo(target);
            dist106 = Math.sqrt(dSQ106) + v106;
            if(bestEstimation > dist106 && dSQ84 > dSQ106){
                bestEstimation = dist106;
                ans = d106;
            }
            dSQ114 = l114.distanceSquaredTo(target);
            dist114 = Math.sqrt(dSQ114) + v114;
            if(bestEstimation > dist114 && dSQ84 > dSQ114){
                bestEstimation = dist114;
                ans = d114;
            }
            dSQ120 = l120.distanceSquaredTo(target);
            dist120 = Math.sqrt(dSQ120) + v120;
            if(bestEstimation > dist120 && dSQ84 > dSQ120){
                bestEstimation = dist120;
                ans = d120;
            }
            dSQ126 = l126.distanceSquaredTo(target);
            dist126 = Math.sqrt(dSQ126) + v126;
            if(bestEstimation > dist126 && dSQ84 > dSQ126){
                bestEstimation = dist126;
                ans = d126;
            }
            dSQ134 = l134.distanceSquaredTo(target);
            dist134 = Math.sqrt(dSQ134) + v134;
            if(bestEstimation > dist134 && dSQ84 > dSQ134){
                bestEstimation = dist134;
                ans = d134;
            }
            dSQ135 = l135.distanceSquaredTo(target);
            dist135 = Math.sqrt(dSQ135) + v135;
            if(bestEstimation > dist135 && dSQ84 > dSQ135){
                bestEstimation = dist135;
                ans = d135;
            }
            dSQ136 = l136.distanceSquaredTo(target);
            dist136 = Math.sqrt(dSQ136) + v136;
            if(bestEstimation > dist136 && dSQ84 > dSQ136){
                bestEstimation = dist136;
                ans = d136;
            }
            dSQ137 = l137.distanceSquaredTo(target);
            dist137 = Math.sqrt(dSQ137) + v137;
            if(bestEstimation > dist137 && dSQ84 > dSQ137){
                bestEstimation = dist137;
                ans = d137;
            }
            dSQ138 = l138.distanceSquaredTo(target);
            dist138 = Math.sqrt(dSQ138) + v138;
            if(bestEstimation > dist138 && dSQ84 > dSQ138){
                ans = d138;
            }
            return ans;

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //If too much bytecode, remove this method and directly put the switch statement into every if statement
    private int turnsToMove(MapLocation loc) throws GameActionException {
        switch(rc.senseRubble(loc)){
            case  0: case  1: case  2: case  3: return 2;
            case  4: case  5: case  6: case  7: case  8: case  9: return 3;
            case 10: case 11: case 12: case 13: case 14: case 15: return 4;
            case 16: case 17: case 18: case 19: case 20: case 21: return 5;
            case 22: case 23: case 24: case 25: case 26: case 27: case 28: return 6;
            case 29: case 30: case 31: case 32: case 33: case 34: return 7;
            case 35: case 36: case 37: case 38: case 39: case 40: return 8;
            case 41: case 42: case 43: case 44: case 45: case 46: return 9;
            case 47: case 48: case 49: case 50: case 51: case 52: case 53: return 10;
            case 54: case 55: case 56: case 57: case 58: case 59: return 11;
            case 60: case 61: case 62: case 63: case 64: case 65: return 12;
            case 66: case 67: case 68: case 69: case 70: case 71: return 13;
            case 72: case 73: case 74: case 75: case 76: case 77: case 78: return 14;
            case 79: case 80: case 81: case 82: case 83: case 84: return 15;
            case 85: case 86: case 87: case 88: case 89: case 90: return 16;
            case 91: case 92: case 93: case 94: case 95: case 96: return 17;
            default: return 18;
        }
    }

    public Direction getBestDirMiner(MapLocation target){
        l84  = rc.getLocation();                 v84 = 0;
        l85  = l84.add(Direction.NORTH);         v85 = 1000000;  d85 = null;
        l72  = l85.add(Direction.WEST);          v72 = 1000000;  d72 = null;
        l71  = l72.add(Direction.SOUTH);         v71 = 1000000;  d71 = null;
        l70  = l71.add(Direction.SOUTH);         v70 = 1000000;  d70 = null;
        l83  = l70.add(Direction.EAST);          v83 = 1000000;  d83 = null;
        l96  = l83.add(Direction.EAST);          v96 = 1000000;  d96 = null;
        l97  = l96.add(Direction.NORTH);         v97 = 1000000;  d97 = null;
        l98  = l97.add(Direction.NORTH);         v98 = 1000000;  d98 = null;
        l99  = l98.add(Direction.NORTH);         v99 = 1000000;  d99 = null;
        l86  = l99.add(Direction.WEST);          v86 = 1000000;  d86 = null;
        l73  = l86.add(Direction.WEST);          v73 = 1000000;  d73 = null;
        l60  = l73.add(Direction.WEST);          v60 = 1000000;  d60 = null;
        l59  = l60.add(Direction.SOUTH);         v59 = 1000000;  d59 = null;
        l58  = l59.add(Direction.SOUTH);         v58 = 1000000;  d58 = null;
        l57  = l58.add(Direction.SOUTH);         v57 = 1000000;  d57 = null;
        l56  = l57.add(Direction.SOUTH);         v56 = 1000000;  d56 = null;
        l69  = l56.add(Direction.EAST);          v69 = 1000000;  d69 = null;
        l82  = l69.add(Direction.EAST);          v82 = 1000000;  d82 = null;
        l95  = l82.add(Direction.EAST);          v95 = 1000000;  d95 = null;
        l108 = l95.add(Direction.EAST);         v108 = 1000000; d108 = null;
        l109 = l108.add(Direction.NORTH);       v109 = 1000000; d109 = null;
        l110 = l109.add(Direction.NORTH);       v110 = 1000000; d110 = null;
        l111 = l110.add(Direction.NORTH);       v111 = 1000000; d111 = null;
        l112 = l111.add(Direction.NORTH);       v112 = 1000000; d112 = null;
        l100 = l112.add(Direction.NORTHWEST);   v100 = 1000000; d100 = null;
        l87  = l100.add(Direction.WEST);         v87 = 1000000;  d87 = null;
        l74  = l87.add(Direction.WEST);          v74 = 1000000;  d74 = null;
        l61  = l74.add(Direction.WEST);          v61 = 1000000;  d61 = null;
        l47  = l61.add(Direction.SOUTHWEST);     v47 = 1000000;  d47 = null;
        l46  = l47.add(Direction.SOUTH);         v46 = 1000000;  d46 = null;
        l45  = l46.add(Direction.SOUTH);         v45 = 1000000;  d45 = null;
        l44  = l45.add(Direction.SOUTH);         v44 = 1000000;  d44 = null;
        l43  = l44.add(Direction.SOUTH);         v43 = 1000000;  d43 = null;
        l55  = l43.add(Direction.SOUTHEAST);     v55 = 1000000;  d55 = null;
        l68  = l55.add(Direction.EAST);          v68 = 1000000;  d68 = null;
        l81  = l68.add(Direction.EAST);          v81 = 1000000;  d81 = null;
        l94  = l81.add(Direction.EAST);          v94 = 1000000;  d94 = null;
        l107 = l94.add(Direction.EAST);         v107 = 1000000; d107 = null;
        l121 = l107.add(Direction.NORTHEAST);   v121 = 1000000; d121 = null;
        l122 = l121.add(Direction.NORTH);       v122 = 1000000; d122 = null;
        l123 = l122.add(Direction.NORTH);       v123 = 1000000; d123 = null;
        l124 = l123.add(Direction.NORTH);       v124 = 1000000; d124 = null;
        l125 = l124.add(Direction.NORTH);       v125 = 1000000; d125 = null;
        l113 = l125.add(Direction.NORTHWEST);   v113 = 1000000; d113 = null;
        l101 = l113.add(Direction.NORTHWEST);   v101 = 1000000; d101 = null;
        l88  = l101.add(Direction.WEST);         v88 = 1000000;  d88 = null;
        l75  = l88.add(Direction.WEST);          v75 = 1000000;  d75 = null;
        l62  = l75.add(Direction.WEST);          v62 = 1000000;  d62 = null;
        l48  = l62.add(Direction.SOUTHWEST);     v48 = 1000000;  d48 = null;
        l34  = l48.add(Direction.SOUTHWEST);     v34 = 1000000;  d34 = null;
        l33  = l34.add(Direction.SOUTH);         v33 = 1000000;  d33 = null;
        l32  = l33.add(Direction.SOUTH);         v32 = 1000000;  d32 = null;
        l31  = l32.add(Direction.SOUTH);         v31 = 1000000;  d31 = null;
        l30  = l31.add(Direction.SOUTH);         v30 = 1000000;  d30 = null;
        l42  = l30.add(Direction.SOUTHEAST);     v42 = 1000000;  d42 = null;
        l54  = l42.add(Direction.SOUTHEAST);     v54 = 1000000;  d54 = null;
        l67  = l54.add(Direction.EAST);          v67 = 1000000;  d67 = null;
        l80  = l67.add(Direction.EAST);          v80 = 1000000;  d80 = null;
        l93  = l80.add(Direction.EAST);          v93 = 1000000;  d93 = null;
        l106 = l93.add(Direction.EAST);         v106 = 1000000; d106 = null;
        l120 = l106.add(Direction.NORTHEAST);   v120 = 1000000; d120 = null;
        l134 = l120.add(Direction.NORTHEAST);   v134 = 1000000; d134 = null;
        l135 = l134.add(Direction.NORTH);       v135 = 1000000; d135 = null;
        l136 = l135.add(Direction.NORTH);       v136 = 1000000; d136 = null;
        l137 = l136.add(Direction.NORTH);       v137 = 1000000; d137 = null;
        l138 = l137.add(Direction.NORTH);       v138 = 1000000; d138 = null;
        l126 = l138.add(Direction.NORTHWEST);   v126 = 1000000; d126 = null;
        l114 = l126.add(Direction.NORTHWEST);   v114 = 1000000; d114 = null;

        //Maybe Remove the try/catch statement
        try{
            if(rc.onTheMap(l71)){
                if(!rc.isLocationOccupied(l71)){
                    p71 = turnsToMoveMiner(l71);
                    if(v71 > v84 + p71){
                        v71 = v84 + p71;
                        d71 = Direction.WEST;
                    }
                }
            }
            if (rc.onTheMap(l83)) {
                if (!rc.isLocationOccupied(l83)) {
                    p83 = turnsToMoveMiner(l83);
                    if (v83 > v84 + p83) {
                        v83 = v84 + p83;
                        d83 = Direction.SOUTH;
                    }
                    if (v83 > v71 + p83) {
                        v83 = v71 + p83;
                        d83 = d71;
                    }
                }
            }
            if (rc.onTheMap(l85)) {
                if (!rc.isLocationOccupied(l85)) {
                    p85 = turnsToMoveMiner(l85);
                    if (v85 > v84 + p85) {
                        v85 = v84 + p85;
                        d85 = Direction.NORTH;
                    }
                    if (v85 > v71 + p85) {
                        v85 = v71 + p85;
                        d85 = d71;
                    }
                }
            }
            if (rc.onTheMap(l97)) {
                if (!rc.isLocationOccupied(l97)) {
                    p97 = turnsToMoveMiner(l97);
                    if (v97 > v84 + p97) {
                        v97 = v84 + p97;
                        d97 = Direction.EAST;
                    }
                    if (v97 > v85 + p97) {
                        v97 = v85 + p97;
                        d97 = d85;
                    }
                    if (v97 > v83 + p97) {
                        v97 = v83 + p97;
                        d97 = d83;
                    }
                }
            }
            if (rc.onTheMap(l70)) {
                if (!rc.isLocationOccupied(l70)) {
                    p70 = turnsToMoveMiner(l70);
                    if (v70 > v84 + p70) {
                        v70 = v84 + p70;
                        d70 = Direction.SOUTHWEST;
                    }
                    if (v70 > v71 + p70) {
                        v70 = v71 + p70;
                        d70 = d71;
                    }
                    if (v70 > v83 + p70) {
                        v70 = v83 + p70;
                        d70 = d83;
                    }
                }
            }
            if (rc.onTheMap(l72)) {
                if (!rc.isLocationOccupied(l72)) {
                    p72 = turnsToMoveMiner(l72);
                    if (v72 > v84 + p72) {
                        v72 = v84 + p72;
                        d72 = Direction.NORTHWEST;
                    }
                    if (v72 > v71 + p72) {
                        v72 = v71 + p72;
                        d72 = d71;
                    }
                    if (v72 > v85 + p72) {
                        v72 = v85 + p72;
                        d72 = d85;
                    }
                }
            }
            if (rc.onTheMap(l96)) {
                if (!rc.isLocationOccupied(l96)) {
                    p96 = turnsToMoveMiner(l96);
                    if (v96 > v84 + p96) {
                        v96 = v84 + p96;
                        d96 = Direction.SOUTHEAST;
                    }
                    if (v96 > v97 + p96) {
                        v96 = v97 + p96;
                        d96 = d97;
                    }
                    if (v96 > v83 + p96) {
                        v96 = v83 + p96;
                        d96 = d83;
                    }
                }
            }
            if (rc.onTheMap(l98)) {
                if (!rc.isLocationOccupied(l98)) {
                    p98 = turnsToMoveMiner(l98);
                    if (v98 > v84 + p98) {
                        v98 = v84 + p98;
                        d98 = Direction.NORTHEAST;
                    }
                    if (v98 > v85 + p98) {
                        v98 = v85 + p98;
                        d98 = d85;
                    }
                    if (v98 > v97 + p98) {
                        v98 = v97 + p98;
                        d98 = d97;
                    }
                }
            }
            if (rc.onTheMap(l58)) {
                p58 = turnsToMoveMiner(l58);
                if (v58 > v71 + p58) {
                    v58 = v71 + p58;
                    d58 = d71;
                }
                if (v58 > v70 + p58) {
                    v58 = v70 + p58;
                    d58 = d70;
                }
                if (v58 > v72 + p58) {
                    v58 = v72 + p58;
                    d58 = d72;
                }
            }
            if (rc.onTheMap(l82)) {
                p82 = turnsToMoveMiner(l82);
                if (v82 > v83 + p82) {
                    v82 = v83 + p82;
                    d82 = d83;
                }
                if (v82 > v70 + p82) {
                    v82 = v70 + p82;
                    d82 = d70;
                }
                if (v82 > v96 + p82) {
                    v82 = v96 + p82;
                    d82 = d96;
                }
            }
            if (rc.onTheMap(l86)) {
                p86 = turnsToMoveMiner(l86);
                if (v86 > v85 + p86) {
                    v86 = v85 + p86;
                    d86 = d85;
                }
                if (v86 > v72 + p86) {
                    v86 = v72 + p86;
                    d86 = d72;
                }
                if (v86 > v98 + p86) {
                    v86 = v98 + p86;
                    d86 = d98;
                }
            }
            if (rc.onTheMap(l110)) {
                p110 = turnsToMoveMiner(l110);
                if (v110 > v97 + p110) {
                    v110 = v97 + p110;
                    d110 = d97;
                }
                if (v110 > v98 + p110) {
                    v110 = v98 + p110;
                    d110 = d98;
                }
                if (v110 > v96 + p110) {
                    v110 = v96 + p110;
                    d110 = d96;
                }
            }
            if (rc.onTheMap(l57)) {
                p57 = turnsToMoveMiner(l57);
                if (v57 > v71 + p57) {
                    v57 = v71 + p57;
                    d57 = d71;
                }
                if (v57 > v70 + p57) {
                    v57 = v70 + p57;
                    d57 = d70;
                }
                if (v57 > v58 + p57) {
                    v57 = v58 + p57;
                    d57 = d58;
                }
            }
            if (rc.onTheMap(l59)) {
                p59 = turnsToMoveMiner(l59);
                if (v59 > v71 + p59) {
                    v59 = v71 + p59;
                    d59 = d71;
                }
                if (v59 > v72 + p59) {
                    v59 = v72 + p59;
                    d59 = d72;
                }
                if (v59 > v58 + p59) {
                    v59 = v58 + p59;
                    d59 = d58;
                }
            }
            if (rc.onTheMap(l69)) {
                p69 = turnsToMoveMiner(l69);
                if (v69 > v83 + p69) {
                    v69 = v83 + p69;
                    d69 = d83;
                }
                if (v69 > v70 + p69) {
                    v69 = v70 + p69;
                    d69 = d70;
                }
                if (v69 > v82 + p69) {
                    v69 = v82 + p69;
                    d69 = d82;
                }
                if (v69 > v57 + p69) {
                    v69 = v57 + p69;
                    d69 = d57;
                }
            }
            if (rc.onTheMap(l73)) {
                p73 = turnsToMoveMiner(l73);
                if (v73 > v85 + p73) {
                    v73 = v85 + p73;
                    d73 = d85;
                }
                if (v73 > v72 + p73) {
                    v73 = v72 + p73;
                    d73 = d72;
                }
                if (v73 > v86 + p73) {
                    v73 = v86 + p73;
                    d73 = d86;
                }
                if (v73 > v59 + p73) {
                    v73 = v59 + p73;
                    d73 = d59;
                }
            }
            if (rc.onTheMap(l95)) {
                p95 = turnsToMoveMiner(l95);
                if (v95 > v83 + p95) {
                    v95 = v83 + p95;
                    d95 = d83;
                }
                if (v95 > v96 + p95) {
                    v95 = v96 + p95;
                    d95 = d96;
                }
                if (v95 > v82 + p95) {
                    v95 = v82 + p95;
                    d95 = d82;
                }
            }
            if (rc.onTheMap(l99)) {
                p99 = turnsToMoveMiner(l99);
                if (v99 > v85 + p99) {
                    v99 = v85 + p99;
                    d99 = d85;
                }
                if (v99 > v98 + p99) {
                    v99 = v98 + p99;
                    d99 = d98;
                }
                if (v99 > v86 + p99) {
                    v99 = v86 + p99;
                    d99 = d86;
                }
            }
            if (rc.onTheMap(l109)) {
                p109 = turnsToMoveMiner(l109);
                if (v109 > v97 + p109) {
                    v109 = v97 + p109;
                    d109 = d97;
                }
                if (v109 > v96 + p109) {
                    v109 = v96 + p109;
                    d109 = d96;
                }
                if (v109 > v110 + p109) {
                    v109 = v110 + p109;
                    d109 = d110;
                }
                if (v109 > v95 + p109) {
                    v109 = v95 + p109;
                    d109 = d95;
                }
            }
            if (rc.onTheMap(l111)) {
                p111 = turnsToMoveMiner(l111);
                if (v111 > v97 + p111) {
                    v111 = v97 + p111;
                    d111 = d97;
                }
                if (v111 > v98 + p111) {
                    v111 = v98 + p111;
                    d111 = d98;
                }
                if (v111 > v110 + p111) {
                    v111 = v110 + p111;
                    d111 = d110;
                }
                if (v111 > v99 + p111) {
                    v111 = v99 + p111;
                    d111 = d99;
                }
            }
            if (rc.onTheMap(l56)) {
                p56 = turnsToMoveMiner(l56);
                if (v56 > v70 + p56) {
                    v56 = v70 + p56;
                    d56 = d70;
                }
                if (v56 > v57 + p56) {
                    v56 = v57 + p56;
                    d56 = d57;
                }
                if (v56 > v69 + p56) {
                    v56 = v69 + p56;
                    d56 = d69;
                }
            }
            if (rc.onTheMap(l60)) {
                p60 = turnsToMoveMiner(l60);
                if (v60 > v72 + p60) {
                    v60 = v72 + p60;
                    d60 = d72;
                }
                if (v60 > v59 + p60) {
                    v60 = v59 + p60;
                    d60 = d59;
                }
                if (v60 > v73 + p60) {
                    v60 = v73 + p60;
                    d60 = d73;
                }
            }
            if (rc.onTheMap(l108)) {
                p108 = turnsToMoveMiner(l108);
                if (v108 > v96 + p108) {
                    v108 = v96 + p108;
                    d108 = d96;
                }
                if (v108 > v109 + p108) {
                    v108 = v109 + p108;
                    d108 = d109;
                }
                if (v108 > v95 + p108) {
                    v108 = v95 + p108;
                    d108 = d95;
                }
            }
            if (rc.onTheMap(l112)) {
                p112 = turnsToMoveMiner(l112);
                if (v112 > v98 + p112) {
                    v112 = v98 + p112;
                    d112 = d98;
                }
                if (v112 > v99 + p112) {
                    v112 = v99 + p112;
                    d112 = d99;
                }
                if (v112 > v111 + p112) {
                    v112 = v111 + p112;
                    d112 = d111;
                }
            }
            if (rc.onTheMap(l45)) {
                p45 = turnsToMoveMiner(l45);
                if (v45 > v58 + p45) {
                    v45 = v58 + p45;
                    d45 = d58;
                }
                if (v45 > v57 + p45) {
                    v45 = v57 + p45;
                    d45 = d57;
                }
                if (v45 > v59 + p45) {
                    v45 = v59 + p45;
                    d45 = d59;
                }
            }
            if (rc.onTheMap(l81)) {
                p81 = turnsToMoveMiner(l81);
                if (v81 > v82 + p81) {
                    v81 = v82 + p81;
                    d81 = d82;
                }
                if (v81 > v69 + p81) {
                    v81 = v69 + p81;
                    d81 = d69;
                }
                if (v81 > v95 + p81) {
                    v81 = v95 + p81;
                    d81 = d95;
                }
            }
            if (rc.onTheMap(l87)) {
                p87 = turnsToMoveMiner(l87);
                if (v87 > v86 + p87) {
                    v87 = v86 + p87;
                    d87 = d86;
                }
                if (v87 > v73 + p87) {
                    v87 = v73 + p87;
                    d87 = d73;
                }
                if (v87 > v99 + p87) {
                    v87 = v99 + p87;
                    d87 = d99;
                }
            }
            if (rc.onTheMap(l123)) {
                p123 = turnsToMoveMiner(l123);
                if (v123 > v110 + p123) {
                    v123 = v110 + p123;
                    d123 = d110;
                }
                if (v123 > v111 + p123) {
                    v123 = v111 + p123;
                    d123 = d111;
                }
                if (v123 > v109 + p123) {
                    v123 = v109 + p123;
                    d123 = d109;
                }
            }
            if (rc.onTheMap(l44)) {
                p44 = turnsToMoveMiner(l44);
                if (v44 > v58 + p44) {
                    v44 = v58 + p44;
                    d44 = d58;
                }
                if (v44 > v57 + p44) {
                    v44 = v57 + p44;
                    d44 = d57;
                }
                if (v44 > v56 + p44) {
                    v44 = v56 + p44;
                    d44 = d56;
                }
                if (v44 > v45 + p44) {
                    v44 = v45 + p44;
                    d44 = d45;
                }
            }
            if (rc.onTheMap(l46)) {
                p46 = turnsToMoveMiner(l46);
                if (v46 > v58 + p46) {
                    v46 = v58 + p46;
                    d46 = d58;
                }
                if (v46 > v59 + p46) {
                    v46 = v59 + p46;
                    d46 = d59;
                }
                if (v46 > v60 + p46) {
                    v46 = v60 + p46;
                    d46 = d60;
                }
                if (v46 > v45 + p46) {
                    v46 = v45 + p46;
                    d46 = d45;
                }
            }
            if (rc.onTheMap(l68)) {
                p68 = turnsToMoveMiner(l68);
                if (v68 > v82 + p68) {
                    v68 = v82 + p68;
                    d68 = d82;
                }
                if (v68 > v69 + p68) {
                    v68 = v69 + p68;
                    d68 = d69;
                }
                if (v68 > v56 + p68) {
                    v68 = v56 + p68;
                    d68 = d56;
                }
                if (v68 > v81 + p68) {
                    v68 = v81 + p68;
                    d68 = d81;
                }
            }
            if (rc.onTheMap(l74)) {
                p74 = turnsToMoveMiner(l74);
                if (v74 > v86 + p74) {
                    v74 = v86 + p74;
                    d74 = d86;
                }
                if (v74 > v73 + p74) {
                    v74 = v73 + p74;
                    d74 = d73;
                }
                if (v74 > v60 + p74) {
                    v74 = v60 + p74;
                    d74 = d60;
                }
                if (v74 > v87 + p74) {
                    v74 = v87 + p74;
                    d74 = d87;
                }
            }
            if (rc.onTheMap(l94)) {
                p94 = turnsToMoveMiner(l94);
                if (v94 > v82 + p94) {
                    v94 = v82 + p94;
                    d94 = d82;
                }
                if (v94 > v95 + p94) {
                    v94 = v95 + p94;
                    d94 = d95;
                }
                if (v94 > v108 + p94) {
                    v94 = v108 + p94;
                    d94 = d108;
                }
                if (v94 > v81 + p94) {
                    v94 = v81 + p94;
                    d94 = d81;
                }
            }
            if (rc.onTheMap(l100)) {
                p100 = turnsToMoveMiner(l100);
                if (v100 > v86 + p100) {
                    v100 = v86 + p100;
                    d100 = d86;
                }
                if (v100 > v99 + p100) {
                    v100 = v99 + p100;
                    d100 = d99;
                }
                if (v100 > v112 + p100) {
                    v100 = v112 + p100;
                    d100 = d112;
                }
                if (v100 > v87 + p100) {
                    v100 = v87 + p100;
                    d100 = d87;
                }
            }
            if (rc.onTheMap(l122)) {
                p122 = turnsToMoveMiner(l122);
                if (v122 > v110 + p122) {
                    v122 = v110 + p122;
                    d122 = d110;
                }
                if (v122 > v109 + p122) {
                    v122 = v109 + p122;
                    d122 = d109;
                }
                if (v122 > v108 + p122) {
                    v122 = v108 + p122;
                    d122 = d108;
                }
                if (v122 > v123 + p122) {
                    v122 = v123 + p122;
                    d122 = d123;
                }
            }
            if (rc.onTheMap(l124)) {
                p124 = turnsToMoveMiner(l124);
                if (v124 > v110 + p124) {
                    v124 = v110 + p124;
                    d124 = d110;
                }
                if (v124 > v111 + p124) {
                    v124 = v111 + p124;
                    d124 = d111;
                }
                if (v124 > v112 + p124) {
                    v124 = v112 + p124;
                    d124 = d112;
                }
                if (v124 > v123 + p124) {
                    v124 = v123 + p124;
                    d124 = d123;
                }
            }
            if (rc.onTheMap(l43)) {
                p43 = turnsToMoveMiner(l43);
                if (v43 > v57 + p43) {
                    v43 = v57 + p43;
                    d43 = d57;
                }
                if (v43 > v56 + p43) {
                    v43 = v56 + p43;
                    d43 = d56;
                }
                if (v43 > v44 + p43) {
                    v43 = v44 + p43;
                    d43 = d44;
                }
            }
            if (rc.onTheMap(l47)) {
                p47 = turnsToMoveMiner(l47);
                if (v47 > v59 + p47) {
                    v47 = v59 + p47;
                    d47 = d59;
                }
                if (v47 > v60 + p47) {
                    v47 = v60 + p47;
                    d47 = d60;
                }
                if (v47 > v46 + p47) {
                    v47 = v46 + p47;
                    d47 = d46;
                }
            }
            if (rc.onTheMap(l55)) {
                p55 = turnsToMoveMiner(l55);
                if (v55 > v69 + p55) {
                    v55 = v69 + p55;
                    d55 = d69;
                }
                if (v55 > v56 + p55) {
                    v55 = v56 + p55;
                    d55 = d56;
                }
                if (v55 > v68 + p55) {
                    v55 = v68 + p55;
                    d55 = d68;
                }
                if (v55 > v43 + p55) {
                    v55 = v43 + p55;
                    d55 = d43;
                }
            }
            if (rc.onTheMap(l61)) {
                p61 = turnsToMoveMiner(l61);
                if (v61 > v73 + p61) {
                    v61 = v73 + p61;
                    d61 = d73;
                }
                if (v61 > v60 + p61) {
                    v61 = v60 + p61;
                    d61 = d60;
                }
                if (v61 > v74 + p61) {
                    v61 = v74 + p61;
                    d61 = d74;
                }
                if (v61 > v47 + p61) {
                    v61 = v47 + p61;
                    d61 = d47;
                }
            }
            if (rc.onTheMap(l107)) {
                p107 = turnsToMoveMiner(l107);
                if (v107 > v95 + p107) {
                    v107 = v95 + p107;
                    d107 = d95;
                }
                if (v107 > v108 + p107) {
                    v107 = v108 + p107;
                    d107 = d108;
                }
                if (v107 > v94 + p107) {
                    v107 = v94 + p107;
                    d107 = d94;
                }
            }
            if (rc.onTheMap(l113)) {
                p113 = turnsToMoveMiner(l113);
                if (v113 > v99 + p113) {
                    v113 = v99 + p113;
                    d113 = d99;
                }
                if (v113 > v112 + p113) {
                    v113 = v112 + p113;
                    d113 = d112;
                }
                if (v113 > v100 + p113) {
                    v113 = v100 + p113;
                    d113 = d100;
                }
            }
            if (rc.onTheMap(l121)) {
                p121 = turnsToMoveMiner(l121);
                if (v121 > v109 + p121) {
                    v121 = v109 + p121;
                    d121 = d109;
                }
                if (v121 > v108 + p121) {
                    v121 = v108 + p121;
                    d121 = d108;
                }
                if (v121 > v122 + p121) {
                    v121 = v122 + p121;
                    d121 = d122;
                }
                if (v121 > v107 + p121) {
                    v121 = v107 + p121;
                    d121 = d107;
                }
            }
            if (rc.onTheMap(l125)) {
                p125 = turnsToMoveMiner(l125);
                if (v125 > v111 + p125) {
                    v125 = v111 + p125;
                    d125 = d111;
                }
                if (v125 > v112 + p125) {
                    v125 = v112 + p125;
                    d125 = d112;
                }
                if (v125 > v124 + p125) {
                    v125 = v124 + p125;
                    d125 = d124;
                }
                if (v125 > v113 + p125) {
                    v125 = v113 + p125;
                    d125 = d113;
                }
            }
            if (rc.onTheMap(l32)) {
                p32 = turnsToMoveMiner(l32);
                if (v32 > v45 + p32) {
                    v32 = v45 + p32;
                    d32 = d45;
                }
                if (v32 > v44 + p32) {
                    v32 = v44 + p32;
                    d32 = d44;
                }
                if (v32 > v46 + p32) {
                    v32 = v46 + p32;
                    d32 = d46;
                }
            }
            if (rc.onTheMap(l80)) {
                p80 = turnsToMoveMiner(l80);
                if (v80 > v81 + p80) {
                    v80 = v81 + p80;
                    d80 = d81;
                }
                if (v80 > v68 + p80) {
                    v80 = v68 + p80;
                    d80 = d68;
                }
                if (v80 > v94 + p80) {
                    v80 = v94 + p80;
                    d80 = d94;
                }
            }
            if (rc.onTheMap(l88)) {
                p88 = turnsToMoveMiner(l88);
                if (v88 > v87 + p88) {
                    v88 = v87 + p88;
                    d88 = d87;
                }
                if (v88 > v74 + p88) {
                    v88 = v74 + p88;
                    d88 = d74;
                }
                if (v88 > v100 + p88) {
                    v88 = v100 + p88;
                    d88 = d100;
                }
            }
            if (rc.onTheMap(l136)) {
                p136 = turnsToMoveMiner(l136);
                if (v136 > v123 + p136) {
                    v136 = v123 + p136;
                    d136 = d123;
                }
                if (v136 > v124 + p136) {
                    v136 = v124 + p136;
                    d136 = d124;
                }
                if (v136 > v122 + p136) {
                    v136 = v122 + p136;
                    d136 = d122;
                }
            }
            if (rc.onTheMap(l31)) {
                p31 = turnsToMoveMiner(l31);
                if (v31 > v45 + p31) {
                    v31 = v45 + p31;
                    d31 = d45;
                }
                if (v31 > v44 + p31) {
                    v31 = v44 + p31;
                    d31 = d44;
                }
                if (v31 > v43 + p31) {
                    v31 = v43 + p31;
                    d31 = d43;
                }
                if (v31 > v32 + p31) {
                    v31 = v32 + p31;
                    d31 = d32;
                }
            }
            if (rc.onTheMap(l33)) {
                p33 = turnsToMoveMiner(l33);
                if (v33 > v45 + p33) {
                    v33 = v45 + p33;
                    d33 = d45;
                }
                if (v33 > v46 + p33) {
                    v33 = v46 + p33;
                    d33 = d46;
                }
                if (v33 > v47 + p33) {
                    v33 = v47 + p33;
                    d33 = d47;
                }
                if (v33 > v32 + p33) {
                    v33 = v32 + p33;
                    d33 = d32;
                }
            }
            if (rc.onTheMap(l67)) {
                p67 = turnsToMoveMiner(l67);
                if (v67 > v81 + p67) {
                    v67 = v81 + p67;
                    d67 = d81;
                }
                if (v67 > v68 + p67) {
                    v67 = v68 + p67;
                    d67 = d68;
                }
                if (v67 > v55 + p67) {
                    v67 = v55 + p67;
                    d67 = d55;
                }
                if (v67 > v80 + p67) {
                    v67 = v80 + p67;
                    d67 = d80;
                }
            }
            if (rc.onTheMap(l75)) {
                p75 = turnsToMoveMiner(l75);
                if (v75 > v87 + p75) {
                    v75 = v87 + p75;
                    d75 = d87;
                }
                if (v75 > v74 + p75) {
                    v75 = v74 + p75;
                    d75 = d74;
                }
                if (v75 > v61 + p75) {
                    v75 = v61 + p75;
                    d75 = d61;
                }
                if (v75 > v88 + p75) {
                    v75 = v88 + p75;
                    d75 = d88;
                }
            }
            if (rc.onTheMap(l93)) {
                p93 = turnsToMoveMiner(l93);
                if (v93 > v81 + p93) {
                    v93 = v81 + p93;
                    d93 = d81;
                }
                if (v93 > v94 + p93) {
                    v93 = v94 + p93;
                    d93 = d94;
                }
                if (v93 > v107 + p93) {
                    v93 = v107 + p93;
                    d93 = d107;
                }
                if (v93 > v80 + p93) {
                    v93 = v80 + p93;
                    d93 = d80;
                }
            }
            if (rc.onTheMap(l101)) {
                p101 = turnsToMoveMiner(l101);
                if (v101 > v87 + p101) {
                    v101 = v87 + p101;
                    d101 = d87;
                }
                if (v101 > v100 + p101) {
                    v101 = v100 + p101;
                    d101 = d100;
                }
                if (v101 > v113 + p101) {
                    v101 = v113 + p101;
                    d101 = d113;
                }
                if (v101 > v88 + p101) {
                    v101 = v88 + p101;
                    d101 = d88;
                }
            }
            if (rc.onTheMap(l135)) {
                p135 = turnsToMoveMiner(l135);
                if (v135 > v123 + p135) {
                    v135 = v123 + p135;
                    d135 = d123;
                }
                if (v135 > v122 + p135) {
                    v135 = v122 + p135;
                    d135 = d122;
                }
                if (v135 > v121 + p135) {
                    v135 = v121 + p135;
                    d135 = d121;
                }
                if (v135 > v136 + p135) {
                    v135 = v136 + p135;
                    d135 = d136;
                }
            }
            if (rc.onTheMap(l137)) {
                p137 = turnsToMoveMiner(l137);
                if (v137 > v123 + p137) {
                    v137 = v123 + p137;
                    d137 = d123;
                }
                if (v137 > v124 + p137) {
                    v137 = v124 + p137;
                    d137 = d124;
                }
                if (v137 > v125 + p137) {
                    v137 = v125 + p137;
                    d137 = d125;
                }
                if (v137 > v136 + p137) {
                    v137 = v136 + p137;
                    d137 = d136;
                }
            }
            if (rc.onTheMap(l42)) {
                p42 = turnsToMoveMiner(l42);
                if (v42 > v56 + p42) {
                    v42 = v56 + p42;
                    d42 = d56;
                }
                if (v42 > v43 + p42) {
                    v42 = v43 + p42;
                    d42 = d43;
                }
                if (v42 > v55 + p42) {
                    v42 = v55 + p42;
                    d42 = d55;
                }
            }
            if (rc.onTheMap(l48)) {
                p48 = turnsToMoveMiner(l48);
                if (v48 > v60 + p48) {
                    v48 = v60 + p48;
                    d48 = d60;
                }
                if (v48 > v47 + p48) {
                    v48 = v47 + p48;
                    d48 = d47;
                }
                if (v48 > v61 + p48) {
                    v48 = v61 + p48;
                    d48 = d61;
                }
            }
            if (rc.onTheMap(l120)) {
                p120 = turnsToMoveMiner(l120);
                if (v120 > v108 + p120) {
                    v120 = v108 + p120;
                    d120 = d108;
                }
                if (v120 > v121 + p120) {
                    v120 = v121 + p120;
                    d120 = d121;
                }
                if (v120 > v107 + p120) {
                    v120 = v107 + p120;
                    d120 = d107;
                }
            }
            if (rc.onTheMap(l126)) {
                p126 = turnsToMoveMiner(l126);
                if (v126 > v112 + p126) {
                    v126 = v112 + p126;
                    d126 = d112;
                }
                if (v126 > v113 + p126) {
                    v126 = v113 + p126;
                    d126 = d113;
                }
                if (v126 > v125 + p126) {
                    v126 = v125 + p126;
                    d126 = d125;
                }
            }
            if (rc.onTheMap(l30)) {
                p30 = turnsToMoveMiner(l30);
                if (v30 > v44 + p30) {
                    v30 = v44 + p30;
                    d30 = d44;
                }
                if (v30 > v43 + p30) {
                    v30 = v43 + p30;
                    d30 = d43;
                }
                if (v30 > v31 + p30) {
                    v30 = v31 + p30;
                    d30 = d31;
                }
                if (v30 > v42 + p30) {
                    v30 = v42 + p30;
                    d30 = d42;
                }
            }
            if (rc.onTheMap(l34)) {
                p34 = turnsToMoveMiner(l34);
                if (v34 > v46 + p34) {
                    v34 = v46 + p34;
                    d34 = d46;
                }
                if (v34 > v47 + p34) {
                    v34 = v47 + p34;
                    d34 = d47;
                }
                if (v34 > v33 + p34) {
                    v34 = v33 + p34;
                    d34 = d33;
                }
                if (v34 > v48 + p34) {
                    v34 = v48 + p34;
                    d34 = d48;
                }
            }
            if (rc.onTheMap(l54)) {
                p54 = turnsToMoveMiner(l54);
                if (v54 > v68 + p54) {
                    v54 = v68 + p54;
                    d54 = d68;
                }
                if (v54 > v55 + p54) {
                    v54 = v55 + p54;
                    d54 = d55;
                }
                if (v54 > v67 + p54) {
                    v54 = v67 + p54;
                    d54 = d67;
                }
                if (v54 > v42 + p54) {
                    v54 = v42 + p54;
                    d54 = d42;
                }
            }
            if (rc.onTheMap(l62)) {
                p62 = turnsToMoveMiner(l62);
                if (v62 > v74 + p62) {
                    v62 = v74 + p62;
                    d62 = d74;
                }
                if (v62 > v61 + p62) {
                    v62 = v61 + p62;
                    d62 = d61;
                }
                if (v62 > v75 + p62) {
                    v62 = v75 + p62;
                    d62 = d75;
                }
                if (v62 > v48 + p62) {
                    v62 = v48 + p62;
                    d62 = d48;
                }
            }
            if (rc.onTheMap(l106)) {
                p106 = turnsToMoveMiner(l106);
                if (v106 > v94 + p106) {
                    v106 = v94 + p106;
                    d106 = d94;
                }
                if (v106 > v107 + p106) {
                    v106 = v107 + p106;
                    d106 = d107;
                }
                if (v106 > v93 + p106) {
                    v106 = v93 + p106;
                    d106 = d93;
                }
                if (v106 > v120 + p106) {
                    v106 = v120 + p106;
                    d106 = d120;
                }
            }
            if (rc.onTheMap(l114)) {
                p114 = turnsToMoveMiner(l114);
                if (v114 > v100 + p114) {
                    v114 = v100 + p114;
                    d114 = d100;
                }
                if (v114 > v113 + p114) {
                    v114 = v113 + p114;
                    d114 = d113;
                }
                if (v114 > v101 + p114) {
                    v114 = v101 + p114;
                    d114 = d101;
                }
                if (v114 > v126 + p114) {
                    v114 = v126 + p114;
                    d114 = d126;
                }
            }
            if (rc.onTheMap(l134)) {
                p134 = turnsToMoveMiner(l134);
                if (v134 > v122 + p134) {
                    v134 = v122 + p134;
                    d134 = d122;
                }
                if (v134 > v121 + p134) {
                    v134 = v121 + p134;
                    d134 = d121;
                }
                if (v134 > v135 + p134) {
                    v134 = v135 + p134;
                    d134 = d135;
                }
                if (v134 > v120 + p134) {
                    v134 = v120 + p134;
                    d134 = d120;
                }
            }
            if (rc.onTheMap(l138)) {
                p138 = turnsToMoveMiner(l138);
                if (v138 > v124 + p138) {
                    v138 = v124 + p138;
                    d138 = d124;
                }
                if (v138 > v125 + p138) {
                    v138 = v125 + p138;
                    d138 = d125;
                }
                if (v138 > v137 + p138) {
                    v138 = v137 + p138;
                    d138 = d137;
                }
                if (v138 > v126 + p138) {
                    v138 = v126 + p138;
                    d138 = d126;
                }
            }
            int dx = target.x - l84.x;
            int dy = target.y - l84.y;
            switch(dx){
                case -4:
                    switch(dy){
                        case -2: return d30;
                        case -1: return d31;
                        case  0: return d32;
                        case  1: return d33;
                        case  2: return d34;
                    }break;
                case -3:
                    switch(dy){
                        case -3: return d42;
                        case -2: return d43;
                        case -1: return d44;
                        case  0: return d45;
                        case  1: return d46;
                        case  2: return d47;
                        case  3: return d48;
                    }break;
                case -2:
                    switch(dy){
                        case -4: return d54;
                        case -3: return d55;
                        case -2: return d56;
                        case -1: return d57;
                        case  0: return d58;
                        case  1: return d59;
                        case  2: return d60;
                        case  3: return d61;
                        case  4: return d62;
                    }break;
                case -1:
                    switch(dy){
                        case -4: return d67;
                        case -3: return d68;
                        case -2: return d69;
                        case -1: return d70;
                        case  0: return d71;
                        case  1: return d72;
                        case  2: return d73;
                        case  3: return d74;
                        case  4: return d75;
                    }break;
                case 0:
                    switch(dy){
                        case -4: return d80;
                        case -3: return d81;
                        case -2: return d82;
                        case -1: return d83;
                        case  0: return d84;
                        case  1: return d85;
                        case  2: return d86;
                        case  3: return d87;
                        case  4: return d88;
                    }break;
                case 1:
                    switch(dy){
                        case -4: return d93;
                        case -3: return d94;
                        case -2: return d95;
                        case -1: return d96;
                        case  0: return d97;
                        case  1: return d98;
                        case  2: return d99;
                        case  3: return d100;
                        case  4: return d101;
                    }break;
                case 2:
                    switch(dy){
                        case -4: return d106;
                        case -3: return d107;
                        case -2: return d108;
                        case -1: return d109;
                        case  0: return d110;
                        case  1: return d111;
                        case  2: return d112;
                        case  3: return d113;
                        case  4: return d114;
                    }break;
                case 3:
                    switch(dy){
                        case -3: return d120;
                        case -2: return d121;
                        case -1: return d122;
                        case  0: return d123;
                        case  1: return d124;
                        case  2: return d125;
                        case  3: return d126;
                    }break;
                case 4:
                    switch(dy){
                        case -2: return d134;
                        case -1: return d135;
                        case  0: return d136;
                        case  1: return d137;
                        case  2: return d138;
                    }break;
            }
            Direction ans = null;
            //If bytecode is an issue, change ints to doubles
            double bestEstimation = 100000;
            dSQ84 = l84.distanceSquaredTo(target);

            dSQ30 = l30.distanceSquaredTo(target);
            dist30 = Math.sqrt(dSQ30) + v30;
            if(bestEstimation > dist30 && dSQ84 > dSQ30){
                bestEstimation = dist30;
                ans = d30;
            }
            dSQ31 = l31.distanceSquaredTo(target);
            dist31 = Math.sqrt(dSQ31) + v31;
            if(bestEstimation > dist31 && dSQ84 > dSQ31){
                bestEstimation = dist31;
                ans = d31;
            }
            dSQ32 = l32.distanceSquaredTo(target);
            dist32 = Math.sqrt(dSQ32) + v32;
            if(bestEstimation > dist32 && dSQ84 > dSQ32){
                bestEstimation = dist32;
                ans = d32;
            }
            dSQ33 = l33.distanceSquaredTo(target);
            dist33 = Math.sqrt(dSQ33) + v33;
            if(bestEstimation > dist33 && dSQ84 > dSQ33){
                bestEstimation = dist33;
                ans = d33;
            }
            dSQ34 = l34.distanceSquaredTo(target);
            dist34 = Math.sqrt(dSQ34) + v34;
            if(bestEstimation > dist34 && dSQ84 > dSQ34){
                bestEstimation = dist34;
                ans = d34;
            }
            dSQ42 = l42.distanceSquaredTo(target);
            dist42 = Math.sqrt(dSQ42) + v42;
            if(bestEstimation > dist42 && dSQ84 > dSQ42){
                bestEstimation = dist42;
                ans = d42;
            }
            dSQ48 = l48.distanceSquaredTo(target);
            dist48 = Math.sqrt(dSQ48) + v48;
            if(bestEstimation > dist48 && dSQ84 > dSQ48){
                bestEstimation = dist48;
                ans = d48;
            }
            dSQ54 = l54.distanceSquaredTo(target);
            dist54 = Math.sqrt(dSQ54) + v54;
            if(bestEstimation > dist54 && dSQ84 > dSQ54){
                bestEstimation = dist54;
                ans = d54;
            }
            dSQ62 = l62.distanceSquaredTo(target);
            dist62 = Math.sqrt(dSQ62) + v62;
            if(bestEstimation > dist62 && dSQ84 > dSQ62){
                bestEstimation = dist62;
                ans = d62;
            }
            dSQ67 = l67.distanceSquaredTo(target);
            dist67 = Math.sqrt(dSQ67) + v67;
            if(bestEstimation > dist67 && dSQ84 > dSQ67){
                bestEstimation = dist67;
                ans = d67;
            }
            dSQ75 = l75.distanceSquaredTo(target);
            dist75 = Math.sqrt(dSQ75) + v75;
            if(bestEstimation > dist75 && dSQ84 > dSQ75){
                bestEstimation = dist75;
                ans = d75;
            }
            dSQ80 = l80.distanceSquaredTo(target);
            dist80 = Math.sqrt(dSQ80) + v80;
            if(bestEstimation > dist80 && dSQ84 > dSQ80){
                bestEstimation = dist80;
                ans = d80;
            }
            dSQ88 = l88.distanceSquaredTo(target);
            dist88 = Math.sqrt(dSQ88) + v88;
            if(bestEstimation > dist88 && dSQ84 > dSQ88){
                bestEstimation = dist88;
                ans = d88;
            }
            dSQ93 = l93.distanceSquaredTo(target);
            dist93 = Math.sqrt(dSQ93) + v93;
            if(bestEstimation > dist93 && dSQ84 > dSQ93){
                bestEstimation = dist93;
                ans = d93;
            }
            dSQ101 = l101.distanceSquaredTo(target);
            dist101 = Math.sqrt(dSQ101) + v101;
            if(bestEstimation > dist101 && dSQ84 > dSQ101){
                bestEstimation = dist101;
                ans = d101;
            }
            dSQ106 = l106.distanceSquaredTo(target);
            dist106 = Math.sqrt(dSQ106) + v106;
            if(bestEstimation > dist106 && dSQ84 > dSQ106){
                bestEstimation = dist106;
                ans = d106;
            }
            dSQ114 = l114.distanceSquaredTo(target);
            dist114 = Math.sqrt(dSQ114) + v114;
            if(bestEstimation > dist114 && dSQ84 > dSQ114){
                bestEstimation = dist114;
                ans = d114;
            }
            dSQ120 = l120.distanceSquaredTo(target);
            dist120 = Math.sqrt(dSQ120) + v120;
            if(bestEstimation > dist120 && dSQ84 > dSQ120){
                bestEstimation = dist120;
                ans = d120;
            }
            dSQ126 = l126.distanceSquaredTo(target);
            dist126 = Math.sqrt(dSQ126) + v126;
            if(bestEstimation > dist126 && dSQ84 > dSQ126){
                bestEstimation = dist126;
                ans = d126;
            }
            dSQ134 = l134.distanceSquaredTo(target);
            dist134 = Math.sqrt(dSQ134) + v134;
            if(bestEstimation > dist134 && dSQ84 > dSQ134){
                bestEstimation = dist134;
                ans = d134;
            }
            dSQ135 = l135.distanceSquaredTo(target);
            dist135 = Math.sqrt(dSQ135) + v135;
            if(bestEstimation > dist135 && dSQ84 > dSQ135){
                bestEstimation = dist135;
                ans = d135;
            }
            dSQ136 = l136.distanceSquaredTo(target);
            dist136 = Math.sqrt(dSQ136) + v136;
            if(bestEstimation > dist136 && dSQ84 > dSQ136){
                bestEstimation = dist136;
                ans = d136;
            }
            dSQ137 = l137.distanceSquaredTo(target);
            dist137 = Math.sqrt(dSQ137) + v137;
            if(bestEstimation > dist137 && dSQ84 > dSQ137){
                bestEstimation = dist137;
                ans = d137;
            }
            dSQ138 = l138.distanceSquaredTo(target);
            dist138 = Math.sqrt(dSQ138) + v138;
            if(bestEstimation > dist138 && dSQ84 > dSQ138){
                ans = d138;
            }
            return ans;

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private int turnsToMoveMiner(MapLocation loc) throws GameActionException {//Edit to make into miner
        switch(rc.senseRubble(loc)){
            case  0:                                     return  2;
            case  1: case  2: case  3: case  4: case  5: return  3;
            case  6: case  7: case  8: case  9: case 10: return  4;
            case 11: case 12: case 13: case 14: case 15: return  5;
            case 16: case 17: case 18: case 19: case 20: return  6;
            case 21: case 22: case 23: case 24: case 25: return  7;
            case 26: case 27: case 28: case 29: case 30: return  8;
            case 31: case 32: case 33: case 34: case 35: return  9;
            case 36: case 37: case 38: case 39: case 40: return 10;
            case 41: case 42: case 43: case 44: case 45: return 11;
            case 46: case 47: case 48: case 49: case 50: return 12;
            case 51: case 52: case 53: case 54: case 55: return 13;
            case 56: case 57: case 58: case 59: case 60: return 14;
            case 61: case 62: case 63: case 64: case 65: return 15;
            case 66: case 67: case 68: case 69: case 70: return 16;
            case 71: case 72: case 73: case 74: case 75: return 17;
            case 76: case 77: case 78: case 79: case 80: return 18;
            case 81: case 82: case 83: case 84: case 85: return 19;
            case 86: case 87: case 88: case 89: case 90: return 20;
            case 91: case 92: case 93: case 94: case 95: return 21;
            default:                                     return 22;
        }
    }

    HashSet<MapLocation> exploredLocations = new HashSet<>();
    public void newExploreLocation(){
        exploreTarget = new MapLocation((int)(Math.random()*width),(int)(Math.random()*height));
        while(exploredLocations.contains(exploreTarget) || Clock.getBytecodesLeft() > 5500){
            exploreTarget = new MapLocation((int)(Math.random()*width),(int)(Math.random()*height));
        }
        exploredLocations.add(exploreTarget);
    }

    public MapLocation getExploreTarget(){
        if(rc.getLocation().equals(exploreTarget)) newExploreLocation();
        return exploreTarget;
    }
}
