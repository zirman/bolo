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
    Boat(tileInd(x = 14, y = 10)),
    Wall(tileInd(x = 14, y = 2)),
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
    Road(tileInd(x = 4, y = 5)),
    Forest0(tileInd(x = 0, y = 4)),
    Forest1(tileInd(x = 1, y = 4)),
    Forest2(tileInd(x = 2, y = 4)),
    Forest3(tileInd(x = 0, y = 5)),
    Forest4(tileInd(x = 1, y = 5)),
    Forest5(tileInd(x = 2, y = 5)),
    Forest6(tileInd(x = 0, y = 6)),
    Forest7(tileInd(x = 1, y = 6)),
    Forest8(tileInd(x = 2, y = 6)),
    Forest9(tileInd(x = 1, y = 12)),
    ForestMined0(tileInd(x = 0, y = 7)),
    ForestMined1(tileInd(x = 1, y = 7)),
    ForestMined2(tileInd(x = 2, y = 7)),
    ForestMined3(tileInd(x = 0, y = 8)),
    ForestMined4(tileInd(x = 1, y = 8)),
    ForestMined5(tileInd(x = 2, y = 8)),
    ForestMined6(tileInd(x = 0, y = 9)),
    ForestMined7(tileInd(x = 1, y = 9)),
    ForestMined8(tileInd(x = 2, y = 9)),
    ForestMined9(tileInd(x = 5, y = 12)),
    Rubble(tileInd(x = 3, y = 12)),
    Grass(tileInd(x = 0, y = 12)),
    DamagedWall(tileInd(x = 8, y = 12)),
    SeaMined(tileInd(x = 9, y = 7)),
    SwampMined(tileInd(x = 6, y = 12)),
    CraterMined(tileInd(x = 6, y = 2)),
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