package client

enum class ImageTile(val index: Int) {
    Sea0(tileInd(x = 9, y = 4)),
    Sea1(tileInd(x = 10, y = 4)),
    Sea2(tileInd(x = 11, y = 4)),
    Sea3(tileInd(x = 9, y = 5)),
    Sea4(tileInd(x = 10, y = 5)),
    Sea5(tileInd(x = 11, y = 5)),
    Sea6(tileInd(x = 9, y = 6)),
    Sea7(tileInd(x = 10, y = 6)),
    Sea8(tileInd(x = 11, y = 6)),
    Boat0(tileInd(x = 3, y = 13)),
    Boat1(tileInd(x = 4, y = 13)),
    Boat2(tileInd(x = 5, y = 13)),
    Boat3(tileInd(x = 6, y = 13)),
    Boat4(tileInd(x = 7, y = 13)),
    Boat5(tileInd(x = 8, y = 13)),
    Boat6(tileInd(x = 9, y = 13)),
    Boat7(tileInd(x = 10, y = 13)),
    Wall0(tileInd(x = 12, y = 0)),
    Wall1(tileInd(x = 13, y = 0)),
    Wall2(tileInd(x = 14, y = 0)),
    Wall3(tileInd(x = 15, y = 0)),
    Wall4(tileInd(x = 12, y = 1)),
    Wall5(tileInd(x = 13, y = 1)),
    Wall6(tileInd(x = 14, y = 1)),
    Wall7(tileInd(x = 15, y = 1)),
    Wall8(tileInd(x = 12, y = 2)),
    Wall9(tileInd(x = 13, y = 2)),
    Wall10(tileInd(x = 14, y = 2)),
    Wall11(tileInd(x = 15, y = 2)),
    Wall12(tileInd(x = 12, y = 3)),
    Wall13(tileInd(x = 13, y = 3)),
    Wall14(tileInd(x = 14, y = 3)),
    Wall15(tileInd(x = 15, y = 3)),
    Wall16(tileInd(x = 12, y = 4)),
    Wall17(tileInd(x = 13, y = 4)),
    Wall18(tileInd(x = 14, y = 4)),
    Wall19(tileInd(x = 15, y = 4)),
    Wall20(tileInd(x = 12, y = 5)),
    Wall21(tileInd(x = 13, y = 5)),
    Wall22(tileInd(x = 14, y = 5)),
    Wall23(tileInd(x = 15, y = 5)),
    Wall24(tileInd(x = 12, y = 6)),
    Wall25(tileInd(x = 13, y = 6)),
    Wall26(tileInd(x = 14, y = 6)),
    Wall27(tileInd(x = 15, y = 6)),
    Wall28(tileInd(x = 12, y = 7)),
    Wall29(tileInd(x = 13, y = 7)),
    Wall30(tileInd(x = 14, y = 7)),
    Wall31(tileInd(x = 15, y = 7)),
    Wall32(tileInd(x = 12, y = 8)),
    Wall33(tileInd(x = 13, y = 8)),
    Wall34(tileInd(x = 14, y = 8)),
    Wall35(tileInd(x = 15, y = 8)),
    Wall36(tileInd(x = 12, y = 9)),
    Wall37(tileInd(x = 13, y = 9)),
    Wall38(tileInd(x = 14, y = 9)),
    Wall39(tileInd(x = 15, y = 9)),
    Wall40(tileInd(x = 12, y = 10)),
    Wall41(tileInd(x = 13, y = 10)),
    Wall42(tileInd(x = 14, y = 10)),
    Wall43(tileInd(x = 15, y = 10)),
    Wall44(tileInd(x = 12, y = 11)),
    Wall45(tileInd(x = 13, y = 11)),
    Wall46(tileInd(x = 14, y = 11)),
    River0(tileInd(x = 8, y = 0)),
    River1(tileInd(x = 9, y = 0)),
    River2(tileInd(x = 10, y = 0)),
    River3(tileInd(x = 11, y = 0)),
    River4(tileInd(x = 8, y = 1)),
    River5(tileInd(x = 9, y = 1)),
    River6(tileInd(x = 10, y = 1)),
    River7(tileInd(x = 11, y = 1)),
    River8(tileInd(x = 8, y = 2)),
    River9(tileInd(x = 9, y = 2)),
    River10(tileInd(x = 10, y = 2)),
    River11(tileInd(x = 11, y = 2)),
    River12(tileInd(x = 8, y = 3)),
    River13(tileInd(x = 9, y = 3)),
    River14(tileInd(x = 10, y = 3)),
    River15(tileInd(x = 11, y = 3)),
    Swamp(tileInd(x = 2, y = 12)),
    Crater0(tileInd(x = 0, y = 0)),
    Crater1(tileInd(x = 1, y = 0)),
    Crater2(tileInd(x = 2, y = 0)),
    Crater3(tileInd(x = 3, y = 0)),
    Crater4(tileInd(x = 0, y = 1)),
    Crater5(tileInd(x = 1, y = 1)),
    Crater6(tileInd(x = 2, y = 1)),
    Crater7(tileInd(x = 3, y = 1)),
    Crater8(tileInd(x = 0, y = 2)),
    Crater9(tileInd(x = 1, y = 2)),
    Crater10(tileInd(x = 2, y = 2)),
    Crater11(tileInd(x = 3, y = 2)),
    Crater12(tileInd(x = 0, y = 3)),
    Crater13(tileInd(x = 1, y = 3)),
    Crater14(tileInd(x = 2, y = 3)),
    Crater15(tileInd(x = 3, y = 3)),
    CraterMined0(tileInd(x = 4, y = 0)),
    CraterMined1(tileInd(x = 5, y = 0)),
    CraterMined2(tileInd(x = 6, y = 0)),
    CraterMined3(tileInd(x = 7, y = 0)),
    CraterMined4(tileInd(x = 4, y = 1)),
    CraterMined5(tileInd(x = 5, y = 1)),
    CraterMined6(tileInd(x = 6, y = 1)),
    CraterMined7(tileInd(x = 7, y = 1)),
    CraterMined8(tileInd(x = 4, y = 2)),
    CraterMined9(tileInd(x = 5, y = 2)),
    CraterMined10(tileInd(x = 6, y = 2)),
    CraterMined11(tileInd(x = 7, y = 2)),
    CraterMined12(tileInd(x = 4, y = 3)),
    CraterMined13(tileInd(x = 5, y = 3)),
    CraterMined14(tileInd(x = 6, y = 3)),
    CraterMined15(tileInd(x = 7, y = 3)),
    Road(tileInd(x = 4, y = 5)),
    Tree0(tileInd(x = 0, y = 4)),
    Tree1(tileInd(x = 1, y = 4)),
    Tree2(tileInd(x = 2, y = 4)),
    Tree3(tileInd(x = 0, y = 5)),
    Tree4(tileInd(x = 1, y = 5)),
    Tree5(tileInd(x = 2, y = 5)),
    Tree6(tileInd(x = 0, y = 6)),
    Tree7(tileInd(x = 1, y = 6)),
    Tree8(tileInd(x = 2, y = 6)),
    Tree9(tileInd(x = 1, y = 12)),
    TreeMined0(tileInd(x = 0, y = 7)),
    TreeMined1(tileInd(x = 1, y = 7)),
    TreeMined2(tileInd(x = 2, y = 7)),
    TreeMined3(tileInd(x = 0, y = 8)),
    TreeMined4(tileInd(x = 1, y = 8)),
    TreeMined5(tileInd(x = 2, y = 8)),
    TreeMined6(tileInd(x = 0, y = 9)),
    TreeMined7(tileInd(x = 1, y = 9)),
    TreeMined8(tileInd(x = 2, y = 9)),
    TreeMined9(tileInd(x = 5, y = 12)),
    Rubble(tileInd(x = 3, y = 12)),
    Grass(tileInd(x = 0, y = 12)),
    DamagedWall(tileInd(x = 8, y = 12)),
    SeaMined(tileInd(x = 9, y = 7)),
    SwampMined(tileInd(x = 6, y = 12)),
    RoadMined(tileInd(x = 4, y = 8)),
    RubbleMined(tileInd(x = 7, y = 12)),
    GrassMined(tileInd(x = 4, y = 12)),
    BaseNeutral(tileInd(x = 0, y = 13)),
    BaseFriendly(tileInd(x = 1, y = 13)),
    BaseHostile(tileInd(x = 2, y = 13)),
    PillHostile0(tileInd(x = 0, y = 14)),
    PillHostile1(tileInd(x = 1, y = 14)),
    PillHostile2(tileInd(x = 2, y = 14)),
    PillHostile3(tileInd(x = 3, y = 14)),
    PillHostile4(tileInd(x = 4, y = 14)),
    PillHostile5(tileInd(x = 5, y = 14)),
    PillHostile6(tileInd(x = 6, y = 14)),
    PillHostile7(tileInd(x = 7, y = 14)),
    PillHostile8(tileInd(x = 8, y = 14)),
    PillHostile9(tileInd(x = 9, y = 14)),
    PillHostile10(tileInd(x = 10, y = 14)),
    PillHostile11(tileInd(x = 11, y = 14)),
    PillHostile12(tileInd(x = 12, y = 14)),
    PillHostile13(tileInd(x = 13, y = 14)),
    PillHostile14(tileInd(x = 14, y = 14)),
    PillHostile15(tileInd(x = 15, y = 14)),
    PillFriendly0(tileInd(x = 0, y = 15)),
    PillFriendly1(tileInd(x = 1, y = 15)),
    PillFriendly2(tileInd(x = 2, y = 15)),
    PillFriendly3(tileInd(x = 3, y = 15)),
    PillFriendly4(tileInd(x = 4, y = 15)),
    PillFriendly5(tileInd(x = 5, y = 15)),
    PillFriendly6(tileInd(x = 6, y = 15)),
    PillFriendly7(tileInd(x = 7, y = 15)),
    PillFriendly8(tileInd(x = 8, y = 15)),
    PillFriendly9(tileInd(x = 9, y = 15)),
    PillFriendly10(tileInd(x = 10, y = 15)),
    PillFriendly11(tileInd(x = 11, y = 15)),
    PillFriendly12(tileInd(x = 12, y = 15)),
    PillFriendly13(tileInd(x = 13, y = 15)),
    PillFriendly14(tileInd(x = 14, y = 15)),
    PillFriendly15(tileInd(x = 15, y = 15)),
}
